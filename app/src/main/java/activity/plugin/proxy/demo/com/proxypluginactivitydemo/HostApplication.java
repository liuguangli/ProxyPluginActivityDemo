package activity.plugin.proxy.demo.com.proxypluginactivitydemo;

import android.content.res.Resources;
import android.os.Environment;

import com.plugin.PluginManager;

/**
 * Created by liuguangli on 16/3/20.
 */
public class HostApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        String plugPath = Environment.getExternalStorageDirectory() + "/bundle2.apk";
        PluginManager.getInstance().install(this, plugPath);
    }


}
