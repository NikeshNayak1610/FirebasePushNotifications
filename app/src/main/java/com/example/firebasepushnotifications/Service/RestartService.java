package com.example.firebasepushnotifications.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.firebasepushnotifications.Service.Observable;

public class RestartService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent innerIntent = new Intent(this, Observable.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, innerIntent);
        } else {
            startService(innerIntent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        return START_STICKY;
    }
}
