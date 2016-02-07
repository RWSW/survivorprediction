package com.rwsw.fantasysurvivor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.util.RequestUtils;

/**
 * Created by Overlordyorch on 03/04/2014.
 */
public class NetworkNotificationActivity extends Activity {
    private Button checkWifiButton, checkServerButton = null;
    private TextView messageText = null;
    private ProgressBar checkingBar = null;
    private Boolean onPause = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_notice);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.onPause) return;

        Boolean server = RequestUtils.isServerOnline();
        Boolean device = RequestUtils.isNetworkAvailable();

        if (device && server) {
            this.goToStart();
            return;
        } else if (!device) {
            this.initializeUI(true);
        } else if (!server) {
            checkWifiButton.setVisibility(ProgressBar.INVISIBLE);
            this.checkServer();
            //this.initializeUI(false);
        }

        this.onPause = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.onPause) return;

        Boolean server = RequestUtils.isServerOnline();
        Boolean device = RequestUtils.isDeviceOnline();

        if (device && server) {
            this.goToStart();
        } else if (!device) {
            this.initializeUI(true);
        } else if (!server) {
            this.initializeUI(false);
        }

        this.onPause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.onPause = true;
    }

    protected void initializeUI(Boolean wifiError) {
        checkWifiButton = checkWifiButton != null ? checkWifiButton : (Button) findViewById(R.id.checkWifiButton);
        checkServerButton = checkServerButton != null ? checkServerButton : (Button) findViewById(R.id.checkServerButton);
        messageText = messageText != null ? messageText : (TextView) findViewById(R.id.messageText);
        checkingBar = checkingBar != null ? checkingBar : (ProgressBar) findViewById(R.id.checkingBar);

        checkingBar.setVisibility(ProgressBar.INVISIBLE);
        if (wifiError) {
            messageText.setText(getResources().getString(R.string.check_wifi));
            showWifiError(true);
        } else {
            messageText.setText(getResources().getString(R.string.server_not_accessible));
            showServerError(true);
        }
    }

    protected void goToStart() {
        Intent intent = new Intent().setClass(NetworkNotificationActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NetworkNotificationActivity.this.startActivity(intent);
        NetworkNotificationActivity.this.finish();
    }

    public void onCheckWifiButtonClick(View view) {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void onCheckServerButtonClick(View view) {
        showCheckingProgress(true);

        this.checkServer();
    }

    protected void checkServer() {
        messageText.setText(getResources().getString(R.string.checking_server));
        showCheckingProgress(true);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            public Boolean doInBackground(Void... param) {
                return RequestUtils.hasActiveInternetConnection();
            }

            @Override
            public void onPostExecute(Boolean result) {
                if (result) {
                    RequestUtils.setServerAsOnline(true);
                    goToStart();
                } else {
                    RequestUtils.setServerAsOnline(false);
                    showCheckingProgress(false);
                    messageText.setText(getResources().getString(R.string.server_not_accessible));
                }
            }
        }.execute();
    }

    public void showWifiError(Boolean show) {
        if (show) {
            checkWifiButton.setVisibility(Button.VISIBLE);
            checkServerButton.setVisibility(Button.INVISIBLE);

        } else {
            checkWifiButton.setVisibility(Button.INVISIBLE);
            checkServerButton.setVisibility(Button.VISIBLE);
        }
    }

    public void showServerError(Boolean show) {
        if (show) {
            checkServerButton.setVisibility(Button.VISIBLE);
            checkWifiButton.setVisibility(Button.INVISIBLE);
        } else {
            checkServerButton.setVisibility(Button.INVISIBLE);
            checkWifiButton.setVisibility(Button.VISIBLE);
        }
    }

    public void showCheckingProgress(Boolean show) {
        if (show) {
            checkingBar.setVisibility(ProgressBar.VISIBLE);
            checkServerButton.setEnabled(false);
        } else {
            checkingBar.setVisibility(ProgressBar.INVISIBLE);
            checkServerButton.setVisibility(Button.VISIBLE);
            checkWifiButton.setVisibility(Button.INVISIBLE);
            checkServerButton.setEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Go to Start")
                    .setMessage("Touch YES to restart Fantasy Survivor...")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToStart();
                        }

                    })
                    .setNegativeButton("NO", null)
                    .show();

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

}
