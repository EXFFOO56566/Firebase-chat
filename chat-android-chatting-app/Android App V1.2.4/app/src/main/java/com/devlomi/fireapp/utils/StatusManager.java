package com.devlomi.fireapp.utils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.devlomi.fireapp.job.DeleteStatusJob;
import com.devlomi.fireapp.model.TextStatus;
import com.devlomi.fireapp.model.constants.StatusType;
import com.devlomi.fireapp.model.realms.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatusManager {
    private static List<String> currentDownloadStatusOperations = new ArrayList<>();

    public static void downloadVideoStatus(final String id, String url, final File file, final OnStatusDownloadComplete onComplete) {
        //prevent duplicates download
        if (currentDownloadStatusOperations.contains(id))
            return;

        currentDownloadStatusOperations.add(id);

        FireConstants.storageRef.child(url)
                .getFile(file)
                .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (currentDownloadStatusOperations.contains(id))
                            currentDownloadStatusOperations.remove(currentDownloadStatusOperations);

                        if (task.isSuccessful()) {
                            RealmHelper.getInstance().setLocalPathForVideoStatus(id, file.getPath());
                            if (onComplete != null)
                                onComplete.onComplete(file.getPath());
                        } else {
                            if (onComplete != null)
                                onComplete.onComplete(null);
                        }
                    }
                });
    }


    public static void deleteStatus(final String statusId, int statusType, final DeleteStatus onComplete) {
        FireConstants.getMyStatusRef(statusType).child(statusId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null) {
                    onComplete.onComplete(task.isSuccessful(), statusId);
                }
                if (task.isSuccessful()) {
                    RealmHelper.getInstance().deleteStatus(FireManager.getUid(), statusId);
                }
            }
        });
    }


    public static void uploadStatus(final String filePath, int statusType, final boolean isVideo, final UploadStatusCallback uploadStatusCallback) {


        final String fileName = Util.getFileNameFromPath(filePath);

        final Status status;
        if (isVideo)
            status = StatusCreator.createVideoStatus(filePath);
        else
            status = StatusCreator.createImageStatus(filePath);


        FireManager.getRef(FireManager.STATUS_TYPE, fileName).putFile(Uri.fromFile(new File(filePath))).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> uploadTask) {
                if (uploadTask.isSuccessful()) {
                    if (!isVideo) {
                        uploadTask.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri uri = task.getResult().normalizeScheme();
                                    status.setContent(String.valueOf(uri));
                                    FireConstants.getMyStatusRef(statusType).child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                                                DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                                            }

                                            uploadStatusCallback.onComplete(task.isSuccessful());
                                        }
                                    });
                                } else {
                                    uploadStatusCallback.onComplete(false);
                                }
                            }
                        });

                    } else {
                        final String filePathBucket = uploadTask.getResult().getStorage().getPath();
                        status.setContent(String.valueOf(filePathBucket));
                        FireConstants.getMyStatusRef(statusType).child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                                    DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                                }
                                uploadStatusCallback.onComplete(task.isSuccessful());

                            }
                        });
                    }
                } else {
                    if (uploadStatusCallback != null)
                        uploadStatusCallback.onComplete(false);
                }
            }
        });
    }

    public static void uploadTextStatus(TextStatus textStatus, final UploadStatusCallback uploadStatusCallback) {

        Status status = StatusCreator.createTextStatus(textStatus);
        FireConstants.getMyStatusRef(StatusType.TEXT).child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                    DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                }
                uploadStatusCallback.onComplete(task.isSuccessful());

            }
        });

    }


    public interface DeleteStatus {
        void onComplete(boolean isSuccessful, String id);
    }

    public interface UploadStatusCallback {
        void onComplete(boolean isSuccessful);
    }

    public interface OnStatusDownloadComplete {
        void onComplete(String path);
    }
}
