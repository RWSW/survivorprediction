package com.rwsw.fantasysurvivor.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Overlordyorch on 26/03/14.
 */
public class KeepAliveService extends IntentService {

    private String POST_NEW_STATUS = "/user/status";
    private Integer connectionErrorLimit = 2;
    private Integer connectionErrorsCount = 0;


    public KeepAliveService() {
        super("KeepAliveService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (RequestUtils.isDeviceOnline() && RequestUtils.isServerOnline()) {
                    String _url = FantasySurvivor.getServerAddress() + POST_NEW_STATUS;

                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = RequestUtils.getServiceClient();
                    HttpPost httppost = RequestUtils.getHttpPost(_url);

                    try {
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        Integer code = response.getStatusLine().getStatusCode();

                        if (code != HttpStatus.SC_OK) {
                            if (connectionErrorsCount == connectionErrorLimit) {
                                RequestUtils.setServerAsOnline(false);
                            }
                            connectionErrorsCount++;
                        }
                        if (response != null) {
                            response.getEntity().consumeContent();
                        }
                        RequestUtils.setServerAsOnline(true);

                    } catch (ClientProtocolException e) {

                    } catch (IOException e) {

                    }
                }
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);

    }

    private class KeepAliveRequestListener implements RequestListener<Integer> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            String e = spiceException.getLocalizedMessage();
            spiceException.printStackTrace();
        }

        @Override
        public void onRequestSuccess(Integer status) {
            Log.v("KeepAliveService", "Keep Alive Success");
        }
    }
}