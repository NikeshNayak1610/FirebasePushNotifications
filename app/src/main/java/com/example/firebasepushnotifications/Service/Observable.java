package com.example.firebasepushnotifications.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.firebasepushnotifications.R;

public class Observable extends Service {

    private String NOTIFICATION_CHANNEL_ID = "2001";
    int count = 0;
    private String TAG = "Observable";
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    public static int GRAY_SERVICE_ID = 1001;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            count = count + 1;
            Log.e(TAG, "onStartCommand called: " + count);
            handler.postDelayed(this, 1000);
        }
    };
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("service_restart_preferences", Context.MODE_PRIVATE);
        handlerThread.start();
        Log.d(TAG, "Handler Thread Started");
        handler = new Handler(handlerThread.getLooper());

        boolean isRestart = sharedPreferences.getBoolean("is_restart", false);

        if (!isRestart) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID + "_name", NotificationManager.IMPORTANCE_HIGH);

                NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notifManager.createNotificationChannel(channel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("This is testing..")
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_person)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE);
                startForeground(GRAY_SERVICE_ID, builder.build());

            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("This is testing..")
                        .setOngoing(true)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                        .setSmallIcon(R.drawable.ic_person)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE);
                startForeground(GRAY_SERVICE_ID, builder.build());
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handler.postDelayed(runnable, 1000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sharedPreferences = getSharedPreferences("service_restart_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("is_restart", true);
        editor.commit();
        handler.removeCallbacks(runnable);
        startService(new Intent(this, RestartService.class));
        super.onDestroy();
        Log.e(TAG, "onDestroy Called");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        Log.e(TAG, "onTaskRemoved Called");
    }
}
