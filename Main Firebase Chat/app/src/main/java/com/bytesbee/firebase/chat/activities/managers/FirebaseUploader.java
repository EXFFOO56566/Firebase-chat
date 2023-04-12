package com.bytesbee.firebase.chat.activities.managers;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.bytesbee.firebase.chat.activities.async.BaseTask;
import com.bytesbee.firebase.chat.activities.async.TaskRunner;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FirebaseUploader {
    private final UploadListener uploadListener;
    private UploadTask uploadTask;

    private final StorageReference uploadRef;
    private Uri fileUri;

    public FirebaseUploader(final StorageReference storageReference, final UploadListener uploadListener) {
        this.uploadRef = storageReference;
        this.uploadListener = uploadListener;
    }

    public void uploadFile(final File file) {
        final BaseTask baseTask = new BaseTask() {
            @Override
            public void setUiForLoading() {
                super.setUiForLoading();
            }

            @Override
            public Object call() {
                try {
                    fileUri = Uri.fromFile(file);
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
                checkIfExists();
                return "";
            }

            @Override
            public void setDataAfterLoading(Object result) {

            }

        };

        TaskRunner runner = new TaskRunner();
        runner.executeAsync(baseTask);
    }

    private void checkIfExists() {
        uploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                uploadListener.onUploadSuccess(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                upload();
            }
        });
    }

    private void upload() {
        Utils.sout("Upload fileURI:::: " + fileUri);
        uploadTask = uploadRef.putFile(fileUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return uploadRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadListener.onUploadSuccess(downloadUri.toString());
                } else {
                    uploadListener.onUploadFail(task.getException().getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadListener.onUploadFail(e.getMessage());
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NotNull UploadTask.TaskSnapshot taskSnapshot) {
                long progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                uploadListener.onUploadProgress((int) progress);
            }
        });
    }

    public void cancelUpload() {
        if (uploadTask != null && uploadTask.isInProgress()) {
            uploadTask.cancel();
            uploadListener.onUploadCancelled();
        }
    }

    public interface UploadListener {
        void onUploadFail(String message);

        void onUploadSuccess(String downloadUrl);

        void onUploadProgress(int progress);

        void onUploadCancelled();
    }
}
