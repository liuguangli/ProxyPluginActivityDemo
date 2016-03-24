# ProxyPluginActivityDemo
一个demo，代理方式实现插件化，一个最简单的插件框架雏形。
＃博客
文章首发：[插件化研究代之Activity注册｜大利猫](http://www.liuguangli.win/?p=387)

最近在研究Android应用的插件化开发，看了好几个相关的开源项目。  插件化都是在解决以下几个问题：
* [如何把插件apk中的代码和资源加载到当前虚拟机](http://www.liuguangli.win/?p=366)。
* 如何把插件apk中的四大组件注册到进程中。
* 如何防止插件apk中的资源和宿主apk中的资源引用冲突。

在上篇文章中我研究了[如何获取并使用插件apk中的资源](http://www.liuguangli.win/?p=370)的问题（文本、图片、布局等），前面两篇文章解决了插件化研究的第一个问题。本篇文章开始研究第二个问题：“注册”插件中的四大组件。
<br>

在安装apk的时候，应用管理服务PackageManagerService会解析apk，解析应用程序配置文件AndroidManifest.xml，并从里面得到得到应用得到应用程序的组件Activity、Service、Broadcast Receiver和Content Provider等信息，对应用的每个组件“登记”，“登记”之后，在启动某个Activity过程在ASM之行时对不“登记”然后“查有此人”从允许后续的启动行为。详细过程可以参考[《Android应用程序安装过程源代码分析》](http://blog.csdn.net/luoshengyang/article/details/6689748)。然而，插件apk并没有进行安装，自然apk中定义的四大组件也没有进行“登记”，那么问题来了：以Activity为例，如何启动插件中的Acivity？大体两种思路。

##一、 代理方式实现。
宿主端实现一个 PluginProxyActivity，使用这个Activity代理插件中的Activity的重要事务，例如生命周期调用、contentview设置、Activity跳转等事务。PluginProxyActivity注册在宿主中，启动插件中的Activity实际就是启动PluginProxyActivity，只是加载的布局和方法逻辑不一样而已。百度的插件框架dynamic-load-apk就是使用的这种方式。

##二、“占坑”方式实现。
启动Activity是一个复杂的过程，有很多环节：Activity.startActivity()->Activity.startActivityForResult()->Instrument.excuteStartActivity()->ASM.startActivity()。大概又这么几个环节，详细了解可以参考文章：[《深入理解Activity的启动过程》](http://www.cloudchou.com/android/post-788.html)。 所谓“占坑”在宿主端的AndroidManifest.xml注册一个不存在的Activity，可以取名为StubActivity，同样启动插件的Activity都是启动StubActivity，然后在启动Activity的某个环节，我们找个“临时”演员来代替StubActivity，这个临时演员就是插件中定义的Activity，这叫“瞒天过海”。如何找“临时”演员，这个过程又有很多几种实现手段，DroidPlugin、dwarf等框架实现手段个有不同，详细后续文章再讨论。<br>

简单解释了两种思路，本文先用demo来说说如何实现第一种思路（后续文章研究下第二思路）。

#PluginProxyActivity的实现

一、重写setContentView（int layoutResID）方法，使用插件的AssertManager加载布局资源。该方法提供给插件Activity调用。

    @Override
    public void setContentView(int layoutResID) {
        // do something plugin need
        Resources resources = PluginManager.getInstace().getResources();
        XmlPullParser xmlResourceParser = resources.getLayout(layoutResID);
        View viewFromPlugin = LayoutInflater.from(this).inflate(xmlResourceParser, null);
        setContentView(viewFromPlugin);

    }
二、 重写onCreate(Bundle savedInstanceState)方法，在这个方法中通过插件的Activity的类名，利用反射实例化插件Activity对象，并调用其onCreate方法。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String className = getIntent().getStringExtra("class");
        initPluginInstance(className);
        invokePluginOnCreate(savedInstanceState);
    }
    private void initPluginInstance(String className) {
        try {
            pluginClass = PluginManager.getInstace().getCloassLoader().loadClass(className);
            Constructor<?> localConstructor = pluginClass.getConstructor(new Class[]{});
            pluginInstance = localConstructor.newInstance(new Object[] {});
            // 把当前的代理Activity注入到插件中
            Method setProxy = pluginClass.getMethod("setProxy",
                    new Class[]{PluginProxyActivity.class});
            setProxy.setAccessible(true);
            setProxy.invoke(pluginInstance, new Object[] { this });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void invokePluginOnCreate(Bundle savedInstanceState) {
        try {
            Method onCreate = pluginClass.getDeclaredMethod("onCreate",
                    new Class[]{Bundle.class});
            onCreate.setAccessible(true);
            onCreate.invoke(pluginInstance, new Object[] { savedInstanceState });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
三、 重写其他生命周期函数并利用反射调用插件Activity的对应的生命周期函数，例如onPause方法。

    @Override
    protected void onPause() {
        super.onPause();
        invokePluginOnPause();
    }

    private void invokePluginOnPause() {
        try {
            Method onPause = pluginClass.getDeclaredMethod("onPause",
                    new Class[]{});
            onPause.setAccessible(true);
            onPause.invoke(pluginInstance, new Object[] {});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
四、 重载startAcivity(String className)方法，也就是使用使用定制的startActivity方法来启动插件Activity啦。

    public void startActivity(String className) {
        // do something plugin need
        Intent intent = new Intent(this,PluginProxyActivity.class);
        intent.putExtra("class",className);
        startActivity(intent);
    }

#约定插件Activity，插件Activity基类：BasePluginActivity的实现


一、 提供public void setProxy(PluginProxyActivity proxyPluginAct)方法，以获得PluginProxyActivity的引用。

     /**
     *
     * @param proxyPluginAct
     * provided this method to invoke by reflect . inject proxy
     */
    public void setProxy(PluginProxyActivity proxyPluginAct){
        mProxy = proxyPluginAct;
    }

二、重写setContentView(int layoutResID)，调用proxy的setContentView(int layoutResID)方法。

     /**
     * set layout to proxyActivity
     * @param layoutResID
     */
    @Override
    public void setContentView(int layoutResID) {
        if (mProxy != null){
            mProxy.setContentView(layoutResID);
        } else {
            super.setContentView(layoutResID);
        }

    }
三、 定制startActivity(String className)方法来启动Activity，调用proxy的startActivity方法。

    public void startActivity(String className) {
        mProxy.startActivity(className);
    }
#最重要的demo
demo实现啦一个插件的框架的最基本雏形，地址：
[https://github.com/liuguangli/ProxyPluginActivityDemo](https://github.com/liuguangli/ProxyPluginActivityDemo)，如果你有兴趣，一定要star，日后研究研究。代理方式实现起来比较简单，也比较好理解，但是有很多缺陷：一、插件Activity不能使用this关键字，比如this.finish()方法是无效的，真正掌管生命周期的是proxy应该调用proxy.finish()，所以百度开源框架 dynamic-load-apk使用that指向proxy，约定插件中使用that来代替this。二、 插件Activity无法深度演绎正直的Activity组件，可能有些高级特性无法使用。

总之，不够透明，插件开发需要定义自己的规范。既然如此，有没有更好的方案？当然有，后续文章继续研究插件化如何注册组件的第二类思路：“占坑”方式实现插件Activity的注册。
