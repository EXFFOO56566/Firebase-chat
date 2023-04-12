package com.devlomi.fireapp.utils;

import com.devlomi.fireapp.model.constants.StatusType;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Devlomi on 01/08/2017.
 */

//this class contains firebase database and firebase storage paths and refs
public class FireConstants {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseStorage sotarage = FirebaseStorage.getInstance();
    public static final DatabaseReference mainRef = database.getReference();
    //users ref that contain user's data (name,phone,photo etc..)
    public static final DatabaseReference usersRef = mainRef.child("users");

    //groups ref that contains user ids and group info
    public static final DatabaseReference groupsRef = mainRef.child("groups");
    public static final DatabaseReference groupsEventsRef = mainRef.child("groupEvents");

    //this will contain all groups ids that the user participated to
    public static DatabaseReference groupsByUser = mainRef.child("groupsByUser");

    //this will get whom added the user to a group
    public static DatabaseReference groupMemberAddedBy = mainRef.child("groupMemberAddedBy");
    //this holds groups links
    public static DatabaseReference groupsLinks = mainRef.child("groupsLinks");
    public static DatabaseReference groupLinkById = mainRef.child("groupLinkById");
    //this is used when an admin removes a group member so the removed user will not be able
    //to re join this group via link again.
    public static DatabaseReference deletedGroupsUsers = mainRef.child("groupsDeletedUsers");

    //this holds broadcasts data like info and users
    public static DatabaseReference broadcastsRef = mainRef.child("broadcasts");
    //this holds broadcasts messages
    public static DatabaseReference broadcastsMessagesRef = mainRef.child("broadcastsMessages");
    //used when user uninstalled and reinstalled to re-fetch the broadcast
    public static DatabaseReference broadcastsByUser = mainRef.child("broadcastsByUser");

    //this will save the UID's of the users whom saw the status
    public static DatabaseReference statusSeenUidsRef = mainRef.child("statusSeenUids");

    //this will get the status count
    public static DatabaseReference statusCountRef = mainRef.child("statusCount");


    //this will delete a message for all users
    private static DatabaseReference deleteMessageRequests = mainRef.child("deleteMessageRequests");

    private static DatabaseReference deleteMessageRequestsForGroup = mainRef.child("deleteMessageRequestsForGroup");

    private static DatabaseReference deleteMessageRequestsForBroadcast = mainRef.child("deleteMessageRequestsForBroadcast");


    //this is the MAJOR ref ,all messages goes in this ref
    public static final DatabaseReference messages = mainRef.child("messages");

    public static final DatabaseReference groupsMessages = mainRef.child("groupsMessages");
    //this ref is for the messages sates (received,read)
    public static final DatabaseReference messageStat = mainRef.child("messages-stat");
    //this ref is for the voice messages sates (is listened or not yet)
    public static final DatabaseReference voiceMessageStat = mainRef.child("voice-messages-stat");

    //all statuses goes here
    public static final DatabaseReference statusRef = mainRef.child("status");
    public static final DatabaseReference textStatusRef = mainRef.child("textStatus");

    //this will save if calls is missed or not
    public static DatabaseReference callsRef = mainRef.child("calls");


    //this ref is for the user state is he online or not ,if he is not online this will contain the last seen timestamp
    public static final DatabaseReference presenceRef = mainRef.child("presence");

    //this will have the user typing or recording or do nothing value when he chatting with another user
    public static final DatabaseReference typingStat = mainRef.child("typingStat").child(FireManager.getUid());

    public static final DatabaseReference groupTypingStat = mainRef.child("groupTypingStat");


    //this is used when the user blocks another user it will save the blocked uid
    public static DatabaseReference blockedUsersRef = mainRef.child("blockedUsers");

    //this will get the user id by his phone number to use it when searching for a user
    public static DatabaseReference uidByPhone = mainRef.child("uidByPhone");


    public static final StorageReference storageRef = sotarage.getReference();
    //firebase storage folders ,used when uploading or downloading
    public static final StorageReference imageRef = storageRef.child("image");
    public static final StorageReference imageProfileRef = storageRef.child("image_profile");
    public static final StorageReference videoRef = storageRef.child("video");
    public static final StorageReference voiceRef = storageRef.child("voice");
    public static final StorageReference fileRef = storageRef.child("file");
    public static final StorageReference audioRef = storageRef.child("audio");
    public static final StorageReference statusStorageRef = storageRef.child("status");


    //MAX SIZE FOR FCM message IS 4096 ,however we want some more space for other items regardless "Content"
    public static int MAX_SIZE_STRING = 3800;

    public static DatabaseReference getMessageRef(boolean isGroup, boolean isBroadcast, String groupOrBroadcastId) {
        if (isGroup)
            return groupsMessages.child(groupOrBroadcastId);
        if (isBroadcast)
            return broadcastsMessagesRef.child(groupOrBroadcastId);

        return messages;
    }

    public static DatabaseReference getDeleteMessageRequestsRef(String messageId, boolean isGroup, boolean isBroadcast, String groupOrBroadcastId) {
        if (isGroup)
            return deleteMessageRequestsForGroup.child(groupOrBroadcastId).child(messageId);

        else if (isBroadcast)
            return deleteMessageRequestsForBroadcast.child(groupOrBroadcastId).child(messageId);

        return deleteMessageRequests.child(messageId);
    }

    public static DatabaseReference getMyStatusRef(int type) {
        if (type == StatusType.TEXT)
            return textStatusRef.child(FireManager.getUid());
        else
            return statusRef.child(FireManager.getUid());
    }
}
