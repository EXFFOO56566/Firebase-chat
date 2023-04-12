package tn.rhdev.mathquiz.utils;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;


public class CheckBackground{
    public static boolean appInBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }
}