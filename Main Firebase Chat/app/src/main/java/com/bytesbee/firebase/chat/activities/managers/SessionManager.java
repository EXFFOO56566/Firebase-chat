package com.bytesbee.firebase.chat.activities.managers;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * @author : Prashant Adesara
 * @url https://www.bytesbee.com
 * Manage the data into local storage via SharedPreference
 */
public class SessionManager {
    // Shared preferences file name
    private static final String PREF_NAME = "BytesBeeChatV1";
    private static final String KEY_ON_OFF_NOTIFICATION = "onOffNotification";
    private static final String KEY_ON_OFF_RTL = "onOffRTL";
    private static final String KEY_ONBOARDING = "isOnBoardingDone";
    private final SharedPreferences pref;

    //============== START

    private static SessionManager mInstance;

    public static SessionManager get() {
        return mInstance;
    }

    public static void init(Context ctx) {
        if (mInstance == null) mInstance = new SessionManager(ctx);
    }

    //============== END

    public SessionManager(final Context context) {
        pref = context.getSharedPreferences(context.getPackageName() + PREF_NAME, 0);
    }

    public void setOnOffNotification(final boolean value) {
        final Editor editor = pref.edit();
        editor.putBoolean(KEY_ON_OFF_NOTIFICATION, value);
        editor.apply();
    }

    public boolean isNotificationOn() {
        return pref.getBoolean(KEY_ON_OFF_NOTIFICATION, TRUE);
    }

    public void setOnOffRTL(final boolean value) {
        final Editor editor = pref.edit();
        editor.putBoolean(KEY_ON_OFF_RTL, value);
        editor.apply();
    }

    public boolean isRTLOn() {
        return pref.getBoolean(KEY_ON_OFF_RTL, FALSE);
    }

    public void setOnBoardingDone(final boolean value) {
        final Editor editor = pref.edit();
        editor.putBoolean(KEY_ONBOARDING, value);
        editor.apply();
    }

    public boolean isOnBoardingDone() {
        return pref.getBoolean(KEY_ONBOARDING, FALSE);
    }

    public void clearAll() {
        final Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }
}
