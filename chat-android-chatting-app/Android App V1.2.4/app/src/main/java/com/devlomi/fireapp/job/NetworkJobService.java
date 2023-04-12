package com.devlomi.fireapp.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;

import com.devlomi.fireapp.events.FetchingUserGroupsAndBroadcastsFinished;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.JobId;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.UnUpdatedStat;
import com.devlomi.fireapp.utils.BroadcastManager;
import com.devlomi.fireapp.utils.DownloadManager;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.JobSchedulerSingleton;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;

import io.realm.RealmResults;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkJobService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        PersistableBundle extras = jobParameters.getExtras();
        String action = extras.getString(IntentUtils.ACTION_TYPE);
        final boolean isVoiceMessage = isVoiceMessage(jobParameters);

        if (action.equals(IntentUtils.INTENT_ACTION_UPDATE_GROUP)) {

            PersistableBundle groupEventBundle = extras.getPersistableBundle(IntentUtils.EXTRA_GROUP_EVENT);
            String contextStart = groupEventBundle.getString(IntentUtils.EXTRA_CONTEXT_START);
            int eventType = groupEventBundle.getInt(IntentUtils.EXTRA_EVENT_TYPE);
            String contextEnd = groupEventBundle.getString(IntentUtils.EXTRA_CONTEXT_END);
            GroupEvent groupEvent = new GroupEvent(contextStart, eventType, contextEnd);
            final String groupId = extras.getString(IntentUtils.EXTRA_GROUP_ID);
            GroupManager.updateGroup(this, groupId, groupEvent, new GroupManager.OnComplete() {
                @Override
                public void onComplete(boolean isSuccess) {
                    if (isSuccess) {
                        RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);
                    }
                    onFinishJob(jobParameters, !isSuccess);

                }
            });
        } else if (action.equals(IntentUtils.INTENT_ACTION_FETCH_AND_CREATE_GROUP)) {
            String groupId = extras.getString(IntentUtils.EXTRA_GROUP_ID);
            GroupManager.fetchAndCreateGroup(this, groupId, false, new GroupManager.OnFetchGroupsComplete() {
                @Override
                public void onComplete(String groupId) {
                    if (groupId != null) {
                        RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);
                    }
                    onFinishJob(jobParameters, groupId == null);
                }
            });


        } else if (action.equals(IntentUtils.INTENT_ACTION_FETCH_USER_GROUPS_AND_BROADCASTS)) {
            GroupManager.fetchUserGroups(new GroupManager.OnComplete() {
                @Override
                public void onComplete(final boolean isSuccess) {
                    if (isSuccess) {

                        BroadcastManager.fetchBroadcasts(FireManager.getUid(), new BroadcastManager.OnComplete() {
                            @Override
                            public void onComplete(boolean isSuccessful) {
                                SharedPreferencesManager.setFetchUserGroupsSaved(true);
                                EventBus.getDefault().post(new FetchingUserGroupsAndBroadcastsFinished());
                                onFinishJob(jobParameters, !isSuccessful);
                            }
                        });
                    } else {
                        onFinishJob(jobParameters, !isSuccess);
                    }
                }
            });
        } else {
            final String messageId = extras.getString(IntentUtils.EXTRA_MESSAGE_ID);
            final String chatId = extras.getString(IntentUtils.EXTRA_CHAT_ID);
            if (action.equals(IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE)) {
                final String myUid = extras.getString(IntentUtils.EXTRA_MY_UID);
                final int state = extras.getInt(IntentUtils.EXTRA_STAT, 0);
                FireManager.updateMessageStat(myUid, messageId, state, new FireManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        if (isSuccessful) {
                            RealmHelper.getInstance().updateMessageStatLocally(messageId, state);
                            RealmHelper.getInstance().deleteUnUpdateStat(messageId);
                        } else {
                            RealmHelper.getInstance().saveUnUpdatedMessageStat(myUid, messageId, state);
                        }
                        final RealmResults<UnUpdatedStat> unUpdateMessageStat = RealmHelper.getInstance().getUnUpdateMessageStat();
                        if (unUpdateMessageStat.isEmpty()) {
                            onFinishJob(jobParameters, false);

                        } else {
                            int i = 0;
                            for (final UnUpdatedStat unUpdatedStat : unUpdateMessageStat) {
                                i++;
                                final int finalI = i;
                                FireManager.updateMessageStat(unUpdatedStat.getMyUid(), unUpdatedStat.getMessageId(), unUpdatedStat.getStatToBeUpdated(), new FireManager.OnComplete() {
                                    @Override
                                    public void onComplete(boolean isSuccessful) {
                                        if (isSuccessful) {
                                            RealmHelper.getInstance().updateMessageStatLocally(unUpdatedStat.getMessageId(), unUpdatedStat.getStatToBeUpdated());
                                            RealmHelper.getInstance().deleteUnUpdateStat(unUpdatedStat.getMessageId());
                                            RealmHelper.getInstance().deleteJobId(unUpdatedStat.getMessageId(), isVoiceMessage);
                                        }
                                        if (finalI == unUpdateMessageStat.size()) {
                                            jobFinished(jobParameters, !isSuccessful);
                                        }
                                    }

                                });
                            }
                        }

                    }
                });
            } else if (action.equals(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE)) {
                final String myUid = extras.getString(IntentUtils.EXTRA_MY_UID);
                FireManager.updateVoiceMessageStat(myUid, messageId, new FireManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        if (isSuccessful) {
                            RealmHelper.getInstance().updateVoiceMessageStatLocally(messageId);
                            RealmHelper.getInstance().deleteUnUpdatedVoiceMessageStat(messageId);
                        } else {
                            RealmHelper.getInstance().saveUnUpdatedVoiceMessageStat(myUid, messageId, true);
                        }
                    }
                });

            } else if (action.equals(IntentUtils.INTENT_ACTION_HANDLE_REPLY)) {
                final Message message = RealmHelper.getInstance().getMessage(messageId, chatId);
                if (message != null) {
                    DownloadManager.sendMessage(message, new DownloadManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            if (isSuccessful) {
                                //set other unread messages as read
                                if (!message.isGroup())
                                    FireManager.setMessagesAsRead(NetworkJobService.this, message.getChatId());

                            }
                            onFinishJob(jobParameters, !isSuccessful);

                        }
                    });
                }
            } else {
                Message message = RealmHelper.getInstance().getMessage(messageId, chatId);
                if (message != null) {
                    DownloadManager.request(message, new DownloadManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccess) {
                            onFinishJob(jobParameters, !isSuccess);
                        }
                    });
                }
            }
        }


        return true;
    }

    private boolean isVoiceMessage(JobParameters jobParameters) {
        return jobParameters.getExtras().getString(IntentUtils.ACTION_TYPE).equals(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE);
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        int jobId = jobParameters.getJobId();
        boolean isVoiceMessage = isVoiceMessage(jobParameters);
        String id = RealmHelper.getInstance().getJobId(jobId, isVoiceMessage);


        return true;
    }

    public static void schedule(Context context, String id, PersistableBundle bundle) {
        ComponentName component = new ComponentName(context, NetworkJobService.class);


        String action = bundle.getString(IntentUtils.ACTION_TYPE);

        //if it's a voice message then we want to generate a new id
        // because in case if  the action was 'update message state' both will have the same id

        JobId jobId = new JobId(id, action.equals(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE));
        RealmHelper.getInstance().saveJobId(jobId);
        int mJobId = jobId.getJobId();

        JobInfo.Builder builder = new JobInfo.Builder(mJobId, component)
                .setMinimumLatency(1)
                .setOverrideDeadline(1)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle);

        JobSchedulerSingleton.getInstance().schedule(builder.build());


    }


    private void onFinishJob(JobParameters jobParameters, boolean needsReschedule) {
        if (!needsReschedule) {
            String id = jobParameters.getExtras().getString(IntentUtils.ID);
            RealmHelper.getInstance().deleteJobId(id, isVoiceMessage(jobParameters));
        }
        jobFinished(jobParameters, needsReschedule);
    }

    public static void cancel(String messageId) {
        int jobId = RealmHelper.getInstance().getJobId(messageId, false);

        if (jobId != -1) {
            JobSchedulerSingleton.getInstance().cancel(jobId);

        }
    }
}
