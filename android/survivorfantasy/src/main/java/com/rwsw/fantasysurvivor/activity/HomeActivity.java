package com.rwsw.fantasysurvivor.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.util.AccountUtils;

import java.util.Timer;


public class HomeActivity extends BaseSpiceActivity {

    private static final int ADDED_CODE = 1;
    private static final int ALREADY_EXISTS_CODE = 2;
    public static Boolean homeActive = false;
    private final String TAG = "HOME_ACTIVITY";
    private HomeActivity mActivity = HomeActivity.this;
    private ImageView imageProfile;
    private TextView textViewName, textViewEmail;
    private String username, email, userImageUrl;
    private ListView contactListView;
    private AsyncTask getImageTask = null;
    private Timer reloadContactTimer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Hide back button on action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        this.initializeUI();

        email = AccountUtils.getAccountName(this);

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email value: " + email + "User Data:  " + AccountUtils.getAccountInfo(this));
            this.finish();
        }
        FantasySurvivor.setUsername(email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final Handler handler = new Handler();
        reloadContactTimer = new Timer();
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        homeActive = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        homeActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (reloadContactTimer != null) {
            //reloadContactTimer.cancel();
        }
        if (getImageTask != null) {
            getImageTask.cancel(true);
        }
        FantasySurvivor.unregisterNetworkReceiver();
        homeActive = false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (reloadContactTimer != null) {
            //reloadContactTimer.cancel();
        }
        if (getImageTask != null) {
            getImageTask.cancel(true);
        }
        FantasySurvivor.unregisterNetworkReceiver();
        homeActive = false;
    }

    private void initializeUI() {
        //imageProfile = (ImageView) findViewById(R.id.profilePicture);
        //textViewName = (TextView) findViewById(R.id.usernameLabel);
        //textViewEmail = (TextView) findViewById(R.id.emailLabel);
    }
}