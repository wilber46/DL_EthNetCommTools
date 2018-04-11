package win.wilber.commassistant.activity;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import win.wilber.commassistant.R;

public class Setting extends Activity implements OnClickListener {
    Button confirm;
    FrameLayout mBody;
    LocalActivityManager mLocalActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ExitApplication.getInstance().addActivity(this);
        mBody = (FrameLayout) findViewById(R.id.setting_body);
        confirm = (Button) findViewById(R.id.setting_confirm);
        mLocalActivity = new LocalActivityManager(this, true);
        mLocalActivity.dispatchCreate(null);
        View v = mLocalActivity.startActivity("preference", new Intent("android.intent.SerialPreference").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)).getDecorView();
        mBody.removeAllViews();
        mBody.addView(v);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
