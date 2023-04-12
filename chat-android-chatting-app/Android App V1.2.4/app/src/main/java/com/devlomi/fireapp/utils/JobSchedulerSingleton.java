package com.devlomi.fireapp.utils;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class JobSchedulerSingleton {
    private static JobScheduler jobScheduler;

    private JobSchedulerSingleton() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static JobScheduler getInstance(){
        if (jobScheduler == null){
            jobScheduler = (JobScheduler) MyApp.context().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        return jobScheduler;
    }
}
