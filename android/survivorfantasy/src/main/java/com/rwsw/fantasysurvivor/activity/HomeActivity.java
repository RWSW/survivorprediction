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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.request.GetGroupRESTFulRequest;
import com.rwsw.fantasysurvivor.util.AccountUtils;
import com.rwsw.fantasysurvivor.util.RequestUtils;

import org.w3c.dom.Text;

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
    private GetGroupRESTFulRequest groupRequest;
    private TextView gettingGroupId;
    private ProgressBar spinner;
    private TextView enterText;
    private Button newGroup, existingGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Hide back button on action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        this.initializeUI();
        gettingGroupId = (TextView)findViewById(R.id.gettingGroupID);
        spinner = (ProgressBar)findViewById(R.id.groupProgressBar);
        enterText = (TextView)findViewById(R.id.introText);
        newGroup = (Button)findViewById(R.id.new_group);
        existingGroup = (Button)findViewById(R.id.existing_group);

        email = AccountUtils.getAccountName(this);

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email value: " + email + "User Data:  " + AccountUtils.getAccountInfo(this));
            this.finish();
        }
        FantasySurvivor.setUsername(email);
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            groupRequest = new GetGroupRESTFulRequest();
            GetGroupRequestListener listener = new GetGroupRequestListener();
            FantasySurvivor.getSpiceManager().execute(groupRequest, listener);
        }
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
        homeActive = true;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    public void joinNewGroup(View view) {
        Intent intent = new Intent(this, JoinGroupActivity.class);
        intent.putExtra("newvsexisting", 2);
        startActivity(intent);
    }

    public void joinExistingGroup(View view) {
        Intent intent = new Intent(this, JoinGroupActivity.class);
        intent.putExtra("newvsexisting", 1);
        startActivity(intent);
    }

    private class GetGroupRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
//            NewContactActivity.this.setProgressBarIndeterminateVisibility(false);
            String e = spiceException.getLocalizedMessage();
            spiceException.printStackTrace();
            Toast.makeText(mActivity, "FAIL: " + e, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(String group) {
            /**
             ADDED_CONTACT = 1;
             ALREADY_EXISTS_CONTACT = 2;
             ADD_ERROR = 0;
             */
            if (group == null) {
                SpiceException e = new SpiceException("Retrived information is empty.");
                onRequestFailure(e);
//                updateStatusIndicator(NOT_EXISTS_CONTACT);
                return;
            } else if (group.equals("0000")) {
//                Toast.makeText(mActivity, "SUCCESS", Toast.LENGTH_SHORT).show();
                gettingGroupId.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                enterText.setVisibility(View.VISIBLE);
                newGroup.setVisibility(View.VISIBLE);
                existingGroup.setVisibility(View.VISIBLE);
                return;
            } else {
                Toast.makeText(mActivity, "ALREADY IN GROUP:" + group, Toast.LENGTH_LONG).show();
                return;

//                setResult(1);
//                Intent data = new Intent();
//                data.putExtra("group_id", group);
//                finish();
            }
        }
    }
}