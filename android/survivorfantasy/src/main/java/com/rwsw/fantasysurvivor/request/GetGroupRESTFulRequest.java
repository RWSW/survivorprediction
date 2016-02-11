package com.rwsw.fantasysurvivor.request;

import android.net.Uri;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.activity.HomeActivity;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.rwsw.fantasysurvivor.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetGroupRESTFulRequest extends SpringAndroidSpiceRequest<String> {

    private String contactID = "";
    private String GET_MESSAGE = "/user/group";

    public GetGroupRESTFulRequest() {
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
        JSONObject contacts = responseData.getJSONObject("group_id");

        return contacts.getString("user_group_id");
    }
}


