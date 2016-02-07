package com.rwsw.fantasysurvivor.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.rwsw.fantasysurvivor.activity.NetworkNotificationActivity;
import com.rwsw.fantasysurvivor.util.RequestUtils;

/**
 * Created by Overlordyorch on 03/04/2014.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = "Network Change Receiver";

    private final int DEVICE_ERROR = -1;
    private final int SERVER_ERROR = -2;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null && wifi.isAvailable())
                || (mobile != null && mobile.isAvailable())) {

            RequestUtils.setDeviceAsOnline(true);
            Log.d(TAG, "Wifi:" + wifi.isAvailable() + " Mobile:" + mobile.isAvailable());

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                public Boolean doInBackground(Void... param) {
                    return RequestUtils.hasActiveInternetConnection();
                }

                @Override
                public void onPostExecute(Boolean result) {
                    if (result) {
                        Log.d(TAG, "Connection Active");
                        RequestUtils.setServerAsOnline(true);
                    } else {
                        Log.e(TAG, "Connection Inactive");
                        RequestUtils.setServerAsOnline(false);
                        showAlert(context, SERVER_ERROR);
                    }
                }
            }.execute();
        } else {
            if (!RequestUtils.isDeviceOnline()) return;
            RequestUtils.setDeviceAsOnline(false);
            RequestUtils.setServerAsOnline(false);
            showAlert(context, DEVICE_ERROR);
        }
    }

    protected void showAlert(Context context, int errorCode) {
        Intent i = new Intent(context, NetworkNotificationActivity.class);
        i.putExtra("errorCode", errorCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
