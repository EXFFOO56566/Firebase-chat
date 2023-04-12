package com.devlomi.fireapp.utils;

import android.support.annotation.NonNull;

import com.devlomi.fireapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GroupLinkUtil {


    //this will generate only new key without link
    private static String generateNewKey(String groupId) {
        return FireConstants.groupsLinks.child(groupId).push().getKey();
    }

    public static String getFinalLink(String newKey) {
        String host = "http://" + MyApp.context().getString(R.string.group_invite_host);
        return host + "/" + newKey;
    }

    public static void generateLink(final String groupId, final GenerateLinkCallback callback) {
        //generate new key locally
        final String newKey = generateNewKey(groupId);
        //update key to database

        getCurrentLink(groupId, new FetchCurrentLinkCallback() {
            @Override
            public void onFetch(String groupLink) {
                //if there is no previous link then just save the link
                if (groupLink == null) {
                    saveToDatabase(groupId, newKey, callback);
                } else {
                    //delete old link
                    FireConstants.groupsLinks.child(groupLink).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                saveToDatabase(groupId, newKey, callback);
                            } else {
                                if (callback != null) callback.onFailed();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed() {

            }
        });


    }

    private static void saveToDatabase(final String groupId, final String newKey, final GenerateLinkCallback callback) {
        FireConstants.groupLinkById.child(groupId).setValue(newKey)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FireConstants.groupsLinks.child(newKey).setValue(groupId).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    saveLinkToRealm(groupId, newKey);
                                    callback.onGenerate(newKey);

                                } else {
                                    callback.onFailed();
                                }
                            }
                        });

                    }
                });
    }

    private static void saveLinkToRealm(String groupId, String newKey) {
        RealmHelper.getInstance().setGroupLink(groupId, newKey);
    }

    //get current Link of the Group
    private static void getCurrentLink(final String groupId, final FetchCurrentLinkCallback callback) {
        FireConstants.groupLinkById.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    if (callback != null)
                        callback.onFetch(null);
                } else {
                    String groupLink = dataSnapshot.getValue(String.class);
                    if (callback != null)
                        callback.onFetch(groupLink);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onFailed();
            }
        });
    }

    //this will check for a previous link,if exists get it otherwise generate a new one
    public static void getLinkAndFetchNewOneIfNotExists(final String groupId, final FetchCurrentLinkCallback generateLinkCallback) {
        getCurrentLink(groupId, new FetchCurrentLinkCallback() {
            @Override
            public void onFetch(String groupLink) {
                //if there is no group link before create new one
                if (groupLink == null) {
                    generateLink(groupId, new GenerateLinkCallback() {
                        @Override
                        public void onGenerate(String groupLink) {
                            if (generateLinkCallback != null)
                                generateLinkCallback.onFetch(groupLink);
                        }

                        @Override
                        public void onFailed() {
                            if (generateLinkCallback != null)
                                generateLinkCallback.onFailed();
                        }
                    });
                } else {
                    //otherwise get group link
                    saveLinkToRealm(groupId, groupLink);
                    if (generateLinkCallback != null)
                        generateLinkCallback.onFetch(groupLink);


                }
            }

            @Override
            public void onFailed() {

                if (generateLinkCallback != null)
                    generateLinkCallback.onFailed();

            }
        });
    }


    //this will check if group link is valid when user click on a group link
    public static void isGroupLinkValid(String groupLink, final GetGroupByLinkCallback callback) {
        FireConstants.groupsLinks.child(groupLink).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    callback.onError();
                    return;
                }
                String groupId = dataSnapshot.getValue() instanceof String ? ((String) dataSnapshot.getValue()) : null;
                if (groupId == null) {
                    callback.onError();
                    return;
                }
                callback.onFound(groupId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    public interface GetGroupByLinkCallback {
        void onFound(String groupId);

        void onError();
    }

    public interface FetchCurrentLinkCallback {
        void onFetch(String groupLink);

        void onFailed();
    }

    public interface GenerateLinkCallback {
        void onGenerate(String groupLink);

        void onFailed();
    }
}
