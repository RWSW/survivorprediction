/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rwsw.fantasysurvivor.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.rwsw.fantasysurvivor.R;
import com.rwsw.fantasysurvivor.activity.FantasySurvivor;
import com.rwsw.fantasysurvivor.activity.HomeActivity;
import com.rwsw.fantasysurvivor.adapter.DBAdapter;
import com.rwsw.fantasysurvivor.request.GetMessageRESTFulRequest;
import com.rwsw.fantasysurvivor.util.RequestUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final String TAG = "GCM Intent Service";
    NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;
    private String UPDATE_MESSAGES = "android.intent.action.UPDATE_MESSAGES";


    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification(extras);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(extras);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                Integer mType = Integer.parseInt(extras.getString("messagetype"));
                if (mType == 1) {
                    sendNotification(extras);
                } else {
                    //update status
                    Log.i(TAG, "Update status message");
                }
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle extra) {
        if (!RequestUtils.isDeviceOnline() || !RequestUtils.isServerOnline()) {
            // TODO: Store some variable with data to get message when internet is back
            return;
        }
        String messagePath = extra.getString("message");
        String contactID = extra.getString("contact_id").trim();
        SpiceManager manager = FantasySurvivor.getSpiceManager();
        manager.execute(new GetMessageRESTFulRequest(messagePath, contactID),
                new GetMessageRequestListener(
                        extra.getString("message_id").trim(),
                        contactID,
                        extra.getString("date").trim())
        );
    }

    private void addMessageToDB(String meesage_id, String contact_id, String message, String messageFilePath) {
        DBAdapter db = new DBAdapter(this);
//        db.insertIncomingMessage(contact_id, meesage_id, message, messageFilePath);
    }

    private class GetMessageRequestListener implements RequestListener<String> {
        private String msg_id = "";
        private String contact_id = "";
        private String date = "";
        public GetMessageRequestListener(String msg_id, String contact_id, String date) {
            super();
            this.msg_id = msg_id;
            this.contact_id = contact_id;
            this.date = date;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            String e = spiceException.getLocalizedMessage();
            spiceException.printStackTrace();
        }

        @Override
        public void onRequestSuccess(String jsonMessagePath) {
            mNotificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            Context context = getApplicationContext();

            // Read message from receive file.
            // Add this message to SQLITE
//            addMessageToDB(this.msg_id, this.contact_id, message, jsonMessagePath);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack
            stackBuilder.addParentStack(HomeActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(new Intent(context, HomeActivity.class));
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            // define sound URI, the sound to be played when there's a notification
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // contact id
            int notifyID = 1;
//            ChatAdapter chatAdapter = null;
//            chatAdapter = ChatManager.getChatAdater(contact_id, cursor);
//            chatAdapter.changeStatus(message, contact_id, Message.SEEN, new ProcessMessageRequestListener());

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setSound(soundUri)
                            .setContentTitle("New Message")
                            .setContentText("You've received new messages.")
                            .setAutoCancel(true);

            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(notifyID, mBuilder.build());
        }
    }
//    private class ProcessMessageRequestListener implements RequestListener<Message> {
//
//        @Override
//        public void onRequestFailure(SpiceException spiceException) {
//            String e = spiceException.getLocalizedMessage();
//            spiceException.printStackTrace();
//        }
//
//        @Override
//        public void onRequestSuccess(Message message) {
//            if ((message.getStatus() == Message.ERROR_IN_SEND)) {
//                SpiceException e = new SpiceException("Message didn't send.");
//                onRequestFailure(e);
//                return;
//            }
//            return;
//        }
//
//    }
}
