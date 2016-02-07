package com.rwsw.fantasysurvivor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.rwsw.fantasysurvivor.BuildConfig;
import com.rwsw.fantasysurvivor.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends ActionBarActivity {

    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        TextView versionText = (TextView) findViewById(R.id.versionText);
        String version = "Version: " + BuildConfig.VERSION_NAME;
        versionText.setText(version);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Check is running in emulator and is available google play service
                // without a dialog message (false param)
                if (Build.BRAND.equalsIgnoreCase("generic") && !checkPlayServices(false)) {
                    Toast.makeText(FantasySurvivor.getContext(),"Incompatible Device.",Toast.LENGTH_LONG).show();
                } else {
                    if (checkPlayServices()) {
                        Intent intent = new Intent().setClass(SplashActivity.this, LoginActivity.class);
                        SplashActivity.this.startActivity(intent);
                        SplashActivity.this.finish();
                    } else {
                        TextView errorLoad = (TextView) SplashActivity.this.findViewById(R.id.loadErrorText);
                        errorLoad.setText("Google Play must be installed.");
                    }
                }
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantasySurvivor.registerNetworkReceiver();
    }

    // Overload
    // Show dialog if google play services is not available and try to fix it.
    private boolean checkPlayServices() {
        return checkPlayServices(true);
    }

    private boolean checkPlayServices(Boolean showDialog) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (showDialog) {
                if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                    showErrorDialog(status);
                } else {
                    TextView errorLoad = (TextView) this.findViewById(R.id.loadErrorText);
                    errorLoad.setText("This device is not supported.");
                }
                return false;
            } else return false;
        }

        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    TextView errorLoad = (TextView) this.findViewById(R.id.loadErrorText);
                    errorLoad.setText("Google Play Services must be installed.");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
