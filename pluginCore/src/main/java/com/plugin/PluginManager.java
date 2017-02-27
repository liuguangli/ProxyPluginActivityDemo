package com.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

/**
 * Created by liuguangli on 16/3/20.
 */
public class PluginManager {
    private static final String OPT_DIR = "opt";
    private static final String LIB_DIR = "lib";
    private  static  PluginManager instance;
    private AssetManager assetManager;
    private Resources resources;
    private Resources appResources;
    private ClassLoader classLoader;
    private PackageInfo packageInfo;
    public static PluginManager getInstance(){
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }
    public void install(Context context,String apkPath){
        File file = context.getDir("apk", Context.MODE_PRIVATE);
        file.mkdir();
        String desPath = file.getAbsolutePath()+"/apkbeloaded-debug.apk";
        copy(apkPath, desPath);
        installClass(context, desPath);
        initAppResources(context, desPath);
        installRes(context, desPath);
        initPluginInfo(context,desPath);
    }
    public void copy( String src, String des) {
        try {
            InputStream is = new FileInputStream(new File(src));
            FileOutputStream fos = new FileOutputStream(new File(des));
            byte[] buffer = new byte[1024];
            while (true) {
                int len = is.read(buffer);
                if (len == -1) {
                    break;
                }
                fos.write(buffer, 0, len);
            }
            is.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void installClass(Context context, String apkPath) {


        File optimizedDirectory = context.getDir( OPT_DIR, Context.MODE_PRIVATE);
        File libDirectory = context.getDir(LIB_DIR,Context.MODE_PRIVATE);
        classLoader = new DexClassLoader(
                apkPath, optimizedDirectory.getAbsolutePath(),
                libDirectory.getAbsolutePath(),
                context.getClassLoader());


    }


    private void installRes(Context context,String apkPath) {
        createAssetManager(apkPath);
        resources = getBundleResource(context,apkPath);
    }

    private void initAppResources(Context context,String apkPath){
        try {
            AssetManager assetManager = context.getAssets();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            appResources = new Resources(assetManager,
                    context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());

        } catch (Throwable th) {
            th.printStackTrace();
        }

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

    private Resources getBundleResource(Context context, String apkPath){
        if (resources != null){
            return resources;
        }
        AssetManager assetManager = createAssetManager(apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    public ClassLoader getClassLoader() {
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


    public Resources getAppResource() {
        return appResources;
    }
}
