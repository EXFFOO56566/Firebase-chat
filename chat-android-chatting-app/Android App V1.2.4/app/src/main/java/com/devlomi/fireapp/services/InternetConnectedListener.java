package com.devlomi.fireapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.devlomi.fireapp.model.constants.PendingGroupTypes;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.PendingGroupJob;
import com.devlomi.fireapp.model.realms.UnUpdatedStat;
import com.devlomi.fireapp.model.realms.UnUpdatedVoiceMessageStat;
import com.devlomi.fireapp.utils.FireConstants;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import io.realm.RealmResults;


//this class will indicates when there is Internet connection
public class InternetConnectedListener extends Service {

    DatabaseReference connectedRef;
    DatabaseReference presenceRef;

    public InternetConnectedListener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();


        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        presenceRef = FireConstants.presenceRef.child(FireManager.getUid());
    }

    //send pending messages that are not sent while there is no internet connection
    private void sendPendingMessages() {
        if (RealmHelper.getInstance().getPendingMessages().isEmpty())
            return;

        for (final Message message : RealmHelper.getInstance().getPendingMessages()) {
            ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
        }
    }

    //update messages states (received,read) while there is no internet connection
    private void updateMessagesStats() {
        RealmResults<UnUpdatedStat> unUpdateMessageStat = RealmHelper.getInstance().getUnUpdateMessageStat();
        for (final UnUpdatedStat unUpdatedStat : unUpdateMessageStat) {
            ServiceHelper.startUpdateMessageStatRequest(this, unUpdatedStat.getMessageId(), unUpdatedStat.getMyUid(),null, unUpdatedStat.getStatToBeUpdated());
        }
    }

    //update voice messages states when voice message is listened while there is no internet connection
    private void updateVoiceMessagesStats() {
        RealmResults<UnUpdatedVoiceMessageStat> unUpdatedVoiceMessageStat = RealmHelper.getInstance().getUnUpdatedVoiceMessageStat();
        for (final UnUpdatedVoiceMessageStat unUpdatedStat : unUpdatedVoiceMessageStat) {
            ServiceHelper.startUpdateVoiceMessageStatRequest(this, unUpdatedStat.getMessageId(), unUpdatedStat.getMyUid(), null);
        }
    }

    private void processPendingGroupEvents() {
        for (PendingGroupJob pendingGroupJob : RealmHelper.getInstance().getPendingGroupCreationJobs()) {
            String groupId = pendingGroupJob.getGroupId();
            if (pendingGroupJob.getType() == PendingGroupTypes.CHANGE_EVENT) {
                GroupEvent groupEvent = pendingGroupJob.getGroupEvent();
                GroupManager.updateGroup(this,groupId,groupEvent,null);
            } else {
                GroupManager.fetchAndCreateGroup(this, groupId,false, null);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //start service again
        startService(new Intent(this, InternetConnectedListener.class));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    sendPendingMessages();
                    updateMessagesStats();
                    updateVoiceMessagesStats();
                    processPendingGroupEvents();
                    //set online status if the App is in Foreground
                    if (MyApp.isBaseActivityVisible()) {
                        FireManager.setOnlineStatus();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        //set last seen when user disconnects from internet
        presenceRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
