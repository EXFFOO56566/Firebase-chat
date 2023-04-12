package com.bytesbee.firebase.chat.activities.views;

import android.os.SystemClock;
import android.view.View;

/**
 * Handling the problem that double-clicking on a control quickly 2 times (or more times) will cause onClick to be triggered 2 times (or more times)
 * Filter by judging the time interval of 2 click events
 * <p>
 * Subclasses respond to click events by implementing {@link #onClickView}
 */
public abstract class SingleClickListener implements View.OnClickListener {
    /**
     * The shortest time interval between click events
     */
    private static final long MIN_CLICK_INTERVAL = 1000;
    /**
     * Last click time
     */
    private long mLastClickTime;

    /**
     * Click response function
     *
     * @param v The view that was clicked.
     */
    public abstract void onClickView(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        //There may be 2 hits or 3 hits, to ensure that mLastClickTime always records the time of the last click
        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL)
            return;

        onClickView(v);
    }

}