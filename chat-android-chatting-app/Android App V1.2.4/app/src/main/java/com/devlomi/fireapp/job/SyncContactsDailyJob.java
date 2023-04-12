package com.devlomi.fireapp.job;

import android.support.annotation.NonNull;

import com.devlomi.fireapp.utils.ContactUtils;
import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class SyncContactsDailyJob extends DailyJob {

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        ContactUtils.syncContacts(getContext(), null);
        return DailyJobResult.SUCCESS;
    }

    public static void schedule() {
        // schedule between 1 and 6 AM
        DailyJob.schedule(new JobRequest.Builder(JobIds.JOB_TAG_SYNC_CONTACTS), TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(6));
    }
}
