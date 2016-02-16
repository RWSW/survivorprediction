package com.rwsw.fantasysurvivor.request;

import android.net.Uri;
import android.util.Log;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.RequestUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetContestantsRESTFulRequest extends SpringAndroidSpiceRequest<String> {

    private String contactID = "";
    private String GET_MESSAGE = "/contestants/getcurrent";

    public GetContestantsRESTFulRequest() {
        super(String.class);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        String _url = FantasySurvivor.getServerAddress() + GET_MESSAGE;
        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(_url).buildUpon();

        HttpGet httpGet = RequestUtils.getHttpGet(uriBuilder.toString());
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpGet);
        int code = response.getStatusLine().getStatusCode();
        String responseStr = "";
        JSONObject responseData;

        if (code == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            responseStr = EntityUtils.toString(resEntity);
        }

        if (response != null) {
            response.getEntity().consumeContent();
        }
        responseData = new JSONObject(responseStr);
        JSONArray idarray = responseData.getJSONArray("groups");
        String idstrings = "";
        for (int i = 0; i < idarray.length(); i++) {
            JSONObject idobject = idarray.getJSONObject(i);
            idstrings = idstrings + ":";
            idstrings = idstrings + idobject.getString("group_uniqueid");
        }
        idstrings = idstrings + ":";

        Log.i("RYAN", idstrings);
        return idstrings;
    }
}


