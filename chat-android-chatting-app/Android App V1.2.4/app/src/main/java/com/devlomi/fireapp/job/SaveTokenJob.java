package com.devlomi.fireapp.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.devlomi.fireapp.utils.FCMTokenSaver;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.JobSchedulerSingleton;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SaveTokenJob extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        String token = jobParameters.getExtras().getString(IntentUtils.FCM_TOKEN);
        saveToken(jobParameters, token);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return true;
    }

    public static void schedule(Context context, String token) {
        ComponentName component = new ComponentName(context, SaveTokenJob.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(IntentUtils.FCM_TOKEN, token);
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.JOB_ID_SAVE_TOKEN, component)
                .setMinimumLatency(1)
                .setOverrideDeadline(1)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle);


        JobSchedulerSingleton.getInstance().schedule(builder.build());
    }

    private void saveToken(final JobParameters jobParameters, String token) {
        if (SharedPreferencesManager.isTokenSaved())
            jobFinished(jobParameters, false);
        else
            new FCMTokenSaver(new FCMTokenSaver.OnComplete() {
                @Override
                public void onComplete(boolean isSuccess) {
                    jobFinished(jobParameters, !isSuccess);
                }
            }).saveTokenToFirebase(token);
    }
}
