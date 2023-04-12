package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.ONE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_OFFLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bytesbee.firebase.chat.activities.fcm.ApplicationLifecycleManager;
import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class UIApplication extends Application implements DefaultLifecycleObserver {
    @Override
    public void onCreate() {
        super.onCreate();

        SessionManager.init(this);

        MobileAds.initialize(this, initializationStatus -> {
        });

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EmojiManager.install(new IosEmojiProvider());
        registerActivityLifecycleCallbacks(new ApplicationLifecycleManager());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onResume(LifecycleOwner owner) {
//        App visible/foreground
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            try {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> Utils.readStatus(STATUS_ONLINE), ONE);
            } catch (Exception ignored) {
            }

        }
    }

    @Override
    public void onPause(LifecycleOwner owner) {
//        App in background
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.STARTED) {
            try {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> Utils.readStatus(STATUS_OFFLINE), ONE);
            } catch (Exception ignored) {
            }
        }
    }
}
