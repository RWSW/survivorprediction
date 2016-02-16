package com.rwsw.fantasysurvivor.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.fragment.ContestantsFragment;
import com.rwsw.fantasysurvivor.fragment.PicksFragment;
import com.rwsw.fantasysurvivor.fragment.SummaryFragment;
import com.rwsw.fantasysurvivor.request.EnterGroupRESTFulRequest;
import com.rwsw.fantasysurvivor.request.GetContestantsRESTFulRequest;
import com.rwsw.fantasysurvivor.request.GetGroupIdsRESTFulRequest;
import com.rwsw.fantasysurvivor.request.GetGroupUsersRESTFulRequest;
import com.rwsw.fantasysurvivor.request.LoadUsersDataRESTFulRequest;
import com.rwsw.fantasysurvivor.util.RequestUtils;

import java.util.Timer;
import java.util.TimerTask;


public class GroupSummaryActivity extends FragmentSpiceActivity {

    static private int ITEMS = 3;
    MyAdapter mAdapter;
    ViewPager mPager;
    private final String TAG = "GROUPSUMMARYACTIVITY";
    private GroupSummaryActivity mActivity = GroupSummaryActivity.this;
    private ImageView imageProfile;
    private TextView textViewName, textViewEmail;
    private String username, email, userImageUrl;
    private ListView contactListView;
    private Timer joinGroupTimer = null;
    private Timer getDataTimer = null;
    private Timer getContestantsTimer = null;
    private EnterGroupRESTFulRequest enterGroupRequest;
    private GetGroupUsersRESTFulRequest getGroupUsersRequest;
    private LoadUsersDataRESTFulRequest loadUsersDataRequest;
    private GetContestantsRESTFulRequest getContestantsRequest;
    private EnterGroupRequestListener enterListener;
    private GetContestantsRequestListener contestantsListener;
    private LoadUsersDataRequestListener loadDataListener;
    private TextView mainText;
    private EditText enterText;
    private Button joinGroup;
    private TextView groupnamelabel;
    private EditText groupnameinput;
    public static Integer ExistingGroup = 1;
    public static Integer NewGroup = 2;
    public Integer GroupType = 0;
    public String GroupName = null;
    public String GroupID = null;
    private Boolean enteredGroup = false;
    private Boolean receivedData = false;
    private Boolean receivedContestants = false;
    private String groupIDvals = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_summary);
        setContentView(R.layout.fragment_pager);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        // Add 3 tabs, specifying the tab's text and TabListener
        actionBar.addTab(
                actionBar.newTab()
                    .setText("Current Week Picks")
                        .setTabListener(tabListener));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Group Summary")
                        .setTabListener(tabListener));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Contestants")
                        .setTabListener(tabListener));

        mPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

//        mainText = (TextView)findViewById(R.id.enterGroupText);
//        enterText = (EditText)findViewById(R.id.groupIdInput);
//        joinGroup = (Button)findViewById(R.id.joingroup);
//        groupnamelabel = (TextView)findViewById(R.id.groupnamelabel);
//        groupnameinput = (EditText)findViewById(R.id.groupnameinput);

        Intent intent = getIntent();
        GroupType = intent.getIntExtra("newvsexisting", 0);
        if (GroupType == 0) {
            enteredGroup = true;
        } else {
            GroupName = intent.getStringExtra("groupname");
            GroupID = intent.getStringExtra("groupid");
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
        startGroupPolling();
        startDataPolling();
        mPager.setCurrentItem(1);
//        startContestantPolling();
    }

    @Override
    public void onResume() {
        super.onResume();
        startGroupPolling();
        startDataPolling();
//        startContestantPolling();
    }

    public void startDataPolling() {
        final Handler handler = new Handler();
        getDataTimer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (enteredGroup == true && receivedData != true) {
                            pollData();
                        }
                    }
                });
            }
        };
        getDataTimer.scheduleAtFixedRate(doAsynchronousTask, 0, 3000);
    }

    public void startGroupPolling() {
        final Handler handler = new Handler();
        joinGroupTimer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (enteredGroup != true && GroupType != 0) {
                            pollGroup();
                        }
                    }
                });
            }
        };
        joinGroupTimer.scheduleAtFixedRate(doAsynchronousTask, 0, 3000);
    }

    public void startContestantPolling() {
        final Handler handler = new Handler();
        getContestantsTimer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (receivedData == true && receivedContestants != true) {
                            pollContestants();
                        }
                    }
                });
            }
        };
        getContestantsTimer.scheduleAtFixedRate(doAsynchronousTask, 0, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        joinGroupTimer.cancel();
        getDataTimer.cancel();
//        getContestantsTimer.cancel();
        FantasySurvivor.unregisterNetworkReceiver();
    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    @Override
    public void onStop() {
        super.onStop();
        joinGroupTimer.cancel();
        getDataTimer.cancel();
//        getContestantsTimer.cancel();
        FantasySurvivor.unregisterNetworkReceiver();
    }

    public void getData(View view) {
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            loadUsersDataRequest = new LoadUsersDataRESTFulRequest();
            loadDataListener = new LoadUsersDataRequestListener();
            FantasySurvivor.getSpiceManager().execute(loadUsersDataRequest, loadDataListener);
        }
    }

    private void pollData() {
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            loadUsersDataRequest = new LoadUsersDataRESTFulRequest();
            loadDataListener = new LoadUsersDataRequestListener();
            FantasySurvivor.getSpiceManager().execute(loadUsersDataRequest, loadDataListener);
        }
    }

    private void pollGroup() {
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            enterGroupRequest = new EnterGroupRESTFulRequest(GroupType, GroupID, GroupName);
            enterListener = new EnterGroupRequestListener();
            FantasySurvivor.getSpiceManager().execute(enterGroupRequest, enterListener);
        }
    }

    private void pollContestants() {
        if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
            getContestantsRequest = new GetContestantsRESTFulRequest();
            contestantsListener = new GetContestantsRequestListener();
            FantasySurvivor.getSpiceManager().execute(getContestantsRequest, contestantsListener);
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

    private class EnterGroupRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
//            NewContactActivity.this.setProgressBarIndeterminateVisibility(false);
            String e = spiceException.getLocalizedMessage();
            spiceException.printStackTrace();
            Log.i("RYAN", "FAILURE");
            Toast.makeText(mActivity, "FAIL: " + e, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(String response) {
            if (response.equals("success")) {
                enteredGroup = true;
                Log.i("RYAN", response);
                Toast.makeText(mActivity, "SUCCESS", Toast.LENGTH_SHORT).show();
                return;
            } else {
                SpiceException e = new SpiceException("Retrived information is empty.");
                onRequestFailure(e);
                Log.i("RYAN", response);
                Toast.makeText(mActivity, "MEH....", Toast.LENGTH_SHORT).show();
//                updateStatusIndicator(NOT_EXISTS_CONTACT);
                return;
            }
        }
    }

    private class GetContestantsRequestListener implements RequestListener<String> {

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
            receivedContestants = true;
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

    private class LoadUsersDataRequestListener implements RequestListener<String> {

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
            receivedData = true;
            /**
             ADDED_CONTACT = 1;
             ALREADY_EXISTS_CONTACT = 2;
             ADD_ERROR = 0;
             */
            if (groupids == null) {
                SpiceException e = new SpiceException("Retrived information is empty.");
                onRequestFailure(e);
                Log.i("RYAN", groupids);
                Toast.makeText(mActivity, "Empty Users....", Toast.LENGTH_LONG).show();
//                updateStatusIndicator(NOT_EXISTS_CONTACT);
                return;
            } else {
                Log.i("RYAN", groupids);
                Toast.makeText(mActivity, "Retrieved Users!", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show image
                    return PicksFragment.init(position);
                case 1: // Fragment # 1 - This will show image
                    return SummaryFragment.init(position);
                case 2:
                    return ContestantsFragment.init(position);
                default:// Fragment # 2-9 - Will show list
                    return SummaryFragment.init(position);
            }
        }
    }
}