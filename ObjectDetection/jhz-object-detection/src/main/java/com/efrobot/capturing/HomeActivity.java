package com.efrobot.capturing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.efrobot.library.mvp.utils.PreferencesUtils;

public class HomeActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button1:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent = new Intent(this, BinocularVisionActivity.class);
                startActivity(intent);
                break;

        }
        HomeActivity.this.finish();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}
