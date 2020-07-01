package com.example.firebasepushnotifications;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.firebasepushnotifications.Service.NewJobService;

public class Utils {

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void scheduleJob(Context context, int job_id) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(new JobInfo.Builder(job_id, new ComponentName(context, NewJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPersisted(true)
                .setImportantWhileForeground(true)
                .build());
    }
}
