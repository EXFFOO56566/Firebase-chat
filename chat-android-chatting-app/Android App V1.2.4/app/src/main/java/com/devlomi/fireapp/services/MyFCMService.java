package com.devlomi.fireapp.services;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.devlomi.fireapp.model.constants.DBConstants;
import com.devlomi.fireapp.model.constants.DownloadUploadStat;
import com.devlomi.fireapp.model.constants.FireCallType;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.constants.PendingGroupTypes;
import com.devlomi.fireapp.model.realms.FireCall;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.PendingGroupJob;
import com.devlomi.fireapp.model.realms.PhoneNumber;
import com.devlomi.fireapp.model.realms.QuotedMessage;
import com.devlomi.fireapp.model.realms.RealmContact;
import com.devlomi.fireapp.model.realms.RealmLocation;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.DownloadManager;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.JsonUtil;
import com.devlomi.fireapp.utils.ListUtil;
import com.devlomi.fireapp.utils.NotificationHelper;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.CallNotificationResult;

import java.util.ArrayList;
import java.util.Map;

import io.realm.RealmList;

public class MyFCMService extends FirebaseMessagingService {


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (FireManager.getUid() == null)
            return;//if the user clears the app data or sign out we don't wan't to do nothing
        SharedPreferencesManager.setTokenSaved(false);

        ServiceHelper.saveToken(this, s);
    }


    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (FireManager.getUid() == null)
            return;//if the user clears the app data or sign out we don't wan't to do nothing

        Map data = remoteMessage.getData();

        //if this payload is Sinch Call
        if (SinchHelpers.isSinchPushPayload(remoteMessage.getData())) {
            new ServiceConnection() {

                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (payload != null) {
                        CallingService.SinchServiceInterface sinchService = (CallingService.SinchServiceInterface) service;
                        if (sinchService != null) {
                            NotificationResult result = sinchService.relayRemotePushNotificationPayload(payload);
                            //if the Messages is a call
                            if (result.isValid() && result.isCall()) {

                                CallNotificationResult callResult = result.getCallResult();
                                String callId = callResult.getCallId();

                                //if this call was missed (user did not answer)
                                if (callResult.isCallCanceled()) {
                                    RealmHelper.getInstance().setCallAsMissed(callId);
                                    User user = RealmHelper.getInstance().getUser(callResult.getRemoteUserId());
                                    FireCall fireCall = RealmHelper.getInstance().getFireCall(callId);
                                    if (user != null && fireCall != null) {
                                        String phoneNumber = fireCall.getPhoneNumber();
                                        new NotificationHelper(MyFCMService.this).createMissedCallNotification(user, phoneNumber);
                                    }
                                } else {
                                    Map<String, String> headers = callResult.getHeaders();
                                    if (!headers.isEmpty()) {
                                        String phoneNumber = headers.get("phoneNumber");
                                        String timestampStr = headers.get("timestamp");
                                        if (phoneNumber != null && timestampStr != null) {
                                            long timestamp = Long.parseLong(timestampStr);
                                            User user = RealmHelper.getInstance().getUser(callResult.getRemoteUserId());
                                            FireCall fireCall = new FireCall(callId, user, FireCallType.INCOMING, timestamp, phoneNumber, callResult.isVideoOffered());
                                            RealmHelper.getInstance().saveObjectToRealm(fireCall);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    payload = null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }

                public void relayMessageData(Map<String, String> data) {
                    payload = data;
                    Intent intent = new Intent(getApplicationContext(), CallingService.class);
                    getApplicationContext().bindService(intent, this, BIND_AUTO_CREATE);
                }
            }.relayMessageData(data);


        } else if (remoteMessage.getData().containsKey("event")) {
            //this will called when something is changed in group.
            // like member removed,added,admin changed, group info changed...
            if (remoteMessage.getData().get("event").equals("group_event")) {
                String groupId = remoteMessage.getData().get("groupId");
                String eventId = remoteMessage.getData().get("eventId");
                String contextStart = remoteMessage.getData().get("contextStart");
                int eventType = Integer.parseInt(remoteMessage.getData().get("eventType"));
                String contextEnd = remoteMessage.getData().get("contextEnd");
                //if this event was by the admin himself  OR if the event already exists do nothing
                if (contextStart.equals(SharedPreferencesManager.getPhoneNumber())
                        || RealmHelper.getInstance().getMessage(eventId) != null) {
                    return;
                }


                GroupEvent groupEvent = new GroupEvent(contextStart, eventType, contextEnd, eventId);
                PendingGroupJob pendingGroupJob = new PendingGroupJob(groupId, PendingGroupTypes.CHANGE_EVENT, groupEvent);
                RealmHelper.getInstance().saveObjectToRealm(pendingGroupJob);
                ServiceHelper.updateGroupInfo(this, groupId, groupEvent);

            } else if (remoteMessage.getData().get("event").equals("new_group")) {

                String groupId = remoteMessage.getData().get("groupId");
                User user = RealmHelper.getInstance().getUser(groupId);

                //if the group is not exists,fetch and download it
                if (user == null) {
                    PendingGroupJob pendingGroupJob = new PendingGroupJob(groupId, PendingGroupTypes.CREATION_EVENT, null);
                    RealmHelper.getInstance().saveObjectToRealm(pendingGroupJob);
                    ServiceHelper.fetchAndCreateGroup(this, groupId);
                } else {

                    RealmList<User> users = user.getGroup().getUsers();
                    User userById = ListUtil.getUserById(FireManager.getUid(), users);

                    //if the group is not active or the group does not contain current user
                    // then fetch and download it and set it as Active
                    if (!user.getGroup().isActive() || !users.contains(userById)) {
                        PendingGroupJob pendingGroupJob = new PendingGroupJob(groupId, PendingGroupTypes.CREATION_EVENT, null);
                        RealmHelper.getInstance().saveObjectToRealm(pendingGroupJob);
                        ServiceHelper.fetchAndCreateGroup(this, groupId);
                    }
                }


            } else if (remoteMessage.getData().get("event").equals("message_deleted")) {
                String messageId = remoteMessage.getData().get("messageId");
                Message message = RealmHelper.getInstance().getMessage(messageId);
                RealmHelper.getInstance().setMessageDeleted(messageId);

                if (message != null) {
                    if (message.getDownloadUploadStat() == DownloadUploadStat.LOADING) {
                        if (MessageType.isSentType(message.getType())) {
                            DownloadManager.cancelUpload(message.getMessageId());
                        } else
                            DownloadManager.cancelDownload(message.getMessageId());
                    }
                    new NotificationHelper(this).messageDeleted(message);
                }


            } else if (remoteMessage.getData().get("event").equals("call")) {


            }
        } else {
            final String messageId = remoteMessage.getData().get(DBConstants.MESSAGE_ID);

            //if message is deleted do not save it
            if (RealmHelper.getInstance().getDeletedMessage(messageId) != null)
                return;


            boolean isGroup = remoteMessage.getData().containsKey("isGroup");
            //getting data from fcm message and convert it to a message
            final String phone = remoteMessage.getData().get(DBConstants.PHONE);
            final String content = remoteMessage.getData().get(DBConstants.CONTENT);
            final String timestamp = remoteMessage.getData().get(DBConstants.TIMESTAMP);
            final int type = Integer.parseInt(remoteMessage.getData().get(DBConstants.TYPE));
            //get sender uid
            final String fromId = remoteMessage.getData().get(DBConstants.FROM_ID);
            String toId = remoteMessage.getData().get(DBConstants.TOID);
            final String metadata = remoteMessage.getData().get(DBConstants.METADATA);
            //convert sent type to received
            int convertedType = MessageType.convertSentToReceived(type);

            //if it's a group message and the message sender is the same
            if (fromId.equals(FireManager.getUid()))
                return;

            //create the message
            final Message message = new Message();
            message.setContent(content);
            message.setTimestamp(timestamp);
            message.setFromId(fromId);
            message.setType(convertedType);
            message.setMessageId(messageId);
            message.setMetadata(metadata);
            message.setToId(toId);
            message.setChatId(isGroup ? toId : fromId);
            message.setGroup(isGroup);
            if (isGroup)
                message.setFromPhone(phone);
            //set default state
            message.setDownloadUploadStat(DownloadUploadStat.FAILED);


            //check if it's text message
            if (MessageType.isSentText(type)) {
                //set the state to default
                message.setDownloadUploadStat(DownloadUploadStat.DEFAULT);


                //check if it's a contact
            } else if (remoteMessage.getData().containsKey(DBConstants.CONTACT)) {
                message.setDownloadUploadStat(DownloadUploadStat.DEFAULT);
                //get the json contact as String
                String jsonString = remoteMessage.getData().get(DBConstants.CONTACT);
                //convert contact numbers from JSON to ArrayList
                ArrayList<PhoneNumber> phoneNumbersList = JsonUtil.getPhoneNumbersList(jsonString);
                // convert it to RealmContact and set the contact name using content
                RealmContact realmContact = new RealmContact(content, phoneNumbersList);

                message.setContact(realmContact);


                //check if it's a location message
            } else if (remoteMessage.getData().containsKey(DBConstants.LOCATION)) {
                message.setDownloadUploadStat(DownloadUploadStat.DEFAULT);
                //get the json location as String
                String jsonString = remoteMessage.getData().get(DBConstants.LOCATION);
                //convert location from JSON to RealmLocation
                RealmLocation location = JsonUtil.getRealmLocationFromJson(jsonString);
                message.setLocation(location);
            }

            //check if it's image or Video
            else if (remoteMessage.getData().containsKey(DBConstants.THUMB)) {
                final String thumb = remoteMessage.getData().get(DBConstants.THUMB);

                //Check if it's Video and set Video Duration
                if (remoteMessage.getData().containsKey(DBConstants.MEDIADURATION)) {
                    final String mediaDuration = remoteMessage.getData().get(DBConstants.MEDIADURATION);
                    message.setMediaDuration(mediaDuration);
                }

                message.setThumb(thumb);


                //check if it's Voice Message or Audio File
            } else if (remoteMessage.getData().containsKey(DBConstants.MEDIADURATION)
                    && type == MessageType.SENT_VOICE_MESSAGE || type == MessageType.SENT_AUDIO) {

                //set audio duration
                final String mediaDuration = remoteMessage.getData().get(DBConstants.MEDIADURATION);


                message.setMediaDuration(mediaDuration);

                //check if it's a File
            } else if (remoteMessage.getData().containsKey(DBConstants.FILESIZE)) {
                String fileSize = remoteMessage.getData().get(DBConstants.FILESIZE);


                message.setFileSize(fileSize);

            }

            //if the message was quoted save it and get the quoted message
            if (remoteMessage.getData().containsKey("quotedMessageId")) {
                String quotedMessageId = remoteMessage.getData().get("quotedMessageId");
                //sometimes the message is not saved because of threads,
                //so we need to make sure that we refresh the database before checking if the message is exists
                RealmHelper.getInstance().refresh();
                Message quotedMessage = RealmHelper.getInstance().getMessage(quotedMessageId, fromId);
                if (quotedMessage != null)
                    message.setQuotedMessage(QuotedMessage.messageToQuotedMessage(quotedMessage));
            }


            //Save it to database and fire notification
            new NotificationHelper(MyFCMService.this).handleNewMessage(phone, message);

        }
    }

}