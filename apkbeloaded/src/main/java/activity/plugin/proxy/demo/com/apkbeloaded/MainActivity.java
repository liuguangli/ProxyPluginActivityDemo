package activity.plugin.proxy.demo.com.apkbeloaded;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.plugin.BasePluginActivity;


public class MainActivity extends BasePluginActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(getString(R.string.plugin));
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AnotherActivity.class.getName());
            }
        });
    }


}
