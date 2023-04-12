package com.devlomi.fireapp.job;

import android.support.annotation.NonNull;

import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.concurrent.TimeUnit;

public class DeleteStatusJob extends Job {


    //schedule a job to delete the status after 24 hours
    public static void schedule(String userId, String statusId) {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(IntentUtils.UID, userId);
        bundle.putString(IntentUtils.EXTRA_STATUS_ID, statusId);
        new JobRequest.Builder(JobIds.JOB_TAG_DELETE_STATUS)
                .setExact(TimeUnit.HOURS.toMillis(24))
                .setExtras(bundle)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        String uid = params.getExtras().getString(IntentUtils.UID, "");
        String statusID = params.getExtras().getString(IntentUtils.EXTRA_STATUS_ID, "");
        RealmHelper.getInstance().deleteStatus(uid, statusID);
        return Result.SUCCESS;
    }


}
