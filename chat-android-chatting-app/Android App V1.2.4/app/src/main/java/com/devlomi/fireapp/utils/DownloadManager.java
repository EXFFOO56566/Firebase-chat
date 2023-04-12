package com.devlomi.fireapp.utils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.devlomi.fireapp.events.OnNetworkComplete;
import com.devlomi.fireapp.events.UpdateNetworkProgress;
import com.devlomi.fireapp.job.NetworkJobService;
import com.devlomi.fireapp.model.ProgressData;
import com.devlomi.fireapp.model.constants.DownloadUploadStat;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;

import static com.devlomi.fireapp.model.constants.MessageStat.SENT;

/**
 * Created by Devlomi on 06/01/2018.
 */

//this class is responsible for making upload/download files from Firebase Storage
//it's also responsible for saving messages in database
public class DownloadManager {
    //save the file download task to cancel it later if user wants to
    public static HashMap<String, FileDownloadTask> downloadTaskHashmap = new HashMap<>();
    //save the file upload task to cancel it later if user wants to
    public static HashMap<String, UploadTask> uploadTaskHashMap = new HashMap<>();


    //used in activity to get the current progress
    public static HashMap<String, ProgressData> progressDataHashMap = new HashMap<>();


    //download from firebase storage
    public static void download(final Message message, final OnComplete onComplete) {
        final int type = message.getType();
        final String link = message.getContent();
        final String messageId = message.getMessageId();
        final String receiverId = message.getChatId();


        final File file;


        if (link == null || link.equals("")) return;

        //generate file in the correct directory
        if (type == MessageType.RECEIVED_FILE)
            file = DirManager.generateFileForFilesType(type, Util.getFileNameFromPath(link));
        else if (type == MessageType.RECEIVED_AUDIO)
            file = DirManager.generateAudioFile(type, Util.getFileExtensionFromPath(link));
        else
            file = DirManager.generateFile(type);


        //get firebase storage ref
        StorageReference ref = FireConstants.storageRef.child(link);
        setMessageContent(link, message);


        //get download task
        FileDownloadTask task = ref.getFile(file);

        //save task to hashmap
        fillTaskHashmap(messageId, task);


        //listen for progress
        task.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progressDouble = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                //get progress
                int progress = (int) progressDouble;
                //save progress to hashmap
                fillProgressHashmap(messageId, receiverId, progress);

                //update activity with the progress
                updateProgress(messageId, progress);

            }
        }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

                //update activity UI
                DownloadManager.onComplete(messageId);

                removeTaskFromHashmap(messageId);
                removeProgressFromHashmap(messageId);

                //if download completed successfully and the user did not cancel the process


                if (task.isSuccessful() && message.completeAfterDownload()) {

                    // if it's a video generate the video thumb (without blur)
                    if (MessageType.isVideo(type)) {
                        String videoThumb = BitmapUtils.generateVideoThumbAsBase64(file.getPath());
                        RealmHelper.getInstance().setVideoThumb(messageId, message.getChatId(), videoThumb);
                    }
                    //update downloadupload state to success
                    RealmHelper.getInstance().updateDownloadUploadStat(messageId, DownloadUploadStat.SUCCESS, file.getPath());

                    updateJobCallback(true, onComplete);


                } else {
                    StorageException exception = (StorageException) task.getException();
                    //if this process was not cancelled by the user (a network failure for example) then set the state as failed
                    if (exception != null && exception.getErrorCode() != StorageException.ERROR_CANCELED) {
                        RealmHelper.getInstance().changeDownloadOrUploadStat(messageId, DownloadUploadStat.FAILED);
                        updateJobCallback(false, onComplete);
                    } else {
                        updateJobCallback(true, onComplete);
                    }
                    //delete uncompleted file from device
                    FileUtils.deleteFile(file.getPath());

                }
            }
        });
    }

    private static void updateJobCallback(boolean isSuccess, OnComplete onComplete) {
        if (onComplete != null)
            onComplete.onComplete(isSuccess);
    }


    private static void fillProgressHashmap(String messageId, String receiverId, int progress) {
        ProgressData progressData = new ProgressData(progress, receiverId, messageId);
        progressDataHashMap.put(messageId, progressData);
    }

    private static void removeProgressFromHashmap(String messageId) {
        if (progressDataHashMap.containsKey(messageId))
            progressDataHashMap.remove(messageId);


    }

    private static void fillTaskHashmap(String messageId, FileDownloadTask downloadTask) {
        downloadTaskHashmap.put(messageId, downloadTask);
    }


    private static void fillTaskHashmap(String messageId, UploadTask uploadTask) {
        uploadTaskHashMap.put(messageId, uploadTask);
    }

    private static void removeTaskFromHashmap(String messageId) {
        if (uploadTaskHashMap.containsKey(messageId))
            uploadTaskHashMap.remove(messageId);


        if (downloadTaskHashmap.containsKey(messageId))
            downloadTaskHashmap.remove(messageId);

    }


    public static void cancelDownload(Message message) {

        String messageId = message.getMessageId();
        if (downloadTaskHashmap.containsKey(messageId)) {
            FileDownloadTask fileDownloadTask = downloadTaskHashmap.get(messageId);
            fileDownloadTask.cancel();
            downloadTaskHashmap.remove(messageId);
            FileUtils.deleteFile(message.getLocalPath());
        }

        removeProgressFromHashmap(messageId);
        RealmHelper.getInstance().changeDownloadOrUploadStat(messageId, DownloadUploadStat.CANCELLED);
        if (Util.isOreoOrAbove()) {
            NetworkJobService.cancel(messageId);
        }


    }

    public static void cancelDownload(String messageId) {
        Message message = RealmHelper.getInstance().getMessage(messageId);
        if (downloadTaskHashmap.containsKey(messageId)) {
            FileDownloadTask fileDownloadTask = downloadTaskHashmap.get(messageId);
            fileDownloadTask.cancel();
            downloadTaskHashmap.remove(messageId);
            FileUtils.deleteFile(message.getLocalPath());
        }


        removeProgressFromHashmap(messageId);
        RealmHelper.getInstance().changeDownloadOrUploadStat(messageId, DownloadUploadStat.CANCELLED);
        if (Util.isOreoOrAbove()) {
            NetworkJobService.cancel(messageId);
        }


    }


    public static void cancelUpload(Message message) {
        String messageId = message.getMessageId();

        if (uploadTaskHashMap.containsKey(messageId)) {
            UploadTask uploadTask = uploadTaskHashMap.get(messageId);
            uploadTask.cancel();
            uploadTaskHashMap.remove(messageId);
        }

        removeProgressFromHashmap(messageId);
        RealmHelper.getInstance().changeDownloadOrUploadStat(messageId, DownloadUploadStat.CANCELLED);

        if (Util.isOreoOrAbove()) {
            NetworkJobService.cancel(messageId);
        }


    }


    public static void cancelUpload(String messageId) {
        if (uploadTaskHashMap.containsKey(messageId)) {
            UploadTask uploadTask = uploadTaskHashMap.get(messageId);
            uploadTask.cancel();
            uploadTaskHashMap.remove(messageId);
        }

        removeProgressFromHashmap(messageId);
        RealmHelper.getInstance().changeDownloadOrUploadStat(messageId, DownloadUploadStat.CANCELLED);

        if (Util.isOreoOrAbove()) {
            NetworkJobService.cancel(messageId);
        }

    }

    private static void updateProgress(String id, int progress) {
        EventBus.getDefault().post(new UpdateNetworkProgress(id, progress));
    }

    private static void onComplete(String id) {
        EventBus.getDefault().post(new OnNetworkComplete(id));
    }


    //send a message to Firebase database
    public static void sendMessage(final Message message, final OnComplete onComplete) {
        final String pushKey = message.getMessageId();

        //convert message object to a Map
        Map<String, Object> postValues = message.toMap();

        final Map<String, Object> childUpdates = new HashMap<>();
        //add message id and the map
        childUpdates.put(pushKey, postValues);

        //send the message to firebase database
        FireConstants.getMessageRef(message.isGroup(), message.isBroadcast(), message.getChatId()).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //update message state to SENT if it's success
                if (task.isSuccessful()) {
                    // if it's a broadcast update all copied messages
                    if (message.isBroadcast()) {
                        RealmResults<Message> broadcastedMessages = RealmHelper.getInstance().getMessages(message.getMessageId());
                        for (Message broadcastedMessage : broadcastedMessages) {
                            RealmHelper.getInstance().updateMessageStatLocally(broadcastedMessage.getMessageId(), broadcastedMessage.getChatId(), SENT);
                        }
                    } else {
                        RealmHelper.getInstance().updateMessageStatLocally(pushKey, SENT);
                    }

                }


                updateJobCallback(task.isSuccessful(), onComplete);


            }
        });


    }


    private static void upload(final Message message, final OnComplete onComplete) {


        //get file path
        final String filePath = message.getLocalPath();

        final String pushKey = message.getMessageId();
        //get file name from file path
        final String fileName = Util.getFileNameFromPath(filePath);
        //get receiver uid
        final String receiverId = message.getToId();

        //get correct ref in firebase storage folders ,if it's an image it will be saved in images folder
        //if it's a video it will be saved in video folder
        StorageReference ref = FireManager.getRef(message.getType(), fileName);

        UploadTask task = ref.putFile(Uri.fromFile(new File(filePath)));


        fillTaskHashmap(pushKey, task);

        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                try {

                    int bytesTransferred = (int) taskSnapshot.getBytesTransferred();
                    int totalBytes = (int) taskSnapshot.getTotalByteCount();


                    int progress = (100 * bytesTransferred) / totalBytes;
                    fillProgressHashmap(pushKey, receiverId, progress);

                    //update progress in UI
                    updateProgress(pushKey, progress);
                } catch (Exception e) {
                }


            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                //UPDATE UI
                DownloadManager.onComplete(pushKey);

                removeProgressFromHashmap(pushKey);
                removeTaskFromHashmap(pushKey);


                // check if upload is success && the user is not cancelled the upload request
                if (task.isSuccessful() && message.completeAfterDownload()) {

                    //get the firebase folder path to save it locally (used when forwarding a message
                    // so we don't re-upload files to firebase storage
                    String filePathBucket = task.getResult().getStorage().getPath();
                    //save it locally
                    setMessageContent(filePathBucket, message);

                    //convert message to a Map
                    Map<String, Object> postValues = message.toMap();

                    final Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(pushKey, postValues);


                    FireConstants.getMessageRef(message.isGroup(), message.isBroadcast(), message.getChatId()).updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> mTask) {
                            //update download upload state if it's success or not
                            RealmHelper.getInstance().updateDownloadUploadStat(pushKey, mTask.isSuccessful() ? DownloadUploadStat.SUCCESS : DownloadUploadStat.FAILED);
                            updateJobCallback(mTask.isSuccessful(), onComplete);

                        }
                    });

                } else {
                    //if this process was not cancelled by the user (a network failure for example) then set the state as failed
                    StorageException exception = (StorageException) task.getException();
                    if (exception != null && exception.getErrorCode() != StorageException.ERROR_CANCELED) {
                        RealmHelper.getInstance().changeDownloadOrUploadStat(pushKey, DownloadUploadStat.FAILED);
                        updateJobCallback(false, onComplete);
                    } else {
                        updateJobCallback(true, onComplete);

                    }
                }
            }
        });


    }

    //save file link from firebase storage in realm to use it later when forward a message
    private static void setMessageContent(String filePath, Message message) {
        try {
            //save it when the message is not saved to realm yet
            message.setContent(filePath);
            RealmHelper.getInstance().changeMessageContent(message.getMessageId(), filePath);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            //otherwise  the message is exists and update it using transaction
            RealmHelper.getInstance().changeMessageContent(message.getMessageId(), filePath);
        }
    }


    public static void cancelAllTasks() {
        for (String s : downloadTaskHashmap.keySet()) {
            cancelDownload(s)/*second param is just dummy param here :D*/;

        }

        for (String s : uploadTaskHashMap.keySet()) {
            cancelUpload(s)/*second param is just dummy param here :D*/;
        }
    }


    public static void request(Message message, OnComplete onComplete) {

        int type = message.getType();


        if (MessageType.isSentType(type)) {
            switch (type) {
                case MessageType.SENT_TEXT:
                case MessageType.SENT_CONTACT:
                case MessageType.SENT_LOCATION:


                    sendMessage(message, onComplete);

                    break;


                default:
                    if (message.isForwarded()) {
                        sendMessage(message, onComplete);
                    } else
                        upload(message, onComplete);

            }
        } else {
            download(message, onComplete);
        }

    }


    public interface OnComplete {
        void onComplete(boolean isSuccess);
    }


}
