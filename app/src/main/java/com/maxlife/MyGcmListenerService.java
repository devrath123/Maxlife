/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maxlife;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.SplashActivity;
import com.maxlife.utils.Constants;
import com.maxlife.utils.PrefStore;

public class MyGcmListenerService extends GcmListenerService {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String TAG = "MyGcmListenerService";
    private int noti_id;


    @Override
    public void onMessageReceived(String from, Bundle data) {
        log("onMessage :" + data);
        PrefStore prefStore = new PrefStore(this);
        boolean state = prefStore.getBoolean("state");
        if (!state) {
            generateNotification(this, data);
        } else {
            //generateNotification(this, data);
            displayMessage(data);
        }
    }

    public static void log(String string) {
        if (BuildConfig.DEBUG)
            Log.e("GCM", string);
    }

    private void displayMessage(Bundle data) {
        Intent displayMessage = new Intent(Constants.DISPLAY_MESSAGE_ACTION);
        displayMessage.putExtras(data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(displayMessage);
    }

    public void generateNotification(Context context, Bundle extra) {
        int nid = 0;
//        extra = intent.getExtras();
        String notificationMessage = extra.getString("message");
        PrefStore store = new PrefStore(this);

        String id = "";
        String to_user = "";
        if (extra.containsKey("id")) {
            id = extra.getString("id");
        }
        if (extra.containsKey("to_user")) {
            to_user = extra.getString("to_user");
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.notifications)
                .setContentTitle("MaxLife")
                .setContentText(notificationMessage).setAutoCancel(true)
                .setOngoing(true)
                .setAutoCancel(true);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(notificationMessage);
        mBuilder.setStyle(bigTextStyle);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(notificationSound);
        long[] vibrate = {0, 100, 200, 300};
        mBuilder.setVibrate(vibrate);

        PendingIntent resultPendingIntent = null;
        Intent resultIntent = new Intent(context, SplashActivity.class);
        resultIntent.putExtra("isPush", true);
        resultIntent.putExtra("id", id);
        resultIntent.putExtra("to_user", to_user);


//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);

//        resultPendingIntent = stackBuilder.getPendingIntent(
//                nid, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, nid, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(nid, mBuilder.build());
    }
}
