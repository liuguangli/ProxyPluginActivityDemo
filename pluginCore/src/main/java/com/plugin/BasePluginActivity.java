package com.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

public class BasePluginActivity extends Activity {

    private PluginProxyActivity mProxy;
    /**
     *
     * @param proxyPluginAct
     * provided this method to invoke by reflect . inject proxy
     */
    public void setProxy(PluginProxyActivity proxyPluginAct){
        mProxy = proxyPluginAct;
    }

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


    public void startActivity(String className) {
        mProxy.startActivity(className);
    }

    @Override
    public View findViewById(int id) {
        return mProxy.findViewById(id);
    }

    @Override
    public Resources getResources() {
        return mProxy.getResources();
    }
}
