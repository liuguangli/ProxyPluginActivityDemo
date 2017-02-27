package activity.plugin.proxy.demo.com.proxypluginactivitydemo;

import android.content.res.Resources;
import android.os.Environment;

import com.plugin.PluginManager;

/**
 * Created by liuguangli on 16/3/20.
 */
public class HostApplication extends android.app.Application {
    private  Resources oldResource;
    @Override
    public void onCreate() {
        super.onCreate();
        String plugPath = Environment.getExternalStorageDirectory()+"/apkbeloaded-debug.apk";
        oldResource = super.getResources();
        PluginManager.getInstance().install(this,plugPath);
    }

    @Override
    public Resources getResources() {

        Resources newRes =  PluginManager.getInstance().getAppResource();
        return newRes == null?oldResource:newRes;
    }
}
