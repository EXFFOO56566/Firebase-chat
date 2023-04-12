package com.bytesbee.firebase.chat.activities.fcm;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.bytesbee.firebase.chat.activities.managers.Utils;

/**
 * Determines global app lifecycle states.
 * <p>
 * The following is the reference of activities states:
 * <p>
 * The <b>visible</b> lifetime of an activity happens between a call to onStart()
 * until a corresponding call to onStop(). During this time the user can see the
 * activity on-screen, though it may not be in the foreground and interacting with
 * the user. The onStart() and onStop() methods can be called multiple times, as
 * the activity becomes visible and hidden to the user.
 * <p>
 * The <b>foreground</b> lifetime of an activity happens between a call to onResume()
 * until a corresponding call to onPause(). During this time the activity is in front
 * of all other activities and interacting with the user. An activity can frequently
 * go between the resumed and paused states -- for example when the device goes to
 * sleep, when an activity result is delivered, when a new intent is delivered --
 * so the code in these methods should be fairly lightweight.
 */
public class ApplicationLifecycleManager implements ActivityLifecycleCallbacks {

    /**
     * Manages the state of opened vs closed activities, should be 0 or 1.
     * It will be 2 if this value is checked between activity B onStart() and
     * activity A onStop().
     * It could be greater if the top activities are not fullscreen or have
     * transparent backgrounds.
     */
    private static int visibleActivityCount = 0;

    /**
     * Manages the state of opened vs closed activities, should be 0 or 1
     * because only one can be in foreground at a time. It will be 2 if this
     * value is checked between activity B onResume() and activity A onPause().
     */
    private static int foregroundActivityCount = 0;

    /**
     * Returns true if app has foreground
     */
    public static boolean isAppInForeground() {
        return foregroundActivityCount > 0;
    }

    /**
     * Returns true if any activity of app is visible (or device is sleep when
     * an activity was visible)
     */
    public static boolean isAppVisible() {
        return visibleActivityCount > 0;
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (SessionManager.get().isRTLOn()) {
            Utils.RTLSupport(activity.getWindow());
        }
    }

    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        foregroundActivityCount++;
    }

    public void onActivityPaused(Activity activity) {
        foregroundActivityCount--;
    }


    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
        visibleActivityCount++;
    }

    public void onActivityStopped(Activity activity) {
        visibleActivityCount--;
    }
}