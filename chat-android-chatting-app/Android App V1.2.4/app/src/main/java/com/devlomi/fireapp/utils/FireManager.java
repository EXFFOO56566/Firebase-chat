package com.devlomi.fireapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.devlomi.fireapp.events.UserImageDownloadedEvent;
import com.devlomi.fireapp.model.constants.LastSeenStates;
import com.devlomi.fireapp.model.constants.MessageStat;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.RealmResults;

/**
 * Created by Devlomi on 01/08/2017.
 */

public class FireManager {

    public static final int STATUS_TYPE = 8888;
    //every user image download request will saved here to prevent download the same image over and over
    private static List<String> imageDownloadProcessIds = new ArrayList<>();

    //is this user is logged in
    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    //get this user's uid
    public static String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            return FirebaseAuth.getInstance().getCurrentUser().getUid();

        return null;
    }

    public static boolean isAdmin(List<String> adminUids) {
        return adminUids.contains(FireManager.getUid());
    }

    public static boolean isAdmin(String adminUid, List<String> adminUids) {
        return adminUids.contains(adminUid);
    }

    //get this user's phone number
    public static String getPhoneNumber() {
        return FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    }

    OnComplete onComplete;

    public FireManager(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    //check if this user has installed this app and return user object if user is exists
    public static void isHasFireApp(String phone, final IsHasAppListener listener) {
        //check if the number contains denied characters
        if (isHasDeniedFirebaseStrings(phone)) return;

        //get phone number and start searching for this phone number
        DatabaseReference query = FireConstants.uidByPhone.child(phone);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //user not exists
                if (dataSnapshot.getValue() == null) {
                    listener.onNotFound();
                } else {
                    if (dataSnapshot.getValue() instanceof Map) {
                        listener.onNotFound();
                        return;
                    }

                    //get user uid to get the user uid since they are in different nodes
                    final String uid = dataSnapshot.getValue(String.class);
                    //start getting user info
                    FireConstants.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                //get user data
                                User user = dataSnapshot.getValue(User.class);
                                //set user uid
                                user.setUid(dataSnapshot.getRef().getKey());


                                listener.onFound(user);
                            } else {
                                listener.onNotFound();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onNotFound();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onNotFound();
            }
        });

    }

    public static void fetchUserByUid(final Context context, String uid, final IsHasAppListener listener) {
        FireConstants.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //get user data
                    User user = dataSnapshot.getValue(User.class);
                    //set user uid
                    user.setUid(dataSnapshot.getRef().getKey());
                    user.setUserName(ContactUtils.queryForNameByNumber(context, user.getPhone()));
                    user.setStoredInContacts(ContactUtils.contactExists(context, user.getPhone()));

                    RealmHelper.getInstance().saveObjectToRealm(user);
                    listener.onFound(user);
                } else {
                    listener.onNotFound();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onNotFound();
            }
        });
    }

    //set the current presence as Online
    public static void setOnlineStatus() {
        FireConstants.presenceRef.child(getUid()).setValue("Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SharedPreferencesManager.setLastSeenState(LastSeenStates.ONLINE);
            }
        });
    }


    public static void downloadUserPhoto(final String photo, final String uid, final String oldLocalPath, final OnUpdateUserPhoto onUpdateUserPhoto) {
        if (photo == null || imageDownloadProcessIds.contains(uid)) return;


        StorageReference referenceFromUrl = FirebaseStorage.getInstance().getReferenceFromUrl(photo);
        //generate new file in profile images directory
        final File imagePath = DirManager.generateUserProfileImage();
        //save the process id to prevent run the same process again
        imageDownloadProcessIds.add(uid);

        referenceFromUrl.getFile(imagePath).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                //remove process id
                imageDownloadProcessIds.remove(uid);
                if (task.isSuccessful()) {
                    //update activity UI
                    postUserImageDownloaded(imagePath.getPath());
                    //save user image to realm if it's not the same
                    RealmHelper.getInstance().updateUserImg(uid, photo, imagePath.getPath(), oldLocalPath);
                    //handleNewMessage callback
                    if (onUpdateUserPhoto != null)
                        onUpdateUserPhoto.onSuccess(imagePath.getPath());
                }
            }
        });

    }

    public static void downloadUserPhoto(final String uid, final String oldLocalPath, boolean isGroup, final OnUpdateUserPhoto onUpdateUserPhoto) {
        if (imageDownloadProcessIds.contains(uid))
            return;

        imageDownloadProcessIds.add(uid);
        DatabaseReference ref = isGroup ? FireConstants.groupsRef.child(uid).child("info") : FireConstants.usersRef.child(uid);
        ref.child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String photo = dataSnapshot.getValue(String.class);
                StorageReference referenceFromUrl = FirebaseStorage.getInstance().getReferenceFromUrl(photo);
                final File imagePath = DirManager.generateUserProfileImage();
                referenceFromUrl.getFile(imagePath).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        imageDownloadProcessIds.remove(uid);
                        if (task.isSuccessful()) {
                            postUserImageDownloaded(imagePath.getPath());
                            RealmHelper.getInstance().updateUserImg(uid, photo, imagePath.getPath(), oldLocalPath);
                            if (onUpdateUserPhoto != null)
                                onUpdateUserPhoto.onSuccess(imagePath.getPath());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //update user text status
    public static void updateMyStatus(String status, final OnComplete onComplete) {
        FireConstants.usersRef.child(FireManager.getUid()).child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null)
                    onComplete.onComplete(task.isSuccessful());
            }
        });
    }

    //update user username
    public static void updateMyUserName(String username, final OnComplete onComplete) {
        FireConstants.usersRef.child(FireManager.getUid()).child("name").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null)
                    onComplete.onComplete(task.isSuccessful());
            }
        });
    }

    //this will upload the user photo that he picked and generate a Small circle image and decode as base64
    public static void updateMyPhoto(final String imagePath, final OnComplete onComplete) {
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


                        SharedPreferencesManager.saveMyThumbImg(decodedImage);

                        //add the photo to the map
                        updateMap.put("photo", downloadUrl);
                        //add the thumb circle image to the map
                        updateMap.put("thumbImg", decodedImage);

                        //save them in firebase database using one request
                        FireConstants.usersRef.child(FireManager.getUid()).updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (onComplete != null)
                                    onComplete.onComplete(task.isSuccessful());
                            }
                        });
                    }
                });
    }


    public static void setUserBlocked(String uid, String receiverUid, boolean setBlocked, final OnComplete onComplete) {
        OnCompleteListener onCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (onComplete != null)
                    onComplete.onComplete(task.isSuccessful());
            }
        };
        if (setBlocked) {
            FireConstants.blockedUsersRef.child(uid).child(receiverUid).setValue(true).addOnCompleteListener(onCompleteListener);
        } else {
            FireConstants.blockedUsersRef.child(uid).child(receiverUid).removeValue().addOnCompleteListener(onCompleteListener);
        }
    }

    //set all unread messages in a Chat as Read
    public static void setMessagesAsRead(Context context, String chatId) {
        //get unread messages
        RealmResults<Message> results = RealmHelper.getInstance().getUnReadIncomingMessages(chatId);
        for (final Message message : results) {
            ServiceHelper.startUpdateMessageStatRequest(context, message.getMessageId(), FireManager.getUid(), chatId, MessageStat.READ);
        }
    }


    //set last seen value,this will set value at the Server Time
    //so if the device clock is not correct it will not affect the last seen value
    public static void setLastSeen() {
        FireConstants.presenceRef.child(getUid()).setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SharedPreferencesManager.setLastSeenState(LastSeenStates.LAST_SEEN);
            }
        });
    }


    //set the typing or recording or do nothing state
    public static void setTypingStat(String receiverUid, int stat, boolean isGroup, boolean isBroadcast) {
        if (isBroadcast) return;
        if (isGroup) {
            FireConstants.groupTypingStat.child(receiverUid).child(FireManager.getUid()).setValue(stat);
        } else {
            FireConstants.typingStat.child(receiverUid).setValue(stat);
        }
    }

    //get correct ref for the given type
    public static StorageReference getRef(int type, String fileName) {
        String mName = UUID.randomUUID().toString() + "." + Util.getFileExtensionFromPath(fileName);

        switch (type) {

            case MessageType.SENT_IMAGE:

                return FireConstants.imageRef.child(mName);

            case MessageType.SENT_VIDEO:
                return FireConstants.videoRef.child(mName);

            case MessageType.SENT_VOICE_MESSAGE:
                return FireConstants.voiceRef.child(mName);

            case MessageType.SENT_AUDIO:
                return FireConstants.audioRef.child(mName);

            case STATUS_TYPE:
                return FireConstants.statusStorageRef.child(mName);

        }

        return FireConstants.fileRef.child(mName);

    }


    //update message state as received or read
    public static void updateMessageStat(String myUid, String messageId, int stat, final OnComplete OnComplete) {
        FireConstants.messageStat.child(myUid)
                .child(messageId).setValue(stat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (OnComplete != null)
                    OnComplete.onComplete(task.isSuccessful());
            }

        });
    }

    //update voice message state as Seen
    public static void updateVoiceMessageStat(String myUid, String messageId, final OnComplete onComplete) {
        FireConstants.voiceMessageStat.child(myUid)
                .child(messageId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null)
                    onComplete.onComplete(task.isSuccessful());
            }


        });

    }

    //check if there is a new photo for this user and download it
    //check for both thumb and full photo
    public static void checkAndDownloadUserPhoto(final User user, final OnGetUserPhoto onGetUserPhoto) {
        if (user == null) return;


        DatabaseReference databaseReference = user.isGroupBool() ? FireConstants.groupsRef.child(user.getUid()).child("info") : FireConstants.usersRef.child(user.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;

                final String photo = dataSnapshot.child("photo").getValue(String.class);
                String thumbImg = dataSnapshot.child("thumbImg").getValue(String.class);


                if (user.getThumbImg() == null) {
                    RealmHelper.getInstance().updateThumbImg(user.getUid(), thumbImg);
                    if (onGetUserPhoto != null)
                        onGetUserPhoto.onGetThumb(thumbImg);
                } else if (user.getThumbImg() != null && !user.getThumbImg().equals(thumbImg)) {
                    RealmHelper.getInstance().updateThumbImg(user.getUid(), thumbImg);
                    if (onGetUserPhoto != null)
                        onGetUserPhoto.onGetThumb(thumbImg);

                }

                if (user.getPhoto() != null && !photo.equals(user.getPhoto()) || !FileUtils.isFileExists(user.getUserLocalPhoto())) {
                    FireManager.downloadUserPhoto(photo, user.getUid(), user.getUserLocalPhoto(), new FireManager.OnUpdateUserPhoto() {
                        @Override
                        public void onSuccess(String photoPath) {
                            if (onGetUserPhoto != null)
                                onGetUserPhoto.onGetPhoto(photoPath);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //check only for thumb img
    public static void checkAndDownloadUserPhoto(final User user, final OnGetUserThumbImg onGetUserThumbImg) {
        if (user == null) return;

        DatabaseReference databaseReference = user.isGroupBool()
                ? FireConstants.groupsRef.child(user.getUid()).child("info")
                : FireConstants.usersRef.child(user.getUid());
        databaseReference.child("thumbImg").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;


                String thumbImg = dataSnapshot.getValue(String.class);

                if (user.getThumbImg() == null) {
                    RealmHelper.getInstance().updateThumbImg(user.getUid(), thumbImg);
                    if (onGetUserThumbImg != null)
                        onGetUserThumbImg.onGetThumb(thumbImg);
                } else if (user.getThumbImg() != null && !user.getThumbImg().equals(thumbImg)) {
                    RealmHelper.getInstance().updateThumbImg(user.getUid(), thumbImg);
                    if (onGetUserThumbImg != null)
                        onGetUserThumbImg.onGetThumb(thumbImg);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private static void postUserImageDownloaded(String path) {
        EventBus.getDefault().post(new UserImageDownloadedEvent(path));
    }

    //fix for com.google.firebase.database.DatabaseException: Invalid Firebase Database path: #21#.
    // Firebase Database paths must not contain '.', '#', '$', '[', or ']'
    //if a phone number contains one of these characters we will skip this number since it's not a Phone Number
    private static String[] deniedFirebaseStrings = new String[]{".", "#", "$", "[", "]"};

    //will check if phone number has one of these strings
    private static boolean isHasDeniedFirebaseStrings(String deniedString) {
        for (String deniedFirebaseString : deniedFirebaseStrings) {
            if (deniedString.contains(deniedFirebaseString)) {
                return true;
            }
        }
        return false;
    }


    public static void isCallCancelled(String userId, String callId, final IsCallCancelled isCallCancelled) {
        FireConstants.callsRef.child(FireManager.getUid()).child(userId).child(callId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    if (isCallCancelled != null)
                        isCallCancelled.isCancelled(false);
                } else {
                    if (isCallCancelled != null)
                        isCallCancelled.isCancelled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isCallCancelled != null)
                    isCallCancelled.isCancelled(true);
            }
        });

    }

    public static void setCallCancelled(String userId, String callId) {
        FireConstants.callsRef.child(FireManager.getUid()).child(userId).child(callId).setValue(true);
    }

    public interface OnComplete {
        void onComplete(boolean isSuccessful);
    }

    public interface IsCallCancelled {
        void isCancelled(boolean isCancelled);
    }


    public interface OnUpdateUserPhoto {
        void onSuccess(String photoPath);
    }

    public interface OnGetUserPhoto {
        void onGetPhoto(String photoPath);

        void onGetThumb(String thumbImg);
    }


    public interface OnGetUserThumbImg {
        void onGetThumb(String thumbImg);
    }


    public interface IsHasAppListener {
        void onFound(User user);

        void onNotFound();
    }

    //get user info when he messages the user
    public static void fetchUserDataAndSaveIt(final Context context, final String phoneNumber) {

        if (phoneNumber == null)
            return;

        FireManager.isHasFireApp(phoneNumber, new FireManager.IsHasAppListener() {
            @Override
            public void onFound(User user) {
                user.setUserName(ContactUtils.queryForNameByNumber(context, phoneNumber));
                user.setStoredInContacts(ContactUtils.contactExists(context, user.getPhone()));
                //save it to realm
                RealmHelper.getInstance().saveObjectToRealm(user);
            }

            @Override
            public void onNotFound() {
            }
        });

    }


    public static void getServerTime(final OnGetServerTime onComplete) {
        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()) {
                            long timestamp = (long) task.getResult().getData();
                            if (onComplete != null) {
                                onComplete.onSuccess(timestamp);
                            }
                        } else {
                            onComplete.onFailed();
                        }
                    }
                });

    }


    public static void isUserBlocked(String otherUserUid, final UserBlockedCallback userBlockedCallback) {
        FireConstants.blockedUsersRef.child(otherUserUid).child(FireManager.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    userBlockedCallback.isBlocked(false);
                else
                    userBlockedCallback.isBlocked(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userBlockedCallback.isBlocked(true);
            }
        });
    }

    public interface UserBlockedCallback {
        void isBlocked(boolean isBlocked);
    }

    public interface OnGetServerTime {
        void onSuccess(long timestamp);

        void onFailed();
    }


}
