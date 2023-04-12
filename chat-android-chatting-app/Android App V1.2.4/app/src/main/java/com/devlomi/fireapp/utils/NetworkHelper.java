package com.devlomi.fireapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.devlomi.fireapp.model.constants.NetworkType;

/**
 * Created by Devlomi on 20/03/2018.
 */

//this class will get the current network state if it's wifi , data ,roaming, or not connected
public class NetworkHelper {


    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    //is connected to internet regardless wifi or data
    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context.getApplicationContext());
        return (info != null && info.isConnected());
    }


    //is connected via wifi
    private static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context.getApplicationContext());
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    //is connected via mobile data
    private static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context.getApplicationContext());
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    private static boolean isRoaming(Context context) {
        NetworkInfo info = getNetworkInfo(context.getApplicationContext());
        return info.isRoaming();
    }

    //get current network type
    public static int getCurrentNetworkType(Context context) {
        if (isConnectedWifi(context.getApplicationContext()))
            return NetworkType.WIFI;

        else if (isRoaming(context.getApplicationContext()))
            return NetworkType.ROAMING;

        else if (isConnectedMobile(context.getApplicationContext()))
            return NetworkType.DATA;
        else {
            return NetworkType.NOT_CONNECTED;
        }
    }


}
