package com.rwsw.fantasysurvivor.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class RequestUtils {

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static final String DEVICE_ONLINE = "device_online";
    private static final String ONLINE_SERVER_KEY = "server_online";
    private static HttpClient serviceClient = null;
    private static String userCookie = "";

    public static HttpGet getHttpGet(String url) {
        return RequestUtils.getHttpGet(url, true);
    }

    public static HttpGet getHttpGet(String url, Boolean userCookie) {
        HttpGet httpGet = new HttpGet(url);

        if (!userCookie) return httpGet;

        httpGet.addHeader("Cookie", RequestUtils.getUserCookie());
        return httpGet;
    }

    public static HttpPost getHttpPost(String url) {

        return RequestUtils.getHttpPost(url, true);
    }

    public static HttpPost getHttpPost(String url, Boolean userCookie) {
        HttpPost httpPost = new HttpPost(url);
        if (!userCookie) return httpPost;

        httpPost.setHeader("Cookie", RequestUtils.getUserCookie());
        return httpPost;
    }

    public static HttpClient getClient() {
        return RequestUtils.createHttpClient();
    }

    public static HttpClient getServiceClient() {
        if (RequestUtils.serviceClient == null) {
            RequestUtils.serviceClient = new DefaultHttpClient();
        }

        return RequestUtils.serviceClient;
    }

    public static void createUserCookie(String cookie) {
        RequestUtils.userCookie = cookie;
        AccountUtils.setSecurityHeader(FantasySurvivor.getContext(), cookie);
    }

    public static String getUserCookie() {
        if (RequestUtils.userCookie.isEmpty()) {
            RequestUtils.userCookie = AccountUtils.getSecurityHeader(FantasySurvivor.getContext());
        }
        return RequestUtils.userCookie;
    }

    protected static HttpClient createHttpClient() {

        HttpClient client = new DefaultHttpClient();

        ((DefaultHttpClient) client).addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept gzip content
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        ((DefaultHttpClient) client).addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with gzip
                final HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });

        return client;
    }

    public static boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) FantasySurvivor.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        Log.e("Network Testing", "***Unavailable***");
        return false;
    }

    public static boolean hasActiveInternetConnection() {
        try {
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 1500);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet get = new HttpGet(FantasySurvivor.getServerAddress() + "/");
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            Boolean status = (code == HttpStatus.SC_OK);
            response.getEntity().consumeContent();
            return status;
        } catch (IOException e) {
            Log.e("Internet Testing", "Error checking internet connection", e);
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setServerAsOnline(Boolean serverStatus) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(FantasySurvivor.getContext()).edit();
        editor.putBoolean(ONLINE_SERVER_KEY, serverStatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setDeviceAsOnline(Boolean deviceStatus) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(FantasySurvivor.getContext()).edit();
        editor.putBoolean(DEVICE_ONLINE, deviceStatus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static Boolean isServerOnline() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FantasySurvivor.getContext());
        return prefs.getBoolean(ONLINE_SERVER_KEY, false);
    }

    public static Boolean isDeviceOnline() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FantasySurvivor.getContext());
        return prefs.getBoolean(DEVICE_ONLINE, false);
    }

    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }


}
