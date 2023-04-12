package com.devlomi.fireapp.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.job.FireJobCreator;
import com.evernote.android.job.JobManager;
import com.google.android.gms.ads.MobileAds;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Devlomi on 13/08/2017.
 */

public class MyApp extends Application {
    private static MyApp mApp = null;
    private static String currentChatId = "";
    private static boolean chatActivityVisible;
    private static boolean phoneCallActivityVisible;
    private static boolean baseActivityVisible;
    private static boolean isCallActive = false;

    public static boolean isChatActivityVisible() {
        return chatActivityVisible;
    }

    public static String getCurrentChatId() {
        return currentChatId;
    }

    public static void chatActivityResumed(String chatId) {
        chatActivityVisible = true;
        currentChatId = chatId;
    }

    public static void chatActivityPaused() {
        chatActivityVisible = false;
        currentChatId = "";
    }

    public static boolean isPhoneCallActivityVisible() {
        return phoneCallActivityVisible;
    }

    public static void phoneCallActivityResumed() {
        phoneCallActivityVisible = true;
    }

    public static void phoneCallActivityPaused() {
        phoneCallActivityVisible = false;
    }


    public static boolean isBaseActivityVisible() {
        return baseActivityVisible;
    }

    public static void baseActivityResumed() {
        baseActivityVisible = true;
    }

    public static void baseActivityPaused() {
        baseActivityVisible = false;
    }


    public static void setCallActive(boolean mCallActive) {
        isCallActive = mCallActive;
    }

    public static boolean isIsCallActive() {
        return isCallActive;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //add support for vector drawables on older APIs
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //init realm
        Realm.init(this);
        //init set realm configs
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(MyMigration.SCHEMA_VERSION)
                .migration(new MyMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        //init shared prefs manager
        SharedPreferencesManager.init(this);
        //init evernote job
        JobManager.create(this).addJobCreator(new FireJobCreator());


        //initialize ads for faster loading in first time
        if (getResources().getBoolean(R.bool.are_ads_enabled))
            MobileAds.initialize(this, getString(R.string.admob_app_id));

        mApp = this;

    }

    public static Context context() {
        return mApp.getApplicationContext();
    }

    //to run multi dex
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
