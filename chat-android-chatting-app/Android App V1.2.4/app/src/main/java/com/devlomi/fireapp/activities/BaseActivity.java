package com.devlomi.fireapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.PresenceUtil;

abstract public class BaseActivity extends AppCompatActivity {

    public abstract boolean enablePresence();

    private PresenceUtil presenceUtil;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (enablePresence())
            presenceUtil = new PresenceUtil();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (enablePresence()) {
            presenceUtil.onResume();
            MyApp.baseActivityResumed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (enablePresence()) {
            presenceUtil.onPause();
            MyApp.baseActivityPaused();
        }
    }


}
