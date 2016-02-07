package com.rwsw.fantasysurvivor.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.service.AbstractGetAuthTask;
import com.rwsw.fantasysurvivor.service.GetAuthInForeground;
import com.rwsw.fantasysurvivor.util.AccountUtils;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class LoginActivity extends Activity {
    public static final int GO_HOME_ACTIVITY = 2504;
    public static final int CLOSE_HOME_ACTIVITY = -1;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private String mEmail;
    private ProgressBar progressBar;
    private TextView splashTitle, errorText, loginAsLabel;
    private ImageButton gmailButton;
    private ImageView errorIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Splash screen view
        setContentView(R.layout.activity_login);

        this.initializeUI();
    }

    private void initializeUI() {
        loginAsLabel = (TextView) findViewById(R.id.loginAsLab);
        gmailButton = (ImageButton) findViewById(R.id.gmailButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        splashTitle = (TextView) findViewById(R.id.splashTitle);
        errorText = (TextView) findViewById(R.id.errorText);
        errorIcon = (ImageView) findViewById(R.id.errorImage);
        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserAccount();
            }
        });

        setVisibleLoginNetworks(true);

        if (this.checkUserAccount()) {
            this.getUsername();
        } else {
            this.setVisibleLoginNetworks(true);
        }
    }

    private void pickUserAccount() {
        errorText.setVisibility(TextView.INVISIBLE);
        errorIcon.setVisibility(ImageView.INVISIBLE);

        setVisibleLoginNetworks(false);
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        splashTitle = (TextView) findViewById(R.id.splashTitle);
        splashTitle.setText(FantasySurvivor.getContext().getResources().getString(R.string.splashLoginMsg));

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountUtils.setAccountName(this, mEmail);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
                setVisibleLoginNetworks(true);
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getUsername();
        }
    }

    private void getDebugUsername() {
        if (RequestUtils.isNetworkAvailable()) {
            getTask(LoginActivity.this, mEmail, SCOPE).execute();
        } else {
            showErrorMsg(getResources().getString(R.string.not_online));
        }
    }

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        splashTitle.setText(getResources().getString(R.string.authenticateMsg));
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (RequestUtils.isNetworkAvailable()) {
                getTask(LoginActivity.this, mEmail, SCOPE).execute();
            } else {
                showErrorMsg(getResources().getString(R.string.not_online));
            }
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            LoginActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    private AbstractGetAuthTask getTask(LoginActivity activity, String email,
                                        String scope) {
        String serverAddress = ((FantasySurvivor) this.getApplication()).getServerAddress();
        AbstractGetAuthTask task = new GetAuthInForeground(activity, email, scope, serverAddress);
        return task;
    }

    public void showErrorMsg(String msg) {
        setVisibleLoginNetworks(true);

        progressBar.setVisibility(ProgressBar.INVISIBLE);
        splashTitle.setVisibility(TextView.INVISIBLE);
        errorText.setVisibility(TextView.VISIBLE);
        errorText.setText(msg);
        errorIcon.setVisibility(ImageView.VISIBLE);
    }

    public void setVisibleLoginNetworks(Boolean value) {
        if (value) {
            loginAsLabel.setVisibility(TextView.VISIBLE);
            gmailButton.setVisibility(ImageButton.VISIBLE);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            splashTitle.setVisibility(TextView.INVISIBLE);
        } else {
            loginAsLabel.setVisibility(TextView.INVISIBLE);
            gmailButton.setVisibility(ImageButton.INVISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            splashTitle.setVisibility(TextView.VISIBLE);
        }
    }

    private boolean checkUserAccount() {
        splashTitle.setText("Checking last login...");
        this.setVisibleLoginNetworks(false);

        mEmail = AccountUtils.getAccountName(this);
        if (mEmail != null) {
            Account account = AccountUtils.getGoogleAccountByName(this, mEmail);
            if (account == null) {
                // Then the account has since been removed.
                AccountUtils.removeAccount(this);
                return false;
            }
            return true;
        }

        return false;
    }


}