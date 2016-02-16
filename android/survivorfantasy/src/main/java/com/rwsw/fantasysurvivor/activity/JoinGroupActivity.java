package com.rwsw.fantasysurvivor.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.request.GetGroupIdsRESTFulRequest;
import com.rwsw.fantasysurvivor.request.GetGroupRESTFulRequest;
import com.rwsw.fantasysurvivor.util.AccountUtils;
import com.rwsw.fantasysurvivor.util.RequestUtils;

import java.util.Timer;
import java.util.TimerTask;


public class JoinGroupActivity extends BaseSpiceActivity {

    private static final int ADDED_CODE = 1;
    private static final int ALREADY_EXISTS_CODE = 2;
    public static Boolean homeActive = false;
    private final String TAG = "HOME_ACTIVITY";
    private JoinGroupActivity mActivity = JoinGroupActivity.this;
    private ImageView imageProfile;
    private TextView textViewName, textViewEmail;
    private String username, email, userImageUrl;
    private ListView contactListView;
    private Timer getGroupsTimer = null;
    private GetGroupIdsRESTFulRequest getGroupIdsRequest;
    private GetGroupRequestListener listener;
    private TextView mainText;
    private EditText enterText;
    private Button joinGroup;
    private TextView groupnamelabel;
    private EditText groupnameinput;
    public static Integer ExistingGroup = 1;
    public static Integer NewGroup = 2;
    public Integer GroupType = 0;
    private Boolean stopGroupIds = false;
    private Boolean receivedGroupIds = false;
    private String groupIDvals = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        mainText = (TextView)findViewById(R.id.enterGroupText);
        enterText = (EditText)findViewById(R.id.groupIdInput);
        joinGroup = (Button)findViewById(R.id.joingroup);
        groupnamelabel = (TextView)findViewById(R.id.groupnamelabel);
        groupnameinput = (EditText)findViewById(R.id.groupnameinput);

        Intent intent = getIntent();
        GroupType = intent.getIntExtra("newvsexisting", 0);
//        new GetIdsTask().execute();
        if (GroupType == ExistingGroup) {
            //Existing
            mainText.setText(R.string.join_group_header);
            joinGroup.setText(R.string.join_group);
            groupnamelabel.setVisibility(View.GONE);
            groupnameinput.setVisibility(View.GONE);
        } else if (GroupType == NewGroup) {
            //New
            mainText.setText(R.string.create_group_header);
            joinGroup.setText(R.string.create_group);
            groupnamelabel.setVisibility(View.VISIBLE);
            groupnameinput.setVisibility(View.VISIBLE);
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
        stopGroupIds = false;
        startpolling();
    }

    @Override
    public void onResume() {
        super.onResume();
        stopGroupIds = false;
        startpolling();
    }

    public void startpolling() {
        final Handler handler = new Handler();
        getGroupsTimer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (stopGroupIds != true) {
                            getGroupIds();
                        }
                    }
                });
            }
        };
        getGroupsTimer.scheduleAtFixedRate(doAsynchronousTask, 0, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopGroupIds = true;
        getGroupsTimer.cancel();
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
        stopGroupIds = true;
        getGroupsTimer.cancel();
        FantasySurvivor.unregisterNetworkReceiver();
        homeActive = false;
    }

    private void getGroupIds() {
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            getGroupIdsRequest = new GetGroupIdsRESTFulRequest();
            listener = new GetGroupRequestListener();
            FantasySurvivor.getSpiceManager().execute(getGroupIdsRequest, listener);
        }
    }

    public void displayAlert(int whichalert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String messageStr = "";
        builder.setCancelable(false);

        if (whichalert == 1) {
            //Need to enter data first.
            builder.setTitle("Must Enter Info");
            messageStr = "Please enter group info.";
        } else if (whichalert == 2) {
            //Trying to join non-existant group.
            builder.setTitle("Group ID Not Found");
            messageStr = "Sorry. This group id was not found. Please verify you have entered the correct id and try again.";
        } else if (whichalert == 3) {
            //Group id already used.
            builder.setTitle("Group ID Already Taken");
            messageStr = "Sorry. This group id is already taken. Please try another id.";
        }
        builder.setMessage(messageStr);
        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        joinGroup.setEnabled(true);
                    }
                });

        builder.show();
    }

    public void joinExistingGroup(String groupid) {
        enterText.setText("");
        joinGroup.setEnabled(true);
        Intent intent = new Intent(this, GroupSummaryActivity.class);
        intent.putExtra("newvsexisting", 1);
        intent.putExtra("groupname", "");
        intent.putExtra("groupid", groupid);
        stopGroupIds = true;
        getGroupsTimer.cancel();
        startActivity(intent);
    }

    public void createNewGroup(String groupname, String groupid) {
        enterText.setText("");
        groupnameinput.setText("");
        joinGroup.setEnabled(true);
        Intent intent = new Intent(this, GroupSummaryActivity.class);
        intent.putExtra("newvsexisting", 2);
        intent.putExtra("groupname", groupname);
        intent.putExtra("groupid", groupid);
        stopGroupIds = true;
        getGroupsTimer.cancel();
        startActivity(intent);
    }

    public void enterGroup(View view) {
        joinGroup.setEnabled(false);
        if (receivedGroupIds == false) {
            //TODO: need to wait while group ids are gathered!
            Toast.makeText(this, "Please wait.", Toast.LENGTH_SHORT).show();
            joinGroup.setEnabled(true);
        } else {
            if (GroupType == ExistingGroup) {
                if (enterText.getText().toString().equals("")) {
                    displayAlert(1);
                } else if (groupExists(enterText.getText().toString()) == 0) {
                    displayAlert(2);
                } else if (groupExists(enterText.getText().toString()) == 1) {
                    joinExistingGroup(enterText.getText().toString());
                }
            } else if (GroupType == NewGroup) {
                if (groupnameinput.getText().toString().equals("") || enterText.getText().toString().equals("")) {
                    displayAlert(1);
                } else if (groupExists(enterText.getText().toString()) == 1) {
                    displayAlert(3);
                } else if (groupExists(enterText.getText().toString()) == 0) {
                    createNewGroup(groupnameinput.getText().toString(), enterText.getText().toString());
                }
            }
        }
    }

    private int groupExists(String checkGroup) {
        int value = -1;
        String valString = ":" + checkGroup + ":";
        if (receivedGroupIds == true) {
            if (groupIDvals.contains(valString)) {
                value = 1;
            } else {
                value = 0;
            }
        }
        return value;
    }

    private class GetGroupRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
//            NewContactActivity.this.setProgressBarIndeterminateVisibility(false);
            String e = spiceException.getLocalizedMessage();
            spiceException.printStackTrace();
            Log.i("RYAN", "FAILURE");
            Toast.makeText(mActivity, "FAIL: " + e, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(String groupids) {
            receivedGroupIds = true;
            /**
             ADDED_CONTACT = 1;
             ALREADY_EXISTS_CONTACT = 2;
             ADD_ERROR = 0;
             */
            if (groupids == null) {
                groupIDvals = "";
                SpiceException e = new SpiceException("Retrived information is empty.");
                onRequestFailure(e);
                Log.i("RYAN", groupids);
                Toast.makeText(mActivity, "MEH....", Toast.LENGTH_LONG).show();
//                updateStatusIndicator(NOT_EXISTS_CONTACT);
                return;
            } else {
                Log.i("RYAN", groupids);
                groupIDvals = groupids;
                Toast.makeText(mActivity, "SUCCESS", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}