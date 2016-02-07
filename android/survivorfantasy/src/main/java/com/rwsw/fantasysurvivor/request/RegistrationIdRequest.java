package com.rwsw.fantasysurvivor.request;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.octo.android.robospice.request.SpiceRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationIdRequest extends SpiceRequest<String> {

    private String registration_id;
    private String POST_REG_ID = "/gcm/registration";

    public RegistrationIdRequest(String registration_id) {
        super(String.class);
        this.registration_id = registration_id;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        String _url = FantasySurvivor.getServerAddress() + POST_REG_ID;

        // Create a new HttpClient and Post Header
        HttpClient httpclient = RequestUtils.getClient();
        HttpPost httppost = RequestUtils.getHttpPost(_url);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("gcm_reg_id", this.registration_id));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            int code = response.getStatusLine().getStatusCode();
            Boolean success = false;

            if (code == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                final String responseStr = EntityUtils.toString(resEntity);
                JSONObject responseData = new JSONObject(responseStr);

                success = responseData.getBoolean("success");
            }

            if (!success) {
                this.registration_id = "";
            }

            return this.registration_id;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this.registration_id;
    }

}


