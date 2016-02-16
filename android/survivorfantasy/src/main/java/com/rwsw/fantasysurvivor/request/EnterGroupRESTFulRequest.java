package com.rwsw.fantasysurvivor.request;

import android.net.Uri;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.RequestUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnterGroupRESTFulRequest extends SpringAndroidSpiceRequest<String> {

    private String contactID = "";
    private String GET_MESSAGE = "/user/group";
    private String joinType;
    private String groupID;
    private String groupName;

    public EnterGroupRESTFulRequest(Integer jointype, String groupid, String groupname) {
        super(String.class);
        joinType = jointype.toString();
        groupID = groupid;
        groupName = groupname;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        String _url = FantasySurvivor.getServerAddress() + GET_MESSAGE;
        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(_url).buildUpon();

        HttpPost httpPost = RequestUtils.getHttpPost(uriBuilder.toString());
        HttpClient httpclient = new DefaultHttpClient();
        JSONObject responseData = null;

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("join_type", this.joinType));
            nameValuePairs.add(new BasicNameValuePair("group_id", this.groupID));
            nameValuePairs.add(new BasicNameValuePair("group_name", this.groupName));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String responseStr = "";

            if (code == HttpStatus.SC_OK) {
                return "success";
            }

            if (response != null) {
                response.getEntity().consumeContent();
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return "fail";
    }
}


