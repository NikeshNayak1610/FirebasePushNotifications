package com.example.firebasepushnotifications.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.firebasepushnotifications.Activity.MessageActivity;
import com.example.firebasepushnotifications.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseService";
    private static final String NOTIFICATION_CHANNEL_ID = "5001";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        updateToken(s);
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(refreshToken);
            databaseReference.child(firebaseUser.getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!remoteMessage.getData().isEmpty()) {
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            Log.e(TAG, "User: " + user);
            Log.e(TAG, "Sent: " + sent);

            SharedPreferences sharedPreferences = getSharedPreferences("CurrentUserPrefs", MODE_PRIVATE);
            String currentUser = sharedPreferences.getString("currentUser", "none");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                if (sent.equals(firebaseUser.getUid())) {
                    if (!currentUser.equals(user)) {
                        Log.e(TAG, "Current User: " + user);
                        NotificationCompat.Builder builder = null;

                        Intent intent = new Intent(this, MessageActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid", user);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID + "_name", NotificationManager.IMPORTANCE_HIGH);
                            notificationManager.createNotificationChannel(channel);
                            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                                    .setContentTitle(remoteMessage.getData().get("title"))
                                    .setContentText(remoteMessage.getData().get("body"))
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setSound(defaultSound, AudioManager.STREAM_NOTIFICATION)
                                    .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE);

                        } else {
                            builder = new NotificationCompat.Builder(this)
                                    .setContentTitle(remoteMessage.getData().get("title"))
                                    .setContentText(remoteMessage.getData().get("body"))
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent)
                                    .setSound(defaultSound, AudioManager.STREAM_NOTIFICATION)
                                    .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE);

                        }

                        if (!remoteMessage.getData().get("icon").equals("default")) {
                            try {
                                Bitmap bmp = Picasso.get().load(remoteMessage.getData().get("icon")).get();
                                builder.setLargeIcon(bmp);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        notificationManager.notify(new Random().nextInt(), builder.build());
                    }
                }

            }
        }
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);

    }
}
