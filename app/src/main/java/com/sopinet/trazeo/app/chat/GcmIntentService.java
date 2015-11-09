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

package com.sopinet.trazeo.app.chat;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sopinet.trazeo.app.ChatActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.chat.model.Message;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static int notificationId = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    // Message types
    public static final String TEXT = "text";
    public static final String RIDE_STARTED = "ride.start";
    public static final String RIDE_FINISHED = "ride.finish";
    public static final String CHILD_IN = "child.in";
    public static final String CHILD_OUT = "child.out";

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
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    sendNotification("Send error: " + extras.toString(), null, null, null, null);
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Deleted messages on server: " + extras.toString(), null, null, null, null);
                    // If it's a regular GCM message, do some work.
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    // Post notification of received message.
                    String chatMsg = extras.getString("text");
                    String groupId = extras.getString("groupId");
                    String time = extras.getString("time");
                    String username = extras.getString("username");
                    String type = extras.getString("type");

                    try {
                        sendNotification(groupId, time, type, chatMsg, username);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void launchNotification(Message message, Group group, String type, boolean showMessage) {

        Intent chatIntent = new Intent("com.sopinet.trazeo.app.chatmessage");
        chatIntent.putExtra("groupId", group.id);
        chatIntent.putExtra("username", message.username);
        chatIntent.putExtra("time", message.time);
        chatIntent.putExtra("longId", message.getId());

        sendBroadcast(chatIntent);

        // If chat is not on foreground (visible), launch notifications
        if (group.chat != null && group.chat.visibility == 0) {

            Intent i = new Intent(this, ChatActivity_.class);
            i.putExtra("groupId", group.id);
            i.putExtra("fromNotification", true);

            String msg;
            switch (message.type) {
                case Message.RECEIVED:
                    msg = "Nuevo mensaje en: " + group.name;
                    break;
                default:
                    msg = message.text;
                    break;
            }

            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    i, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(type.equals(TEXT) ? R.drawable.mascota_arrow : R.drawable.mascota_arrow2)
                            .setContentTitle(getString(R.string.app_name))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setLights(0xFF0000FF, 100, 3000)
                            .setContentText(msg)
                            .setAutoCancel(true);

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            //String notifications = sharedPreferences.getString("status_notifications", "on");
            String sounds = sharedPreferences.getString("sounds", "on");

            if (sounds.equals("on")) {
                mBuilder.setSound(alarmSound);
            }

            mBuilder.setContentIntent(contentIntent);

            switch (type) {
                case TEXT:
                    if (sharedPreferences.getBoolean("chatsNotification", true))
                        mNotificationManager.notify(notificationId, mBuilder.build());
                    break;
                case RIDE_STARTED:
                case RIDE_FINISHED:
                    if (sharedPreferences.getBoolean("RaidInOutNotification", true) && showMessage)
                        mNotificationManager.notify(notificationId, mBuilder.build());
                    break;
                case CHILD_IN:
                case CHILD_OUT:
                    if (sharedPreferences.getBoolean("childInOutNotification", true))
                        mNotificationManager.notify(notificationId, mBuilder.build());
                    break;
            }
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    public void sendNotification(String groupId, String time, String type, String text, String username) {

        Group group = Group.getGroupById(groupId);
        boolean showMessage = true;
        if (group != null) {
            Message message = null;

            switch (type) {
                case TEXT:
                    message = new Message(username, text, time, Message.RECEIVED);
                    message.chat = group.chat;
                    message.save();
                    break;
                case RIDE_STARTED:
                    String memberName = "";
                    try {
                        String[] splitted = text.split(";");
                        memberName = splitted[0];
                        if (!group.rideCreator.equals("owner"))
                            group.ride_id = splitted[1];
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    message = new Message(username, String.format(getString(R.string.gcm_ride_started), memberName, group.name),
                            time, Message.NOTIFICATION);
                    message.chat = group.chat;
                    message.save();
                    group.hasride = "true";
                    break;
                case RIDE_FINISHED:
                    message = new Message(username, String.format(getString(R.string.gcm_ride_finished), group.name),
                            time, Message.NOTIFICATION);
                    message.chat = group.chat;
                    message.save();
                    group.hasride = "false";
                    showMessage = text.equals("showMessage");
                    break;
                case CHILD_IN:
                    message = new Message(username, String.format(getString(R.string.gcm_child_in), text, group.name),
                           time, Message.NOTIFICATION);
                    message.chat = group.chat;
                    message.save();
                    break;
                case CHILD_OUT:
                    message = new Message(username, String.format(getString(R.string.gcm_child_out), text, group.name),
                            time, Message.NOTIFICATION);
                    message.chat = group.chat;
                    message.save();
                    break;
            }

            group.hasMessage = 1;
            group.save();

            if (message != null) {
                launchNotification(message, group, type, showMessage);
            }
        }
    }


}
