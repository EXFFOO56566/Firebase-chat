package com.devlomi.fireapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.devlomi.fireapp.events.SyncContactsFinishedEvent;
import com.devlomi.fireapp.utils.ContactUtils;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;

public class SyncContactsService extends IntentService {


    //Required Constructor
    public SyncContactsService() {
        super("SyncContactsService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null && intent.getAction() != null && intent.getAction().equals(IntentUtils.INTENT_ACTION_SYNC_CONTACTS)) {
            ContactUtils.syncContacts(this, new ContactUtils.OnContactSyncFinished() {
                @Override
                public void onFinish() {
                    //update ui when sync is finished
                    EventBus.getDefault().post(new SyncContactsFinishedEvent());
                    //to prevent initial sync contacts when the app is launched for first time
                    SharedPreferencesManager.setContactSynced(true);
                    stopSelf();
                }
            });
        }
    }


}
