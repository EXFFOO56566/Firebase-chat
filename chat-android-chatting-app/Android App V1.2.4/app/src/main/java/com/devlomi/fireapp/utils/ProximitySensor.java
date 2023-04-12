package com.devlomi.fireapp.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Devlomi on 04/10/2017.
 */

//this class will listen for proximity sensor state if it's near or far
//it will also turn the screen to black to prevent user input while it's near
public class ProximitySensor implements SensorEventListener {


    public interface Delegate {
        void onProximitySensorNear();

        void onProximitySensorFar();
    }

    private final SensorManager mSensorManager;
    private Sensor mSensor;
    private PowerManager.WakeLock mScreenLock;
    private final Delegate mDelegate;

    public ProximitySensor(final Context context, final Delegate delegate) {
        if (context == null || delegate == null)
            throw new IllegalArgumentException("You must pass a non-null context and delegate");


        final Context appContext = context.getApplicationContext();
        mSensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        mDelegate = delegate;

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //there is no proximity sensor in device
        if (mSensor == null) return;


        //request turn screen lock (turn to black)
        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);

        int screenLockValue;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
        } else {
            try {
                screenLockValue = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
            } catch (Exception exc) {
                screenLockValue = 32; // default integer value of PROXIMITY_SCREEN_OFF_WAKE_LOCK
            }
        }

        mScreenLock = powerManager.newWakeLock(screenLockValue, getClass().getSimpleName());

    }

    //turn screen to black
    public void acquire() {
        if (mScreenLock != null && !mScreenLock.isHeld()) {
            mScreenLock.acquire();
        }
    }

    //revert screen to normal
    public void release() {
        if (mScreenLock != null && mScreenLock.isHeld())
            mScreenLock.release();
    }


    //the listener will not work unless this is called
    public void listenForSensor() {
        if (mSensorManager != null && mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    public void stopListenForSensor() {
        if (mSensorManager != null && mSensor != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) return;

        try {
            //NEAR
            if (event.values[0] < 5f && event.values[0] != mSensor.getMaximumRange()) {
                mDelegate.onProximitySensorNear();
            } else {
                //FAR
                mDelegate.onProximitySensorFar();
            }

        } catch (final Exception exc) {
            Log.e(getClass().getSimpleName(), "onSensorChanged exception", exc);
        }


    }

}
