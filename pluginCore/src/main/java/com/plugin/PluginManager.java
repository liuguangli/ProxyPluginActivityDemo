package com.plugin;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

/**
 * Created by liuguangli on 16/3/20.
 */
public class PluginManager {
    private static final String TAG  =  "PluginManager";
    private static final String OPT_DIR = "opt";
    private static final String LIB_DIR = "lib";
    private  static  PluginManager instance;
    private AssetManager assetManager;
    private Resources resources;
    private Resources appResources;
    private ClassLoader classLoader;
    private PackageInfo packageInfo;
    private Context pluginContext;

    public static PluginManager getInstance(){
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }
    public void install(Context context,String apkPath){
        Log.d(TAG, "开始安装插件");
        File file = context.getDir("apk", Context.MODE_PRIVATE);
        file.mkdir();
        String desPath = file.getAbsolutePath()+"/bundle1.apk";
        copy(apkPath, desPath);
        installClass(context, desPath);
        initAppResources(context, desPath);
        installRes(context, desPath);
        initPluginInfo(context,desPath);
    }
    public void copy( String src, String des) {
        try {
            Log.d(TAG, "拷贝插件包：" + src + "->" + des);
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
            Log.d(TAG, "拷贝完成：");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "拷贝失败：" + e.getMessage());
        }
    }

    private void installClass(Context context, String apkPath) {

        Log.d(TAG, "开始安装dex");
        File optimizedDirectory = context.getDir( OPT_DIR, Context.MODE_PRIVATE);
        File libDirectory = context.getDir(LIB_DIR,Context.MODE_PRIVATE);
        classLoader = new DexClassLoader(
                apkPath,
                optimizedDirectory.getAbsolutePath(),
                libDirectory.getAbsolutePath(),
                this.getClass().getClassLoader());
        Log.d(TAG, "安装dex完成");

    }


    private void installRes(Context context,String apkPath) {
        Log.d(TAG, "加载资源");
        createAssetManager(apkPath);
        resources = getBundleResource(context,apkPath);
    }

    private void initAppResources(Context context,String apkPath){
        try {
            Log.d(TAG, "初始化 Resource 对象");
            AssetManager assetManager = context.getAssets();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            appResources = new Resources(assetManager,
                    context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());
            Log.d(TAG, "初始化 Resource 完成");
        } catch (Throwable th) {
            th.printStackTrace();
            Log.d(TAG, "初始化 Resource 失败:" + th.getMessage());
        }

    }

    private AssetManager createAssetManager(String apkPath) {
        try {
            Log.d(TAG, "创建AssetManager");
            assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            return assetManager;
        } catch (Throwable th) {
            th.printStackTrace();
            Log.d(TAG, "创建AssetManager失败：" + th.getMessage());
        }
        return null;
    }
    private void  initPluginInfo(Context context,String apkPath){
        PackageManager pm = context.getPackageManager();
        packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        String packageName = context.getPackageName();
        if (packageName != null && packageName.length() > 0) {
            final Application application = (Application) context;
            try {
                pluginContext = application.createPackageContext(packageName, Context.CONTEXT_RESTRICTED | Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            } catch (PackageManager.NameNotFoundException e) {
                String msg = String.format("创建pkg: %s 上下文失败", packageName);
               Log.e(TAG, "pluginContext 创建失败，" + msg);

            }
        }
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
        intent.putExtra("package",packageInfo.packageName);
        context.startActivity(intent);
    }

    private String getPluginMainActivity() {

        return packageInfo.activities[0].name;
    }


    public Resources getAppResource() {
        return appResources;
    }

    public Context getPluginContext() {
        return pluginContext;
    }
}
