package com.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String className = getIntent().getStringExtra("class");
        initPluginInstance(className);
        invokePluginOnCreate(savedInstanceState);
    }

    private void initPluginInstance(String className) {
        try {
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
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        // do something plugin need
       Resources resources = PluginManager.getInstance().getResources();
        XmlPullParser xmlResourceParser = resources.getLayout(layoutResID);
        View viewFromPlugin = LayoutInflater.from(this).inflate(xmlResourceParser, null);
        super.setContentView(viewFromPlugin);

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
        return super.findViewById(id);
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

    @Override
    public Resources getResources() {
        return getApplication().getResources();
    }
}
