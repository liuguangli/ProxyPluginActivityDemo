package com.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class BasePluginActivity extends Activity implements IProxyPluginAct{

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

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle var1) {

    }

    @Override
    public void onNewIntent(Intent var1) {

    }

    @Override
    public void onRestoreInstanceState(Bundle var1) {

    }

    public void startActivity(String className) {
        mProxy.startActivity(className);
    }

    @Override
    public void onCreate(Bundle var1) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void attach(Activity var1) {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onActivityResult(int var1, int var2, Intent var3) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public View findViewById(int id) {
        return mProxy.findViewById(id);
    }

    @Override
    public Resources getResources() {
        return mProxy.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return mProxy.getAssets();
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return mProxy.getLayoutInflater();
    }

    @Override
    public Resources.Theme getTheme() {
        return mProxy.getTheme();
    }

}
