package com.example.firebasepushnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.firebasepushnotifications.Service.RestartService;

public class BootCompleteBroadcastReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("Check", "Boot Completed");
            Toast.makeText(context, "Boot Completed", Toast.LENGTH_LONG).show();

            sharedPreferences = context.getSharedPreferences("service_restart_preferences", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.putBoolean("is_restart", true);
            editor.commit();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent serviceintent = new Intent(context, RestartService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(context, serviceintent);
                        } else {
                            context.startService(serviceintent);
                        }
                    }
                }
            }).start();
        }
    }
}
