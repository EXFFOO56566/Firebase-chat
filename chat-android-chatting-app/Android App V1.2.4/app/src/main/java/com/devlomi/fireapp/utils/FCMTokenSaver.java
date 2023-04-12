package com.devlomi.fireapp.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class FCMTokenSaver {

    public interface OnComplete {
        void onComplete(boolean isSuccess);
    }

    private interface OnGetToken {
        void onSuccess(String token);

        void onFailed();
    }

    OnComplete onComplete;

    public FCMTokenSaver(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    //get token from firebase FCM
    private void getToken(final OnGetToken onGetToken) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    onGetToken.onSuccess(task.getResult().getToken());
                } else {
                    onGetToken.onFailed();
                }

            }
        });

    }

    //this will check if incoming token is null,that means to generate a new token
    //otherwise the token is coming from onNewToken and therefore just save it to database
    public void saveTokenToFirebase(String token) {
        if (token == null) {
            getToken(new OnGetToken() {
                @Override
                public void onSuccess(String token) {
                    saveToken(token);
                }

                @Override
                public void onFailed() {
                    notifyOnComplete(false);
                }
            });
        } else {
            saveToken(token);
        }
    }

    private void saveToken(String token) {
        if (FireManager.getUid() != null) {
            FireConstants.usersRef.child(FireManager.getUid()).child("notificationTokens")
                    .child(token)
                    .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SharedPreferencesManager.setTokenSaved(true);
                    } else {
                        SharedPreferencesManager.setTokenSaved(false);
                        Log.e("Registration Service", "Response : Send Token Failed");

                    }
                    notifyOnComplete(task.isSuccessful());

                }

            });
        }
    }

    private void notifyOnComplete(boolean isSuccess) {
        if (onComplete != null)
            onComplete.onComplete(isSuccess);
    }
}
