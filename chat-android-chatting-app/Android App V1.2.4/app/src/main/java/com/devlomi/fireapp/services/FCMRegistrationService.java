package com.devlomi.fireapp.services;

import android.app.IntentService;
import android.content.Intent;

import com.devlomi.fireapp.utils.FCMTokenSaver;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

//this service will get the Notification token
//and save it to the Firebase Database
public class FCMRegistrationService extends IntentService {


    public FCMRegistrationService() {
        super("FCM");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get token from Firebase Messaging

        // check if intent is null or not if it's not null we will get refreshed value and
        // if its true we will override token_sent value to false and apply
        if (intent != null && intent.getExtras() != null) {

            String newToken = intent.getStringExtra(IntentUtils.FCM_TOKEN);

            new FCMTokenSaver(new FCMTokenSaver.OnComplete() {
                @Override
                public void onComplete(boolean isSuccess) {
                    stopSelf();
                }
            }).saveTokenToFirebase(newToken);
        } else {
            // if token_sent value is false then use method sendTokenToServer to send token to server
            if (!SharedPreferencesManager.isTokenSaved()) {
                new FCMTokenSaver(new FCMTokenSaver.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        stopSelf();
                    }
                }).saveTokenToFirebase(null);


            }
        }


    }
}





