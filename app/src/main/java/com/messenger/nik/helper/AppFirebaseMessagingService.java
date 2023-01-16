package com.messenger.nik.helper;

import static com.messenger.nik.helper.Constants.SP;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.messenger.nik.R;
import com.messenger.nik.Splash;

import java.util.Random;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AppFirebaseMessagingS";

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.e(TAG, "New token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //If app is in background then show notification
//            if (!isAppIsInBackground(getApplicationContext())) {
                //App is in background so show notification
                Object msgTitle = remoteMessage.getData().get("title"); //get the title of notification as object
                Object msgBody = remoteMessage.getData().get("body"); //get the body of notification as object
                //If msgTitle and msgBody is not equals to null then show the notification
                if (msgTitle != null && msgBody != null) {
                    String title = String.valueOf(msgTitle);  //convert and store notification title in string
                    String body = String.valueOf(msgBody); //convert and store notification body in string
                    //Now show the notification
                    FCMNotification(getApplicationContext(), title, body);
                }
//            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void FCMNotification(Context context, String title, String messageBody) {
        Intent intent = new Intent(context, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelID = randomStringGenerator();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelID);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(messageBody);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setGroup("com.asm.heatic.NOTIFICATIONS_GROUP");
        notificationBuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancelAll();

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID,
                    "heatic_app_notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationCompat.Builder notificationBuilder1 =
                    new NotificationCompat.Builder(context, channelID);
            notificationBuilder1.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder1.setContentTitle(title);
            notificationBuilder1.setContentText(messageBody);
            notificationBuilder1.setAutoCancel(true);
            notificationBuilder1.setGroup("com.asm.heatic.NOTIFICATIONS_GROUP");
            notificationBuilder1.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

            notificationBuilder1.setContentIntent(pendingIntent);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private String randomStringGenerator() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(6);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
