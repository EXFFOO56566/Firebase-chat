package com.devlomi.fireapp.utils;

import android.support.annotation.NonNull;

import com.devlomi.fireapp.model.constants.DBConstants;
import com.devlomi.fireapp.model.realms.Broadcast;
import com.devlomi.fireapp.model.realms.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;

public class BroadcastManager {
    //this will create a new group and add users to this group
    public static void createNewBroadcast(final String broadcastName, final List<User> selectedUsers, final OnCreateBroadcastComplete onComplete) {
        //generate broadcastId
        final String broadcastId = FireConstants.broadcastsRef.push().getKey();
        //generate temp group image

        final Map<String, Object> result = new HashMap<>();
        final Map<String, Object> broadcastInfo = new HashMap<>();
        //set timeCreated
        broadcastInfo.put(DBConstants.TIMESTAMP, ServerValue.TIMESTAMP);
        //set whom created the group
        broadcastInfo.put("createdBy", SharedPreferencesManager.getPhoneNumber());
        //set onlyAdminsCanPost by default to false


        Map<String, Object> usersMap = User.toMap(selectedUsers, true);

        broadcastInfo.put("name", broadcastName);
        result.put("info", broadcastInfo);
        result.put("users", usersMap);

        FireConstants.broadcastsRef.child(broadcastId).setValue(result).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    createBroadcastLocally(broadcastName, selectedUsers, broadcastId, new Date().getTime());

                    onComplete.onComplete(true, broadcastId);
                } else {
                    onComplete.onComplete(true, null);
                }

            }
        });


    }

    private static void createBroadcastLocally(String broadcastName, List<User> selectedUsers, String broadcastId, long timestamp) {
        User broadcastUser = new User();
        broadcastUser.setUserName(broadcastName);
        broadcastUser.setStatus("");
        broadcastUser.setPhone("");
        RealmList<User> list = new RealmList();
        list.addAll(selectedUsers);
        Broadcast broadcast = new Broadcast();
        broadcast.setBroadcastId(broadcastId);
        broadcast.setUsers(list);
        broadcast.setTimestamp(timestamp);
        broadcast.setCreatedByNumber(SharedPreferencesManager.getPhoneNumber());
        broadcastUser.setBroadcast(broadcast);
        broadcastUser.setBroadcastBool(true);
        broadcastUser.setUid(broadcastId);
        RealmHelper.getInstance().saveObjectToRealm(broadcastUser);
        RealmHelper.getInstance().saveEmptyChat(broadcastUser);
    }

    public static void deleteBroadcast(final String broadcastId, final OnComplete onComplete) {
        FireConstants.broadcastsRef.child(broadcastId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    RealmHelper.getInstance().deleteBroadcast(broadcastId);
                if (onComplete != null) onComplete.onComplete(task.isSuccessful());
            }
        });
    }

    public static void removeBroadcastMember(final String broadcastId, final String userToDeleteUid, final OnComplete onComplete) {
        FireConstants.broadcastsRef.child(broadcastId).child("users").child(userToDeleteUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    RealmHelper.getInstance().deleteBroadcastMember(broadcastId, userToDeleteUid);
                if (onComplete != null) onComplete.onComplete(task.isSuccessful());
            }
        });
    }

    public static void addParticipant(final String broadcastId, final ArrayList<User> selectedUsers, final OnComplete onComplete) {
        Map<String, Object> map = new HashMap<>();
        for (User selectedUser : selectedUsers) {
            map.put(selectedUser.getUid(), false);
        }
        FireConstants.broadcastsRef.child(broadcastId).child("users").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    for (User selectedUser : selectedUsers) {
                        RealmHelper.getInstance().addUserToBroadcast(broadcastId, selectedUser);
                    }
                }
                if (onComplete != null)
                    onComplete.onComplete(task.isSuccessful());
            }
        });
    }

    public static void changeBroadcastName(final String broadcastId, final String newTitle, final OnComplete onComplete) {
        FireConstants.broadcastsRef.child(broadcastId).child("info").child("name").setValue(newTitle).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null) onComplete.onComplete(task.isSuccessful());
                if (task.isSuccessful()) {
                    RealmHelper.getInstance().changeBroadcastName(broadcastId, newTitle);
                }
            }
        });
    }


    public static void fetchBroadcast(final String broadcastId, final OnFetchBroadcastComplete onComplete) {
        //get only broadcasts that created by this user
        FireConstants.broadcastsRef.child(broadcastId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) onComplete.onComplete(true, "");

                DataSnapshot info = dataSnapshot.child("info");
                DataSnapshot usersSnapshot = dataSnapshot.child("users");


                final String broadcastName = info.child("name").getValue(String.class);
                final long timestamp = info.child("timestamp").getValue(Long.class);

                List<String> broadcastUserIds = getBroadcastUsersIds(usersSnapshot);
                final String lastUserId = getLastUserId(broadcastUserIds);

                final RealmList<User> users = new RealmList<>();
                final RealmHelper instance = RealmHelper.getInstance();
                for (String userId : broadcastUserIds) {
                    User user = instance.getUser(userId);
                    if (user != null) {
                        users.add(user);
                        if (lastUserId.equals(user.getUid())) {
                            createBroadcastLocally(broadcastName, users, broadcastId, timestamp);
                            if (onComplete != null) onComplete.onComplete(true, broadcastId);
                        }
                    } else {
                        FireManager.fetchUserByUid(MyApp.context(), userId, new FireManager.IsHasAppListener() {
                            @Override
                            public void onFound(User user) {
                                instance.saveObjectToRealm(user);
                                users.add(user);
                                if (lastUserId.equals(user.getUid())) {
                                    createBroadcastLocally(broadcastName, users, broadcastId, timestamp);
                                    if (onComplete != null)
                                        onComplete.onComplete(true, broadcastId);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (onComplete != null) onComplete.onComplete(false, "");
            }
        });
    }

    private static String getLastUserId(List<String> broadcastUserIds) {
        String lastUserId = "";
        for (String broadcastUserId : broadcastUserIds) {
            lastUserId = broadcastUserId;
        }
        return lastUserId;
    }

    private static List<String> getBroadcastUsersIds(DataSnapshot usersSnapshot) {
        List<String> uids = new ArrayList<>();
        for (DataSnapshot child : usersSnapshot.getChildren()) {
            String uid = child.getKey();
            //don't add current user to broadcast
            if (!uid.equals(FireManager.getUid())) {
                uids.add(uid);
            }

        }
        return uids;
    }


    public static void fetchBroadcasts(String uid, final OnComplete onComplete) {
        //get only broadcasts that created by this user
        FireConstants.broadcastsByUser.child(uid).orderByValue().equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    if (onComplete != null) onComplete.onComplete(true);
                } else {
                    final String lastKey = getLastKeyInSnapshot(dataSnapshot);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String broadcastId = snapshot.getKey();
                        fetchBroadcast(broadcastId, new OnFetchBroadcastComplete() {
                            @Override
                            public void onComplete(boolean isSuccessful, String broadcastId) {
                                if (lastKey.equals(broadcastId)) {
                                    if (onComplete != null) onComplete.onComplete(isSuccessful);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (onComplete != null) onComplete.onComplete(false);
            }
        });
    }

    private static String getLastKeyInSnapshot(DataSnapshot dataSnapshot) {
        String lastSnapshotKey = "";
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            lastSnapshotKey = snapshot.getKey();
        }
        return lastSnapshotKey;
    }

    public interface OnCreateBroadcastComplete {
        void onComplete(boolean isSuccessful, String broadcastId);
    }

    public interface OnFetchBroadcastComplete {
        void onComplete(boolean isSuccessful, String broadcastId);
    }

    public interface OnComplete {
        void onComplete(boolean isSuccessful);
    }
}
