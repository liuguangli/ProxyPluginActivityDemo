package com.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Created by liuguangli on 16/3/20.
 */
public class PluginManager {
    private static final String OPT_DIR = "opt";
    private  static  PluginManager instace;
    private AssetManager assetManager;
    private Resources resources;
    private ClassLoader classLoader;
    private PackageInfo packageInfo;
    public static PluginManager getInstace(){
        if (instace == null) {
            instace = new PluginManager();
        }
        return instace;
    }
    public void install(Context context,String apkPath){
        installClass(context,apkPath);
        installRes(context, apkPath);
        initPluginInfo(context,apkPath);
    }

    private void installClass(Context context, String apkPath) {


        File optimizedDirectory = context.getDir( OPT_DIR, Context.MODE_PRIVATE);

        classLoader = new DexClassLoader(
                apkPath, optimizedDirectory.getAbsolutePath(),
                "data/local/tmp/natives/",
                context.getClassLoader());


    }


    private void installRes(Context context,String apkPath) {
        createAssetManager(apkPath);
        resources = getBundleResource(context,apkPath);
    }

    private AssetManager createAssetManager(String apkPath) {
        try {
            assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            return assetManager;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }
    private void  initPluginInfo(Context context,String apkPath){
        PackageManager pm = context.getPackageManager();
        packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

    }

    public Resources getResources() {
        return resources;
    }

    public Resources getBundleResource(Context context, String apkPath){
        if (resources != null){
            return resources;
        }
        AssetManager assetManager = createAssetManager(apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    public ClassLoader getCloassLoader() {
        return classLoader;
    }

    public void startPlugin(Context context){
        String className = getPluginMainActivity();
        Intent intent = new Intent(context,PluginProxyActivity.class);
        intent.putExtra("class",className);
        context.startActivity(intent);
    }

    private String getPluginMainActivity() {

        return packageInfo.activities[0].name;
    }


}
