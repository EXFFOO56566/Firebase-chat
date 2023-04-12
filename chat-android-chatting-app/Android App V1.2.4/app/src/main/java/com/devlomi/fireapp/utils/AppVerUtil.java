package com.devlomi.fireapp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppVerUtil {
    public static String getAppVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isAppSupportsGroups(String appVersion) {
        double appVersionDouble = Double.parseDouble(appVersion);
//        return appVersionDouble > 1.0;
        return true; //HACK FIX!
    }
}
