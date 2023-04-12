package com.devlomi.fireapp.utils;

import android.content.Context;

import com.devlomi.fireapp.R;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;

public class SinchConfig {
    private static final String DEBUG_ENVIRONMENT = "sandbox.sinch.com";
    private static final String RELEASE_ENVIRONMENT = "clientapi.sinch.com";

    public static SinchClient getSinchClient(Context context) {
        return Sinch.getSinchClientBuilder()
                .context(context.getApplicationContext())
                .userId(FireManager.getUid())
                .applicationKey(MyApp.context().getString(R.string.sinch_app_id))
                .applicationSecret(MyApp.context().getString(R.string.sinch_app_secret))
                .environmentHost(RELEASE_ENVIRONMENT)
                .build();
    }
}
