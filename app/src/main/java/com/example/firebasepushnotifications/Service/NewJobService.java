package com.example.firebasepushnotifications.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.firebasepushnotifications.R;
import com.example.firebasepushnotifications.Utils;

public class NewJobService extends JobService {

    int job_id = 3001;
    private String NOTIFICATION_CHANNEL_ID = "2001";
    int count = 0;
    private String TAG = "Observable";
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    public static int GRAY_SERVICE_ID = 1001;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            count = count + 1;
            Log.e(TAG, "onStartCommand called: " + count);
            handler.postDelayed(this, 1000);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public boolean onStartJob(JobParameters params) {

        sharedPreferences = getSharedPreferences("job_service_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("is_job_running", true);
        editor.commit();

        handlerThread.start();
        Log.d(TAG, "Handler Thread Started");
        handler = new Handler(handlerThread.getLooper());

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID + "_name", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.createNotificationChannel(channel);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notif);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContent(remoteViews)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setSmallIcon(R.drawable.ic_blank)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);
        startForeground(GRAY_SERVICE_ID, builder.build());

        handler.postDelayed(runnable, 1000);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public boolean onStopJob(JobParameters params) {
        sharedPreferences = getSharedPreferences("job_service_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("is_job_running", false);
        editor.commit();
        handler.removeCallbacks(runnable);
        Utils.scheduleJob(this, job_id);
        return true;
    }
}
