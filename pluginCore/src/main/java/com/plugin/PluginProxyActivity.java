package com.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 *  proxy of plugin Activity in the host,must be define standard.
 *
 */
public class PluginProxyActivity extends Activity implements IProxyPluginAct{

    private static final String TAG = "PluginProxyActivity";
    private Class<?> pluginClass;
    private Object pluginInstance;
    private Context pluginContext;
    private MenuInflater menuInflater;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String className = getIntent().getStringExtra("class");
        initPluginInstance(className);
        pluginContext = PluginManager.getInstance().getPluginContext();
        invokePluginOnCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        invokePluginMethod("onStart",
                new Class[]{},
                new Object[]{});
    }

    @Override
    public void attach(Activity var1) {

        invokePluginMethod("attach",
                new Class[]{Activity.class},
                new Object[]{var1});
    }

    @Override
    public void onRestart() {
        super.onRestart();
        invokePluginMethod("onRestart",
                new Class[]{},
                new Object[]{});
    }

    @Override
    public void onActivityResult(int var1, int var2, Intent var3) {
        super.onActivityResult(var1, var2, var3);
        invokePluginMethod("onActivityResult",
                new Class[]{int.class, int.class, Intent.class},
                new Object[]{var1, var2, var3});
    }

    @Override
    public void onResume() {

        super.onResume();
        invokePluginMethod("onResume", new Class[]{}, new Object[]{});

    }

    private void initPluginInstance(String className) {
        try {
            Log.d(TAG, "创建实例：" + className);
            pluginClass = PluginManager.getInstance().getClassLoader().loadClass(className);
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
            Log.d(TAG, "创建实例：失败");
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        // do something plugin need
       Resources resources = pluginContext.getResources();
        XmlPullParser xmlResourceParser = resources.getLayout(layoutResID);
        rootView = LayoutInflater.from(this).inflate(xmlResourceParser, null);
        super.setContentView(rootView);

    }

    @Override
    public void startActivity(String className) {
        // do something plugin need
        Intent intent = new Intent(this,PluginProxyActivity.class);
        intent.putExtra("class",className);
        startActivity(intent);
    }

    @Override
    public View findViewById(int id) {
        //do something plugin need
        return rootView.findViewById(id);
    }
    private void invokePluginOnCreate(Bundle savedInstanceState) {
        try {
            Method onCreate = pluginClass.getDeclaredMethod("onCreate", new Class[] {Bundle.class});
            onCreate.setAccessible(true);
            onCreate.invoke(pluginInstance, savedInstanceState);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        invokePluginMethod("onPause", new Class[]{}, new Object[]{});
    }

    @Override
    public void onStop() {
        super.onStop();
        invokePluginMethod("onStop", new Class[]{}, new Object[]{});
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        invokePluginMethod("onDestroy", new Class[]{}, new Object[]{});
    }

    @Override
    public void onSaveInstanceState(Bundle var1) {
        super.onSaveInstanceState(var1);
        invokePluginMethod("onSaveInstanceState", new Class[]{Bundle.class}, new Object[]{var1});
    }

    @Override
    public void onNewIntent(Intent var1) {
        super.onNewIntent(var1);
        invokePluginMethod("onSaveInstanceState", new Class[]{Intent.class}, new Object[]{var1});

    }

    @Override
    public void onRestoreInstanceState(Bundle var1) {
        super.onRestoreInstanceState(var1);
        invokePluginMethod("onRestoreInstanceState", new Class[]{Bundle.class}, new Object[]{var1});

    }

    private void invokePluginMethod(String method,Class[] clss, Object[] params) {
        try {
            Method onPause = pluginClass.getDeclaredMethod(method,
                    clss);
            onPause.setAccessible(true);
            onPause.invoke(pluginInstance, params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AssetManager getAssets() {
        return pluginContext.getAssets();
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(pluginContext);
    }

    @Override
    public MenuInflater getMenuInflater() {
        if (menuInflater == null) {
            menuInflater = new MenuInflater(pluginContext);
        }
        return menuInflater;
    }


}
