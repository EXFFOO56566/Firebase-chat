package com.devlomi.fireapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.devlomi.fireapp.events.GroupActiveStateChanged;
import com.devlomi.fireapp.model.constants.DBConstants;
import com.devlomi.fireapp.model.constants.GroupEventTypes;
import com.devlomi.fireapp.model.realms.Group;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.RealmList;

public class GroupManager {
    private static String lastGroupId = "";


    //this will create a new group and add users to this group
    public static void createNewGroup(final Context context, final String groupTitle, final List<User> selectedUsers, final OnCreateGroupComplete onComplete) {
        //generate groupId
        final String groupId = FireConstants.groupsRef.push().getKey();
        //generate temp group image
        final File photoFile = new File(context.getCacheDir(), "group-img.png");

        //download defaultGroupProfilePhoto and save it in cache
        FireConstants.mainRef.child("defaultGroupProfilePhoto").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    if (onComplete != null)
                        onComplete.onComplete(false, null);
                    return;
                }

                final String photoUrl = dataSnapshot.getValue(String.class);

                FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
                        .getFile(photoFile)
                        .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    final Map<String, Object> result = new HashMap<>();
                                    final Map<String, Object> groupInfo = new HashMap<>();
                                    //set timeCreated
                                    groupInfo.put(DBConstants.TIMESTAMP, ServerValue.TIMESTAMP);
                                    //set whom created the group
                                    groupInfo.put("createdBy", SharedPreferencesManager.getPhoneNumber());
                                    //set onlyAdminsCanPost by default to false
                                    groupInfo.put("onlyAdminsCanPost", false);

                                    Map<String, Object> usersMap = User.toMap(selectedUsers, true);
                                    Bitmap circleBitmap = BitmapUtils.getCircleBitmap(BitmapUtils.convertFileImageToBitmap(photoFile.getPath()));
                                    final String thumbImg = BitmapUtils.decodeImageAsPng(circleBitmap);
                                    groupInfo.put("name", groupTitle);
                                    groupInfo.put("photo", photoUrl);
                                    groupInfo.put("thumbImg", thumbImg);
                                    result.put("info", groupInfo);
                                    result.put("users", usersMap);

                                    FireConstants.groupsRef.child(groupId).setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                User groupUser = new User();
                                                groupUser.setUserName(groupTitle);
                                                groupUser.setStatus("");
                                                groupUser.setPhone("");
                                                groupUser.setPhoto(photoUrl);
                                                groupUser.setThumbImg(thumbImg);
                                                RealmList<User> list = new RealmList();
                                                list.addAll(selectedUsers);
                                                //add current user
                                                list.add(SharedPreferencesManager.getCurrentUser());
                                                Group group = new Group();
                                                RealmList<String> adminUids = new RealmList<>();
                                                //set current id as an admin
                                                adminUids.add(FireManager.getUid());
                                                group.setAdminsUids(adminUids);
                                                group.setGroupId(groupId);
                                                group.setActive(true);
                                                group.setUsers(list);
                                                group.setTimestamp(new Date().getTime());
                                                group.setCreatedByNumber(SharedPreferencesManager.getPhoneNumber());

                                                groupUser.setGroup(group);
                                                groupUser.setGroupBool(true);
                                                groupUser.setUid(groupId);
                                                RealmHelper.getInstance().saveObjectToRealm(groupUser);
                                                //create group 'creation event'
                                                GroupEvent groupEvent = new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.GROUP_CREATION, null);
                                                groupEvent.createGroupEvent(groupUser, null);
                                                //add Group events 'this user added user x'
                                                for (User user : list) {
                                                    if (!user.getUid().equals(FireManager.getUid())) {
                                                        new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.USER_ADDED, user.getPhone()).createGroupEvent(groupUser, null);
                                                    }
                                                }
                                                //subscribe to this topic to receive FCM messages
                                                FirebaseMessaging.getInstance().subscribeToTopic(groupId);

                                                onComplete.onComplete(true, groupId);
                                            } else {
                                                onComplete.onComplete(true, null);
                                            }

                                        }
                                    });
                                } else {
                                    if (onComplete != null)
                                        onComplete.onComplete(false, null);
                                }

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (onComplete != null) onComplete.onComplete(false, null);
            }
        });

    }

    public static void fetchAndCreateGroupFromLink(Context context, String groupId, final OnFetchGroupsComplete onComplete) {
        fetchAndCreateGroup(context, groupId, true, new OnFetchGroupsComplete() {
            @Override
            public void onComplete(final String groupId) {
                //add this user to users node in Firebase Database
                if (groupId != null) {
                    FireConstants.groupsRef.child(groupId).child("users").child(FireManager.getUid()).setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (onComplete != null) onComplete.onComplete(groupId);
                            } else if (onComplete != null) onComplete.onComplete(null);
                        }
                    });
                }
            }
        });
    }

    //this is called when a user adds another user to a group
    //it will fetch the group and save it
    public static void fetchAndCreateGroup(final Context context, final String groupId, final boolean isComingFromLink, final OnFetchGroupsComplete onComplete) {


        FireConstants.groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot info = dataSnapshot.child("info");
                final DataSnapshot users = dataSnapshot.child("users");


                if (!isComingFromLink && !users.hasChild(FireManager.getUid())) {
                    if (onComplete != null)
                        onComplete.onComplete(groupId);

                    return;
                }

                final String groupName = info.child("name").getValue(String.class);
                final String photo = info.child("photo").getValue(String.class);
                final String thumbImg = info.child("thumbImg").getValue(String.class);
                final String createdBy = info.child("createdBy").getValue(String.class);
                final long createdAtTimestamp = info.child("timestamp").getValue(Long.class);
                final boolean onlyAdminsCanPost = info.child("onlyAdminsCanPost").getValue(Boolean.class);


                int i = 0;

                final RealmList<String> adminUids = new RealmList<>();
                final RealmList<User> userList = new RealmList<>();

                for (DataSnapshot snapshot : users.getChildren()) {
                    i++;

                    Boolean isAdmin = snapshot.getValue(Boolean.class);
                    String uid = snapshot.getKey();
                    //if the value is true,then he is an admin
                    if (isAdmin) {
                        adminUids.add(uid);
                    }

                    final int finalI = i;
                    //if the user is not stored in Realm fetch his data and save it
                    User user = RealmHelper.getInstance().getUser(uid);
                    if (user != null) {
                        userList.add(user);
                        if (finalI == users.getChildrenCount()) {
                            createAndSaveGroup(adminUids, groupId, userList, groupName, thumbImg, photo, createdBy, createdAtTimestamp, onlyAdminsCanPost, isComingFromLink, onComplete);
                        }
                    } else {
                        if (!uid.equals(FireManager.getUid())) {

                            FireManager.fetchUserByUid(context, uid, new FireManager.IsHasAppListener() {
                                @Override
                                public void onFound(User user) {
                                    userList.add(user);
                                    if (finalI == users.getChildrenCount()) {
                                        createAndSaveGroup(adminUids, groupId, userList, groupName, thumbImg, photo, createdBy, createdAtTimestamp, onlyAdminsCanPost, isComingFromLink, onComplete);
                                    }
                                }

                                @Override
                                public void onNotFound() {

                                }
                            });
                        } else {
                            if (finalI == users.getChildrenCount()) {
                                createAndSaveGroup(adminUids, groupId, userList, groupName, thumbImg, photo, createdBy, createdAtTimestamp, onlyAdminsCanPost, isComingFromLink, null);
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (onComplete != null)
                    onComplete.onComplete(null);
            }
        });

    }


    //this will save the group to realm and subscribe to FCM
    private static void createAndSaveGroup(final RealmList<String> adminUids
            , final String groupId, final RealmList<User> userList, String groupName
            , String thumbImg, String photo, String createdBy, long createdAtTimestamp
            , boolean onlyAdminsCanPost, final boolean isComingFromLink, final OnFetchGroupsComplete onComplete) {


        final Group group = new Group();
        group.setActive(true);
        group.setAdminsUids(adminUids);
        group.setGroupId(groupId);
        group.setOnlyAdminsCanPost(onlyAdminsCanPost);
        if (createdBy != null)
            group.setCreatedByNumber(createdBy);
        if (createdAtTimestamp != 0)
            group.setTimestamp(createdAtTimestamp);
        //add current user to the list
        User currentUser = SharedPreferencesManager.getCurrentUser();
        if (!userList.contains(currentUser))
            userList.add(currentUser);

        group.setUsers(userList);
        final User groupUser = new User();
        groupUser.setGroupBool(true);
        groupUser.setGroup(group);
        groupUser.setUserName(groupName);
        groupUser.setThumbImg(thumbImg);
        groupUser.setPhoto(photo);
        groupUser.setPhone("");
        groupUser.setStatus("");
        groupUser.setUid(groupId);


        RealmHelper.getInstance().saveObjectToRealm(groupUser);


        FirebaseMessaging.getInstance().subscribeToTopic(groupId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //if chat is not exists ,then get last 10 events
                if (RealmHelper.getInstance().getChat(groupId) == null) {
                    //getting last 10 events
                    FireConstants.groupsEventsRef.child(groupId).limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null)
                                return;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String eventId = snapshot.child("eventId").getValue(String.class);
                                GroupEvent groupEvent = snapshot.getValue(GroupEvent.class);
                                //if it's a creation event
                                if (groupEvent.getContextStart().equals(groupEvent.getContextEnd())) {
                                    groupEvent.setEventType(GroupEventTypes.GROUP_CREATION);
                                    groupEvent.setContextEnd("null");
                                }
                                groupEvent.createGroupEvent(groupUser, eventId);
                            }


                            RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);

                            EventBus.getDefault().post(new GroupActiveStateChanged(groupId, true));

                            if (onComplete != null)
                                onComplete.onComplete(groupId);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (onComplete != null)
                                onComplete.onComplete(null);
                        }
                    });
                    //if user is joining using an invite link
                } else if (isComingFromLink) {
                    GroupEvent groupEvent = new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.JOINED_VIA_LINK, "null");
                    groupEvent.createGroupEvent(groupUser, null);

                    RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);

                    EventBus.getDefault().post(new GroupActiveStateChanged(groupId, true));

                    if (onComplete != null)
                        onComplete.onComplete(groupId);
                }
                //otherwise just get whom added this user
                else {
                    FireConstants.groupMemberAddedBy.child(FireManager.getUid()).child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                if (onComplete != null)
                                    onComplete.onComplete(groupId);
                                return;
                            }

                            String addedByPhoneNumber = dataSnapshot.getValue(String.class);

                            GroupEvent groupEvent = new GroupEvent(addedByPhoneNumber, GroupEventTypes.USER_ADDED, SharedPreferencesManager.getPhoneNumber());
                            groupEvent.createGroupEvent(groupUser, null);

                            RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);

                            EventBus.getDefault().post(new GroupActiveStateChanged(groupId, true));

                            if (onComplete != null)
                                onComplete.onComplete(groupId);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (onComplete != null)
                                onComplete.onComplete(null);
                        }
                    });


                }


            }
        });

    }


    public static void removeGroupMember(String groupId, String uidOfUserToRemove, final OnComplete onComplete) {
        FireConstants.groupsRef.child(groupId).child("users").child(uidOfUserToRemove).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                notifyOnComplete(task.isSuccessful(), onComplete);
            }
        });
    }

    public static void addParticipant(String groupId, ArrayList<User> selectedUsers, final OnComplete onComplete) {
        Map<String, Object> usersMap = User.toMap(selectedUsers, false);
        FireConstants.groupsRef.child(groupId).child("users").updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                notifyOnComplete(task.isSuccessful(), onComplete);

            }
        });
    }

    public static void changeGroupName(final String groupTitle, final String groupId, final OnComplete onComplete) {
        FireConstants.groupsRef.child(groupId).child("info").child("name").setValue(groupTitle).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                RealmHelper.getInstance().changeGroupName(groupId, groupTitle);
                notifyOnComplete(task.isSuccessful(), onComplete);

            }
        });
    }

    //this will upload the user photo that he picked and generate a Small circle image and decode as base64
    public static void changeGroupImage(final String imagePath, final String groupId, final FireManager.OnComplete onComplete) {
        //generate new name for the file when uploading to firebase storage
        String fileName = UUID.randomUUID().toString() + Util.getFileExtensionFromPath(imagePath);
        //upload image
        FireConstants.imageProfileRef.child(fileName).putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //get download url of the image
                        String downloadUrl = String.valueOf(taskSnapshot.getDownloadUrl());
                        Map<String, Object> updateMap = new HashMap<>();

                        //generate circle bitmap
                        Bitmap circleBitmap = BitmapUtils.getCircleBitmap(BitmapUtils.convertFileImageToBitmap(imagePath));
                        //decode the image as base64 string
                        String decodedImage = BitmapUtils.decodeImageAsPng(circleBitmap);

                        //add the photo to the map
                        updateMap.put("photo", downloadUrl);
                        //add the thumb circle image to the map
                        updateMap.put("thumbImg", decodedImage);

                        //save them in firebase database using one request
                        FireConstants.groupsRef.child(groupId).child("info").updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (onComplete != null)
                                    onComplete.onComplete(task.isSuccessful());
                            }
                        });
                    }
                });
    }

    public static void exitGroup(final String groupId, final String uid, final OnComplete onComplete) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FireConstants.groupsRef.child(groupId).child("users").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            notifyOnComplete(task.isSuccessful(), onComplete);

                        }
                    });
                } else {
                    notifyOnComplete(false, onComplete);
                }
            }
        });
    }

    //this will update group info if something is changed,whether it's users change or group info change
    public static void updateGroup(final Context context, final String groupId, final GroupEvent groupEvent, final OnComplete onComplete) {
        FireConstants.groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot info = dataSnapshot.child("info");
                DataSnapshot users = dataSnapshot.child("users");
                List<String> unfetchedUsers = RealmHelper.getInstance().updateGroup(groupId, info, users);
                if (unfetchedUsers == null) return;
                if (groupEvent != null) {

                    GroupEvent mGroupEvent;

                    //if it is a creation event show whom created this group event
                    if (groupEvent.getContextStart().equals(groupEvent.getContextEnd())) {
                        mGroupEvent = new GroupEvent(groupEvent.getContextStart(), GroupEventTypes.GROUP_CREATION, "null");
                    } else {
                        mGroupEvent = new GroupEvent(groupEvent.getContextStart(), groupEvent.getEventType(), groupEvent.getContextEnd());
                    }
                    User group = RealmHelper.getInstance().getUser(groupId);
                    if (group == null) return;
                    mGroupEvent.createGroupEvent(group, mGroupEvent.getEventId());
                }
                if (!unfetchedUsers.isEmpty()) {
                    final String lastUnfetchedUser = unfetchedUsers.get(unfetchedUsers.size() - 1);
                    for (String unfetchedUser : unfetchedUsers) {
                        FireManager.fetchUserByUid(context, unfetchedUser, new FireManager.IsHasAppListener() {
                            @Override
                            public void onFound(User user) {
                                RealmHelper.getInstance().addUsersToGroup(groupId, user);
                                if (user.getUid().equals(lastUnfetchedUser)) {
                                    RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);
                                    notifyOnComplete(true, onComplete);
                                }

                            }

                            @Override
                            public void onNotFound() {

                            }
                        });
                    }
                } else {
                    RealmHelper.getInstance().deletePendingGroupCreationJob(groupId);
                    notifyOnComplete(true, onComplete);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                notifyOnComplete(false, onComplete);
            }
        });
    }

    private static void notifyOnComplete(boolean b, OnComplete onComplete) {
        if (onComplete != null)
            onComplete.onComplete(b);
    }

    public static void fetchUserGroups(final OnComplete onComplete) {
        lastGroupId = "";
        FireConstants.groupsByUser.child(FireManager.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    onComplete.onComplete(true);
                    return;
                }

                //getting lastGroupId in snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    lastGroupId = snapshot.getKey();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupId = snapshot.getKey();
                    fetchAndCreateGroup(MyApp.context(), groupId, false, new OnFetchGroupsComplete() {
                        @Override
                        public void onComplete(String groupId) {
                            if (groupId == null)
                                onComplete.onComplete(false);
                            else if (groupId.equals(lastGroupId)) {
                                onComplete.onComplete(true);
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void isUserBannedFromGroup(String groupId, String userId, final IsUserBannedCallback callback) {
        FireConstants.deletedGroupsUsers.child(groupId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (callback != null) callback.onComplete(dataSnapshot.getValue() != null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null) callback.onFailed();
            }
        });
    }

    //this will fetch group name,group creator name,participants count,first 6 users of this group
    public static void fetchGroupPartialInfo(final Context context, final String groupId, final FetchPartialGroupInfoCallback callback) {

        FireConstants.groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot info = dataSnapshot.child("info");
                final DataSnapshot users = dataSnapshot.child("users");

                //group details
                final String groupName = info.child("name").getValue(String.class);
                final String photo = info.child("photo").getValue(String.class);
                final String createdBy = info.child("createdBy").getValue(String.class);

                final int usersInGroupCount = (int) users.getChildrenCount();

                //NOTE this is un-managed object(Not saved to Database)
                final User userGroup = new User();
                userGroup.setUserName(groupName);
                userGroup.setPhoto(photo);
                final Group group = new Group();
                group.setGroupId(groupId);
                group.setCreatedByNumber(createdBy);

                int i = 0;
                int j = 0;
                final RealmList<User> userList = new RealmList<>();
                String lastUserId = null;

                //get last userId
                for (DataSnapshot snapshot : users.getChildren()) {
                    lastUserId = snapshot.getKey();
                    if (j == 6) {
                        break;
                    }
                    j++;
                }


                for (DataSnapshot snapshot : users.getChildren()) {
                    //getting only 6 members
                    if (i == 6) {
                        break;
                    }
                    i++;

                    String uid = snapshot.getKey();
                    final String finalLastUserId = lastUserId;


                    //if the user is not stored in Realm fetch his data and save it
                    User user = RealmHelper.getInstance().getUser(uid);
                    if (user != null) {
                        userList.add(user);
                        if (finalLastUserId.equals(user.getUid())) {
                            if (callback != null) {
                                group.setUsers(userList);
                                userGroup.setGroup(group);
                                callback.onComplete(userGroup, usersInGroupCount);
                            }
                        }
                    } else {
                        //Fetch user data if not exists
                        FireManager.fetchUserByUid(context, uid, new FireManager.IsHasAppListener() {
                            @Override
                            public void onFound(User user) {
                                if (finalLastUserId.equals(user.getUid())) {
                                    if (callback != null) {
                                        group.setUsers(userList);
                                        userGroup.setGroup(group);
                                        callback.onComplete(userGroup, usersInGroupCount);
                                    }
                                }
                            }

                            @Override
                            public void onNotFound() {
                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null)
                    callback.onFailed();
            }


        });
    }

    public interface FetchPartialGroupInfoCallback {
        void onComplete(User user, int usersCount);

        void onFailed();
    }

    public interface IsUserBannedCallback {
        void onComplete(boolean isBanned);

        void onFailed();
    }

    public interface OnFetchGroupsComplete {
        void onComplete(String groupId);
    }

    public interface OnCreateGroupComplete {
        void onComplete(boolean isSuccessful, String groupId);
    }

    public interface OnComplete {
        void onComplete(boolean isSuccess);
    }


}
