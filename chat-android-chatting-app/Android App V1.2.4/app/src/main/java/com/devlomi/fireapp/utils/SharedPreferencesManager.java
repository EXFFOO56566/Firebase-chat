package com.devlomi.fireapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.constants.NetworkType;
import com.devlomi.fireapp.model.realms.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Devlomi on 01/02/2017.
 */

public class SharedPreferencesManager {


    //this will contains the app preferences
    private static SharedPreferences mSharedPref;
    //this will contains the users settings


    //get key of auto download from settings shared
    private static String key_autodownload_roaming;
    private static String key_autodownload_wifi;
    private static String key_autodownload_cellular;

    //check what user is enabled (video,audio,images) for every state (wifi,cellular,roaming)
    private static Set<String> defaultWifiSet;
    private static Set<String> defaultCellularSet;
    private static Set<String> defaultRoamingSet;

    synchronized public static void init(Context context) {
        if (mSharedPref == null)
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);


        if (defaultWifiSet == null)
            defaultWifiSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_wifi_defaults)));

        if (defaultCellularSet == null)
            defaultCellularSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_cellular_defaults)));

        if (defaultRoamingSet == null)
            defaultRoamingSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_roaming_defaults)));

        if (key_autodownload_wifi == null) {
            key_autodownload_wifi = context.getResources().getString(R.string.key_autodownload_wifi);
        }

        if (key_autodownload_cellular == null) {
            key_autodownload_cellular = context.getResources().getString(R.string.key_autodownload_cellular);
        }
        if (key_autodownload_roaming == null) {
            key_autodownload_roaming = context.getResources().getString(R.string.key_autodownload_roaming);
        }

    }

    public static void setContactSynced(boolean isSynced) {
        mSharedPref.edit().putBoolean("isSynced", isSynced).apply();
    }


    public static boolean isContactSynced() {
        return mSharedPref.getBoolean("isSynced", false);
    }


    public static boolean isEnterIsSend() {
        return mSharedPref.getBoolean("enter_is_send", false);
    }


    //this will return if user has enabled auto download for certain network type and for certain media type
    public static boolean canDownload(int mediaType, int availableNetworkType) {
        //if it's a voice message download it automatically
        if (mediaType == MessageType.RECEIVED_VOICE_MESSAGE) return true;

        //get value type in the Set by given media type
        //image is 0,video is 1,audio is 2 and default is 3
        String value = String.valueOf(getTypeValueByMediaType(mediaType));

        switch (availableNetworkType) {

            case NetworkType.WIFI:
                //check if auto download is enabled on wifi for the given type
                Set<String> stringSetWifi = mSharedPref.getStringSet(key_autodownload_wifi, defaultWifiSet);
                if (stringSetWifi.contains(value))
                    return true;

                break;

            //check if auto download is enabled on DATA for the given type
            case NetworkType.DATA:
                Set<String> stringSetData = mSharedPref.getStringSet(key_autodownload_cellular, defaultCellularSet);
                if (stringSetData.contains(value))
                    return true;
                break;
            //check if auto download is enabled on roaming for the given type
            case NetworkType.ROAMING:
                Set<String> stringSetRoaming = mSharedPref.getStringSet(key_autodownload_roaming, defaultRoamingSet);
                if (stringSetRoaming.contains(value))
                    return true;
                break;
        }
        return false;
    }


    //get values from values array by given media type
    public static int getTypeValueByMediaType(int mediaType) {
        switch (mediaType) {
            case MessageType.RECEIVED_IMAGE:
                return 0;
            case MessageType.RECEIVED_AUDIO:
                return 1;
            case MessageType.RECEIVED_VIDEO:
                return 2;

            default:
                return 3;
        }
    }

    //save user status locally to show it his profile
    public static void saveMyStatus(String status) {
        mSharedPref.edit().putString("status", status).apply();
    }

    //save user username locally to show it his profile
    public static void saveMyUsername(String username) {
        mSharedPref.edit().putString("username", username).apply();
    }

    //save user photo path locally to show it his profile
    public static void saveMyPhoto(String path) {
        mSharedPref.edit().putString("user_image", path).apply();
    }

    //save user phone number locally to show it his profile
    public static void savePhoneNumber(String phoneNumber) {
        mSharedPref.edit().putString("phone_number", phoneNumber).apply();
    }


    public static String getUserName() {
        return mSharedPref.getString("username", "");
    }

    public static String getStatus() {
        return mSharedPref.getString("status", "");
    }

    public static String getPhoneNumber() {
        return mSharedPref.getString("phone_number", "");
    }

    public static String getMyPhoto() {
        return mSharedPref.getString("user_image", "");
    }


    //check if user enabled vibration for notifications
    public static boolean isVibrateEnabled() {
        return mSharedPref.getBoolean("notifications_new_message_vibrate", false);
    }

    //get notification ringtone
    public static Uri getRingtone() {
        return Uri.parse(mSharedPref.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound"));
    }

    //check if user enabled notifications
    public static boolean isNotificationEnabled() {
        return mSharedPref.getBoolean("notifications_new_message", true);
    }


    //check if user info is saved when launch the app for first time
    public static boolean isUserInfoSaved() {
        return mSharedPref.getBoolean("is_userInfo_saved", false);
    }

    public static void setUserInfoSaved(boolean bool) {
        mSharedPref.edit().putBoolean("is_userInfo_saved", bool).apply();
    }

    //save country code to use it late when formatting the numbers
    public static void saveCountryCode(String phoneNumber) {
        mSharedPref.edit().putString("ccode", phoneNumber).apply();
    }

    public static void saveMyThumbImg(String thumbImg) {
        mSharedPref.edit().putString("thumbImg", thumbImg).apply();
    }

    public static String getThumbImg() {
        return mSharedPref.getString("thumbImg", "");
    }

    public static String getCountryCode() {
        return mSharedPref.getString("ccode", "");
    }


    // set notification token as saved to prevent resending it to server
    public static void setTokenSaved(boolean bool) {
        mSharedPref.edit().putBoolean("token_sent", bool).commit();
    }

    public static boolean isTokenSaved() {
        return mSharedPref.getBoolean("token_sent", false);
    }


    public static User getCurrentUser() {
        User user = new User();
        user.setUid(FireManager.getUid());
        user.setThumbImg(SharedPreferencesManager.getThumbImg());
        user.setPhoto("");
        user.setPhone(SharedPreferencesManager.getPhoneNumber());
        user.setStatus(SharedPreferencesManager.getStatus());
        user.setUserName(SharedPreferencesManager.getUserName());
        user.setUserLocalPhoto(SharedPreferencesManager.getMyPhoto());
        return user;
    }

    public static void setFetchUserGroupsSaved(boolean b) {
        mSharedPref.edit().putBoolean("fetch_user_groups_saved", b).apply();
    }

    public static boolean isFetchedUserGroups() {
        return mSharedPref.getBoolean("fetch_user_groups_saved", false);
    }

    public static void setAppVersionSaved(boolean b) {
        mSharedPref.edit().putBoolean("is_app_ver_saved", b).apply();
    }

    public static boolean isAppVersionSaved() {
        return mSharedPref.getBoolean("is_app_ver_saved", false);
    }

    public static void setLastSeenState(int lastSeenState) {
        mSharedPref.edit().putInt("lastSeenState", lastSeenState).apply();
    }

    public static int getLastSeenState() {
        return mSharedPref.getInt("lastSeenState", 0);
    }


    public static void setSinchConfigured(boolean value) {
        mSharedPref.edit().putBoolean("sinchConfigured", value).apply();
    }

    public static boolean isSinchConfigured() {
        return mSharedPref.getBoolean("sinchConfigured", false);
    }


    public static void setWallpaperPath(String value) {
        mSharedPref.edit().putString("wallpaperPath", value).apply();
    }

    public static String getWallpaperPath() {
        return mSharedPref.getString("wallpaperPath", "");
    }


    public static void setLastBackup(long value) {
        mSharedPref.edit().putLong("lastBackup", value).apply();
    }

    public static long getLastBackup() {
        return mSharedPref.getLong("lastBackup", -1);
    }


    //used to determine whether the UID,and Phone number were saved
    public static void setCurrentUserInfoSaved(boolean value) {
        mSharedPref.edit().putBoolean("currentUserInfoSaved", value).apply();
    }

    public static boolean isCurrentUserInfoSaved() {
        return mSharedPref.getBoolean("currentUserInfoSaved", false);
    }

    public static void setDoNotShowBatteryOptimizationAgain(boolean value) {
        mSharedPref.edit().putBoolean("doNotShowBatteryOptimizationAgain", value).apply();
    }

    public static boolean isDoNotShowBatteryOptimizationAgain() {
        return mSharedPref.getBoolean("doNotShowBatteryOptimizationAgain", false);
    }
}
