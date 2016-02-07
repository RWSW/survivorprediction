package com.rwsw.fantasysurvivor.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthUtil;

public class AccountUtils {

    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_ACCOUNT_INFO = "account_info";
    private static final String KEY_GCM_ID = "gcm_reg_id";
    private static final String KEY_SEC_HEADER = "sec_header";

    private static String mCurrentUser = null;
    private static String mCurrentUserInfo = null;

    private static String mGCMId = null;

    private static String mSecHeader = null;

    public static Account getGoogleAccountByName(Context ctx, String accountName) {
        if (accountName != null) {
            AccountManager am = AccountManager.get(ctx);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            for (Account account : accounts) {
                if (accountName.equals(account.name)) {
                    return account;
                }
            }
        }
        return null;
    }

    public static String getAccountName(Context ctx) {
        if (mCurrentUser != null) {
            return mCurrentUser;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(KEY_ACCOUNT_NAME, null);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setAccountName(Context ctx, String accountName) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
        mCurrentUser = accountName;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setAccountInfo(Context ctx, String accountInfo) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString(KEY_ACCOUNT_INFO, accountInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }

        mCurrentUserInfo = accountInfo;
    }

    public static String getAccountInfo(Context ctx) {
        if (mCurrentUserInfo != null) {
            return mCurrentUserInfo;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(KEY_ACCOUNT_INFO, null);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void removeAccount(Context ctx) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.remove(KEY_ACCOUNT_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
        mCurrentUser = null;
    }

    public static String getGCMId(Context ctx) {
        if (mGCMId == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            mGCMId = prefs.getString(KEY_GCM_ID, "");
        }
        return mGCMId;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setGCMId(Context ctx, String gcm_id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();
        editor.putString(KEY_GCM_ID, gcm_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
        mGCMId = gcm_id;
    }

    public static String getSecurityHeader(Context ctx) {
        if (mSecHeader == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            mSecHeader = prefs.getString(KEY_SEC_HEADER, "");
        }
        return mSecHeader;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setSecurityHeader(Context ctx, String sec_header) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();
        editor.putString(KEY_SEC_HEADER, sec_header);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
        mSecHeader = sec_header;
    }
}
