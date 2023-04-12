package com.devlomi.fireapp.job;

import android.support.annotation.NonNull;

import com.devlomi.fireapp.utils.FireConstants;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SetStatusSeenJob extends Job {


    public static void schedule(String userId, String statusId) {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(IntentUtils.UID, userId);
        bundle.putString(IntentUtils.EXTRA_STATUS_ID, statusId);
        new JobRequest.Builder(JobIds.JOB_TAG_SET_STATUS_SEEN)
                .setExecutionWindow(30_000L, 40_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setExtras(bundle)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        String uid = params.getExtras().getString(IntentUtils.UID, "");
        final String statusID = params.getExtras().getString(IntentUtils.EXTRA_STATUS_ID, "");
        FireConstants.statusSeenUidsRef.child(uid).child(statusID).child(FireManager.getUid()).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    RealmHelper.getInstance().setStatusSeenSent(statusID);
                }
            }
        });
        return Result.SUCCESS;
    }


}
