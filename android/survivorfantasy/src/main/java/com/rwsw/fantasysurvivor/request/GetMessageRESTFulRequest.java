package com.rwsw.fantasysurvivor.request;

import android.net.Uri;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.rwsw.fantasysurvivor.util.Util;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetMessageRESTFulRequest extends SpringAndroidSpiceRequest<String> {

    private String messageFile = null;
    private String contactID = "";
    private String GET_MESSAGE = "/message/read";

    public GetMessageRESTFulRequest(String messageFile, String contactID) {
        super(String.class);
        this.messageFile = messageFile;
        this.contactID = contactID;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        String _url = FantasySurvivor.getServerAddress() + GET_MESSAGE;
        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(_url).buildUpon();
        uriBuilder.appendQueryParameter("messagePath", this.messageFile);

        HttpGet httpGet = RequestUtils.getHttpGet(uriBuilder.toString());
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpGet);
        int code = response.getStatusLine().getStatusCode();
        String responseStr = "";

        if (code == HttpStatus.SC_OK) {
            HttpEntity resEntity = response.getEntity();
            try {
                File file = Util.getTempFile(FantasySurvivor.getContext(), this.contactID);
                FileOutputStream fileOS = new FileOutputStream(file);
                resEntity.writeTo(fileOS);
                resEntity.consumeContent();
                fileOS.flush();
                fileOS.close();
                responseStr = file.getAbsolutePath();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (response != null) {
            response.getEntity().consumeContent();
        }

        return responseStr;
    }

    public String createCacheKey(String email) {
        return "get_message_" + this.messageFile;
    }

}


