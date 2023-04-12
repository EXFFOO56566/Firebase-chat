package com.devlomi.fireapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.devlomi.fireapp.events.FetchingUserGroupsAndBroadcastsFinished;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.utils.BroadcastManager;
import com.devlomi.fireapp.utils.DownloadManager;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Devlomi on 31/12/2017.
 */

//this is responsible for sending and receiving files/data from firebase using Download Manager Class
public class NetworkService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String chatId = intent.getStringExtra(IntentUtils.EXTRA_CHAT_ID);
            if (intent.getAction().equals(IntentUtils.INTENT_ACTION_UPDATE_GROUP)) {

                GroupEvent groupEvent = intent.getParcelableExtra(IntentUtils.EXTRA_GROUP_EVENT);
                String groupId = intent.getStringExtra(IntentUtils.EXTRA_GROUP_ID);


                GroupManager.updateGroup(this, groupId, groupEvent, null);
            }
            if (intent.getAction().equals(IntentUtils.INTENT_ACTION_FETCH_AND_CREATE_GROUP)) {
                String groupId = intent.getStringExtra(IntentUtils.EXTRA_GROUP_ID);
                GroupManager.fetchAndCreateGroup(this, groupId, false, null);

            } else if (intent.getAction().equals(IntentUtils.INTENT_ACTION_FETCH_USER_GROUPS_AND_BROADCASTS)) {
                GroupManager.fetchUserGroups(new GroupManager.OnComplete() {
                    @Override
                    public void onComplete(final boolean isSuccess) {
                        if (isSuccess) {
                            BroadcastManager.fetchBroadcasts(FireManager.getUid(), new BroadcastManager.OnComplete() {
                                @Override
                                public void onComplete(boolean isSuccessful) {
                                    SharedPreferencesManager.setFetchUserGroupsSaved(true);
                                    EventBus.getDefault().post(new FetchingUserGroupsAndBroadcastsFinished());
                                }
                            });
                        }
                    }
                });
            } else if (intent.getAction().equals(IntentUtils.INTENT_ACTION_HANDLE_REPLY)) {
                String messageId = intent.getStringExtra(IntentUtils.EXTRA_MESSAGE_ID);
                final Message message = RealmHelper.getInstance().getMessage(messageId, chatId);
                if (message != null) {
                    DownloadManager.sendMessage(message, new DownloadManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            if (isSuccessful) {
                                //set other unread messages as read
                                if (!message.isGroup())
                                    FireManager.setMessagesAsRead(NetworkService.this, message.getChatId());
                                //update unread count to 0
                            }
                        }
                    });
                }
            } else {
                String messageId = intent.getStringExtra(IntentUtils.EXTRA_MESSAGE_ID);
                if (intent.getAction().equals(IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE)) {
                    String myUid = intent.getStringExtra(IntentUtils.EXTRA_MY_UID);
                    int state = intent.getIntExtra(IntentUtils.EXTRA_STAT, 0);
                    updateMessageStat(messageId, myUid, chatId, state);
                } else if (intent.getAction().equals(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE)) {
                    String myUid = intent.getStringExtra(IntentUtils.EXTRA_MY_UID);
                    updateVoiceMessageStat(messageId, chatId, myUid);
                } else {
                    Message message = RealmHelper.getInstance().getMessage(messageId, chatId);
                    if (message != null) {
                        DownloadManager.request(message, null);
                    }
                }
            }
        }
        return START_STICKY;
    }


    public void updateMessageStat(final String messageId, final String myUid, final String chatId, final int state) {
        FireManager.updateMessageStat(myUid, messageId, state, new FireManager.OnComplete() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    RealmHelper.getInstance().updateMessageStatLocally(messageId, chatId, state);
                    RealmHelper.getInstance().deleteUnUpdateStat(messageId);
                } else {
                    RealmHelper.getInstance().saveUnUpdatedMessageStat(myUid, messageId, state);
                }
            }
        });

    }


    public void updateVoiceMessageStat(final String messageId, final String chatId, final String myUid) {
        FireManager.updateVoiceMessageStat(myUid, messageId, new FireManager.OnComplete() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    RealmHelper.getInstance().updateVoiceMessageStatLocally(messageId, chatId);
                    RealmHelper.getInstance().deleteUnUpdatedVoiceMessageStat(messageId);
                } else {
                    RealmHelper.getInstance().saveUnUpdatedVoiceMessageStat(myUid, messageId, true);
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        DownloadManager.cancelAllTasks();
        super.onDestroy();
        startService(new Intent(this, NetworkService.class));


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
