/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rwsw.fantasysurvivor.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.activity.HomeActivity;
import com.rwsw.fantasysurvivor.activity.LoginActivity;
import com.rwsw.fantasysurvivor.util.AccountUtils;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Display personalized greeting. This class contains boilerplate code to
 * consume the token but isn't integral to getting the tokens.
 */
public abstract class AbstractGetAuthTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "TokenInfoTask";
    private static String GOOGLE_USER_DATA = "No_data";
    private static String GOBBLER_AUTH_SERVICE = "/auth/login";
    private final int bad_request_limit = 4;
    protected LoginActivity mActivity;
    protected String mScope;
    protected String mServerAddress;
    protected String mEmail;
    protected String error = "";
    private String SENDER_ID = "543635920080";
    private GoogleCloudMessaging gcm;
    private String regid = "";
    private int bad_request_counter = 0;
    private Context context = null;

    AbstractGetAuthTask(LoginActivity activity, String email, String scope, String serverAddress) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mServerAddress = serverAddress;
        this.mEmail = email;
        this.context = activity.getApplicationContext();
    }

    /**
     * Reads the response from the input stream and returns it as a string.
     */
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    protected Boolean doInBackground(Void... params) {
        try {
            return fetchNameFromProfileServer();
        } catch (Exception e) {
            this.error = "Google Authentication Server error.";
            e.printStackTrace();
            onError(this.error, e);
        }
        return false;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
            if (this.error == "") this.error = "Exception: " + e.getMessage();
            Log.e(TAG, this.error, e);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(mActivity, String.format("Welcome: %s", this.mEmail), Toast.LENGTH_SHORT).show();
        } else {
            mActivity.showErrorMsg(this.error);
        }
    }

    /**
     * Get a authentication token if one is not available. If the error is not
     * recoverable then it displays the error message on parent activity.
     */
    protected abstract String fetchToken() throws IOException;

    /**
     * Contacts the user info server to get the profile of the user and extracts
     * the first name of the user from the profile. In order to authenticate
     * with the user info server the method first fetches an access token from
     * Google Play services.
     *
     * @return
     * @throws IOException    if communication with user info server failed.
     * @throws JSONException if the response from the server could not be parsed.
     */
    private Boolean fetchNameFromProfileServer() throws IOException, JSONException {
        String token = fetchToken();
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
            InputStream is = con.getInputStream();
            AccountUtils.setAccountInfo(context, readResponse(is));
            is.close();

            JSONObject profileData = new JSONObject(AccountUtils.getAccountInfo(context));

            if (!checkWithServer(profileData.getString("id"), token)) {
                GoogleAuthUtil.invalidateToken(mActivity, token);
                if (this.error == "") {
                    this.error = "Authentication Failed.";
                    Exception e = new Exception(this.error);
                    onError(this.error, e);
                }
                return false;
            }

            Intent intent = new Intent(mActivity, HomeActivity.class);
            intent.putExtra("email_id", mEmail);
            intent.putExtra("token", token);
            mActivity.startActivity(intent);
            mActivity.finish();
            return true;
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mActivity, token);
            this.error = "Authentication Failed. Server not respond.";
            Exception e = new Exception(this.error);
            onError(this.error, e);
            return false;
        } else {
            this.error = "Authentication Failed. Server returned the following error code: " + sc;
            Exception e = new Exception(this.error);
            onError(this.error, e);
            return false;
        }

    }

    private Boolean checkWithServer(String id, String token) throws IOException, JSONException {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }
        regid = gcm.register(SENDER_ID);
        FantasySurvivor.setmGCMId(regid);

        String _url = FantasySurvivor.getServerAddress() + GOBBLER_AUTH_SERVICE;
        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(_url).buildUpon();
        uriBuilder.appendQueryParameter("id", id);
        uriBuilder.appendQueryParameter("username", mEmail);
        uriBuilder.appendQueryParameter("token", token);
        uriBuilder.appendQueryParameter("gcm_reg_id", regid);

        try {
            HttpGet httpGet = new RequestUtils().getHttpGet(uriBuilder.toString(), false);
            HttpClient httpclient = RequestUtils.getClient();
            HttpResponse response = httpclient.execute(httpGet);

            HttpEntity resEntity = response.getEntity();
            final String responseStr = EntityUtils.toString(resEntity);
            JSONObject responseData = new JSONObject(responseStr);

            if (responseData.getBoolean("success")) {
                Header cookie = response.getFirstHeader("Set-Cookie");
                RequestUtils.createUserCookie(cookie.toString());
                return true;
            } else {
                this.error = responseData.getString("error");
                Exception e = new Exception(this.error);
                onError(this.error, e);
            }
        } catch (IOException e) {
            this.error = "Fail connecting to server. Please try again.";
            onError(this.error, e);
        }

        return false;
    }

}
