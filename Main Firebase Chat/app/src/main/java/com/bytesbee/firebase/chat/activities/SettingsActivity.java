package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.CLICK_DELAY_TIME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PATH_ABOUT_US;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PATH_PRIVACY_POLICY;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.settings.PrivacySettingActivity;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private SwitchCompat notificationOnOff, rtlOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final AdView adView = findViewById(R.id.adView);
        if (BuildConfig.ADS_SHOWN) {
            adView.setVisibility(View.VISIBLE);
            final AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }

        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.strSettings);
        mToolbar.setNavigationOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                onBackPressed();
            }
        });

        final LinearLayout layoutNotification = findViewById(R.id.layoutNotification);
        final LinearLayout layoutRTL = findViewById(R.id.layoutRTL);
        final LinearLayout layoutPrivacySettings = findViewById(R.id.layoutPrivacySettings);
        final LinearLayout layoutRateApp = findViewById(R.id.layoutRateApp);
        final LinearLayout layoutShare = findViewById(R.id.layoutShare);
        final LinearLayout layoutAbout = findViewById(R.id.layoutAbout);
        final LinearLayout layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);
        final LinearLayout layoutLogout = findViewById(R.id.layoutLogout);
        final LinearLayout layoutTakeTour = findViewById(R.id.layoutTakeTour);

        final TextView mTxtVersionName = findViewById(R.id.txtAppVersion);
        mTxtVersionName.setText(String.format(getString(R.string.settingVersion), BuildConfig.VERSION_NAME));

        rtlOnOff = findViewById(R.id.rtlOnOff);
        rtlOnOff.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                restartApp();
            }
        });

        rtlOnOff.setOnCheckedChangeListener((compoundButton, b) -> SessionManager.get().setOnOffRTL(b));

        notificationOnOff = findViewById(R.id.notificationOnOff);
        notificationOnOff.setOnCheckedChangeListener((compoundButton, b) -> SessionManager.get().setOnOffNotification(b));

        if (SessionManager.get().isNotificationOn()) {
            notificationOnOff.setChecked(TRUE);
        } else {
            notificationOnOff.setChecked(FALSE);
        }

        if (SessionManager.get().isRTLOn()) {
            rtlOnOff.setChecked(TRUE);
        } else {
            rtlOnOff.setChecked(FALSE);
        }

        layoutNotification.setOnClickListener(this);
        layoutRTL.setOnClickListener(this);
        layoutPrivacySettings.setOnClickListener(this);
        layoutRateApp.setOnClickListener(this);
        layoutShare.setOnClickListener(this);
        layoutAbout.setOnClickListener(this);
        layoutPrivacyPolicy.setOnClickListener(this);
        layoutLogout.setOnClickListener(this);
        layoutTakeTour.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.layoutNotification) {
            if (notificationOnOff.isChecked()) {
                notificationOnOff.setChecked(FALSE);
            } else {
                notificationOnOff.setChecked(TRUE);
            }
        } else if (id == R.id.layoutRTL) {
            if (rtlOnOff.isChecked()) {
                rtlOnOff.setChecked(FALSE);
            } else {
                rtlOnOff.setChecked(TRUE);
            }
            restartApp();
        } else if (id == R.id.layoutPrivacySettings) {
            screens.showCustomScreen(PrivacySettingActivity.class);
        } else if (id == R.id.layoutTakeTour) {
            screens.openOnBoardingScreen(TRUE);
        } else if (id == R.id.layoutRateApp) {
            Utils.rateApp(mActivity);
        } else if (id == R.id.layoutShare) {
            Utils.shareApp(mActivity);
        } else if (id == R.id.layoutAbout) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> screens.openWebViewActivity(getString(R.string.lblAboutUs), PATH_ABOUT_US), CLICK_DELAY_TIME);
        } else if (id == R.id.layoutPrivacyPolicy) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> screens.openWebViewActivity(getString(R.string.lblPrivacyPolicy), PATH_PRIVACY_POLICY), CLICK_DELAY_TIME);
        } else if (id == R.id.layoutLogout) {
            Utils.logout(mActivity);
        }
    }

    private void restartApp() {
        Utils.showOKDialog(mActivity, R.string.ref_title, R.string.ref_message,
                () -> screens.showClearTopScreen(MainActivity.class));
    }

}
