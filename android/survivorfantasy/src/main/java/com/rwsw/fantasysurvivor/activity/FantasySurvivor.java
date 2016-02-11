package com.rwsw.fantasysurvivor.activity;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.rwsw.fantasysurvivor.adapter.DBAdapter;
import com.rwsw.fantasysurvivor.service.NetworkChangeReceiver;
import com.rwsw.fantasysurvivor.util.AccountUtils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;

public class FantasySurvivor extends Application {

    private static final BroadcastReceiver networkBroadCast = new NetworkChangeReceiver();
    /* Read Only. */
    private static String serverAddress = "http://24.118.160.119:8888"; //TODO: This needs to be updated to server address!
    private static Context context;
    private static String mUsername;
    private static String mGCMId = "";
    private static SpiceManager spiceManager = null;
    private static DBAdapter dbAdapter = null;
    private static Boolean networkBroadCastRegistered = false;

    private static ServiceConnection contactManagerServiceCon = null;
    private static boolean contactManagerBound = false;

    public static Context getContext() {
        return com.rwsw.fantasysurvivor.activity.FantasySurvivor.context;
    }

    public static String getServerAddress() {
        return FantasySurvivor.serverAddress;
    }

    public static String getUsername() {
        return com.rwsw.fantasysurvivor.activity.FantasySurvivor.mUsername;
    }

    public static void setUsername(String username) {
        com.rwsw.fantasysurvivor.activity.FantasySurvivor.mUsername = username;
    }

    public static String getmGCMId() {
        return com.rwsw.fantasysurvivor.activity.FantasySurvivor.mGCMId;
    }

    public static void setmGCMId(String regId) {
        AccountUtils.setGCMId(com.rwsw.fantasysurvivor.activity.FantasySurvivor.getContext(), regId);
        com.rwsw.fantasysurvivor.activity.FantasySurvivor.mGCMId = regId;
    }

    public static SpiceManager getSpiceManager() {
        if(com.rwsw.fantasysurvivor.activity.FantasySurvivor.spiceManager == null){
            com.rwsw.fantasysurvivor.activity.FantasySurvivor.spiceManager = new SpiceManager(UncachedSpiceService.class);
        }
        if(!com.rwsw.fantasysurvivor.activity.FantasySurvivor.spiceManager.isStarted()){
            com.rwsw.fantasysurvivor.activity.FantasySurvivor.spiceManager.start(com.rwsw.fantasysurvivor.activity.FantasySurvivor.context);
        }
        return com.rwsw.fantasysurvivor.activity.FantasySurvivor.spiceManager;
    }

    public static DBAdapter getDbAdapter() {
        if (com.rwsw.fantasysurvivor.activity.FantasySurvivor.dbAdapter == null) {
            com.rwsw.fantasysurvivor.activity.FantasySurvivor.dbAdapter = new DBAdapter(com.rwsw.fantasysurvivor.activity.FantasySurvivor.context);
        }

        return com.rwsw.fantasysurvivor.activity.FantasySurvivor.dbAdapter;
    }

    public static void registerNetworkReceiver() {
        if (com.rwsw.fantasysurvivor.activity.FantasySurvivor.networkBroadCastRegistered) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        com.rwsw.fantasysurvivor.activity.FantasySurvivor.getContext().registerReceiver(com.rwsw.fantasysurvivor.activity.FantasySurvivor.networkBroadCast, filter);
        com.rwsw.fantasysurvivor.activity.FantasySurvivor.networkBroadCastRegistered = true;
    }

    public static void unregisterNetworkReceiver() {
        if (com.rwsw.fantasysurvivor.activity.FantasySurvivor.networkBroadCast != null && com.rwsw.fantasysurvivor.activity.FantasySurvivor.networkBroadCastRegistered) {
            //FantasySurvivor.getContext().unregisterReceiver(FantasySurvivor.networkBroadCast);
        }
    }

    public void onCreate() {
        super.onCreate();
        com.rwsw.fantasysurvivor.activity.FantasySurvivor.context = getApplicationContext();
        dbAdapter = new DBAdapter(com.rwsw.fantasysurvivor.activity.FantasySurvivor.context);
    }
}
