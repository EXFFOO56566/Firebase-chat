package com.devlomi.fireapp.utils;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import com.devlomi.fireapp.job.NetworkJobService;
import com.devlomi.fireapp.job.SaveTokenJob;
import com.devlomi.fireapp.job.SyncContactsJob;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.services.AudioService;
import com.devlomi.fireapp.services.FCMRegistrationService;
import com.devlomi.fireapp.services.NetworkService;
import com.devlomi.fireapp.services.SyncContactsService;

import java.util.UUID;

/**
 * Created by Devlomi on 09/01/2018.
 */

//this will manage service starts and put extras
public class ServiceHelper {

    //this will fire a Network request (download or upload request or event a Firebase database operation)
    public static void startNetworkRequest(Context context, String messageId, String chatId) {
        String action = IntentUtils.INTENT_ACTION_NETWORK_REQUEST;

        if (Util.isOreoOrAbove()) {
            PersistableBundle bundle = new PBundleUtil.Builder(messageId).action(action).messageId(messageId).chatId(chatId).build().getBundle();

            NetworkJobService.schedule(context, messageId, bundle);

        } else {
            Intent intent = new Intent(context, NetworkService.class);
            //set the action to identify the type
            intent.setAction(action);
            intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
            intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
            context.startService(intent);
        }
    }


    //this will update the received message stat and set it as Received or read in Firebase database
    public static void startUpdateMessageStatRequest(Context context, String messageId, String myUid, String chatId, int statToBeUpdated) {
        if (Util.isOreoOrAbove()) {
            PersistableBundle bundle = new PBundleUtil.Builder(messageId).action(IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE)
                    .messageId(messageId)
                    .myUid(myUid)
                    .chatId(chatId)
                    .stat(statToBeUpdated)
                    .build()
                    .getBundle();

            NetworkJobService.schedule(context, messageId, bundle);
        } else {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE);
            intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
            intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
            intent.putExtra(IntentUtils.EXTRA_MY_UID, myUid);
            intent.putExtra(IntentUtils.EXTRA_STAT, statToBeUpdated);
            context.startService(intent);
        }
    }

    //this will update the received voice message stat and set it as seen in Firebase database
    public static void startUpdateVoiceMessageStatRequest(Context context, String messageId, String chatId, String myUid) {
        if (Util.isOreoOrAbove()) {
            PersistableBundle bundle = new PBundleUtil.Builder(messageId)
                    .action(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE)
                    .messageId(messageId)
                    .myUid(myUid)
                    .chatId(chatId)
                    .build()
                    .getBundle();

            NetworkJobService.schedule(context, messageId, bundle);


        } else {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE);
            intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
            intent.putExtra(IntentUtils.EXTRA_MY_UID, myUid);
            intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
            context.startService(intent);
        }
    }

    //this will fire up sync contacts operation
    public static void startSyncContacts(Context context) {
        if (Util.isOreoOrAbove()) {
            SyncContactsJob.schedule(context);
        } else {
            Intent intent = new Intent(context, SyncContactsService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_SYNC_CONTACTS);
            context.startService(intent);
        }
    }


    public static void fetchAndCreateGroup(Context context, String groupId) {
        if (Util.isOreoOrAbove()) {

            PersistableBundle bundle = new PBundleUtil.Builder(groupId)
                    .action(IntentUtils.INTENT_ACTION_FETCH_AND_CREATE_GROUP)
                    .groupId(groupId)
                    .build()
                    .getBundle();

            NetworkJobService.schedule(context, groupId, bundle);
        } else {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_FETCH_AND_CREATE_GROUP);
            intent.putExtra(IntentUtils.EXTRA_GROUP_ID, groupId);
            context.startService(intent);
        }
    }


    public static void fetchUserGroupsAndBroadcasts(Context context) {
        if (Util.isOreoOrAbove()) {
            PersistableBundle bundle = new PBundleUtil.Builder(UUID.randomUUID().toString()).
                    action(IntentUtils.INTENT_ACTION_FETCH_USER_GROUPS_AND_BROADCASTS).
                    build()
                    .getBundle();

            NetworkJobService.schedule(context, UUID.randomUUID().toString(), bundle);

        } else {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_FETCH_USER_GROUPS_AND_BROADCASTS);
            context.startService(intent);
        }
    }

    public static void updateGroupInfo(Context context, String groupId, GroupEvent groupEvent) {
        if (Util.isOreoOrAbove()) {

            PersistableBundle bundle = new PBundleUtil.Builder(groupId).action(IntentUtils.INTENT_ACTION_UPDATE_GROUP).groupId(groupId).groupEvent(groupEvent).build().getBundle();
            NetworkJobService.schedule(context, groupId, bundle);

        } else {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_UPDATE_GROUP);
            intent.putExtra(IntentUtils.EXTRA_GROUP_ID, groupId);
            intent.putExtra(IntentUtils.EXTRA_GROUP_EVENT, groupEvent);
            context.startService(intent);
        }
    }


    public static void saveToken(Context context, String token) {
        if (Util.isOreoOrAbove()) {
            SaveTokenJob.schedule(context, token);
        } else {
            Intent intent = new Intent(context, FCMRegistrationService.class);
            intent.putExtra(IntentUtils.FCM_TOKEN, token);
            context.startService(intent);
        }
    }

    public static void handleReply(Context context, String uid, String chatId, String text) {
        User user = RealmHelper.getInstance().getUser(uid);
        if (user != null) {
            Message message = new MessageCreator.Builder(user, MessageType.SENT_TEXT).text(text).build();
            String messageId = message.getMessageId();
            String action = IntentUtils.INTENT_ACTION_HANDLE_REPLY;
            if (Util.isOreoOrAbove()) {

                PersistableBundle bundle = new PBundleUtil.Builder(messageId).action(action)
                        .messageId(messageId)
                        .chatId(chatId)
                        .build()
                        .getBundle();

                NetworkJobService.schedule(context, messageId, bundle);
            } else {
                Intent intent = new Intent(context, NetworkService.class);
                intent.setAction(action);
                intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
                context.startService(intent);
            }
        }

    }

    public static void playAudio(Context context, String id, String url, int pos, int progress) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(IntentUtils.ACTION_START_PLAY);
        intent.putExtra(IntentUtils.ID, id)
                .putExtra(IntentUtils.URL, url)
                .putExtra(IntentUtils.POS, pos)
                .putExtra(IntentUtils.PROGRESS, progress);

        if (Util.isOreoOrAbove())
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }

    public static void seekTo(Context context, String id, int progress) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(IntentUtils.ACTION_SEEK_TO);
        intent.putExtra(IntentUtils.ID, id);
        intent.putExtra(IntentUtils.PROGRESS, progress);
        context.startService(intent);
    }


    public static void stopAudio(Context context) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(IntentUtils.ACTION_STOP_AUDIO);

        context.startService(intent);

    }

    public static void headsetStateChanged(Context context, int currentHeadsetState) {
        Intent intent = new Intent(context, AudioService.class).putExtra(IntentUtils.EXTRA_HEADSETSTATE_CHANGED, currentHeadsetState);
        context.startService(intent);

    }
}



