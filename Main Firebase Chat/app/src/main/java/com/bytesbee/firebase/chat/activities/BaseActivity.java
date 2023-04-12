package com.bytesbee.firebase.chat.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class BaseActivity extends AppCompatActivity {
    protected final String[] permissionsRecord = {Manifest.permission.VIBRATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    protected final String[] permissionsContact = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected final String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public Activity mActivity;
    public FirebaseAuth auth; //Auth init
    public FirebaseAuth.AuthStateListener authStateListener;
    public DatabaseReference reference; //Database related
    public FirebaseUser firebaseUser; //Current User
    public Screens screens;
    public ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        screens = new Screens(mActivity);
        try {
            authStateListener = firebaseAuth -> {
                try {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        if (getClass().getSimpleName().equalsIgnoreCase("LoginActivity") || getClass().getSimpleName().equalsIgnoreCase("RegisterActivity")) {
                        } else {
                            screens.showClearTopScreen(LoginActivity.class);
                        }
                    }
                } catch (Exception ignored) {
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    public void backButton() {
        try {
            imgBack = findViewById(R.id.imgBack);
            imgBack.setOnClickListener(new SingleClickListener() {
                @Override
                public void onClickView(View v) {
                    finish();
                }
            });
        } catch (Exception ignored) {

        }
    }

    private ProgressDialog pd = null;

    public void showProgress() {
        try {
            if (pd == null) {
                pd = new ProgressDialog(mActivity);
            }
            pd.setMessage(getString(R.string.msg_please_wait));
            pd.show();
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    public void hideProgress() {
        try {
            if (pd != null) {
                pd.dismiss();
                pd = null;
            }
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    protected boolean permissionsAvailable(String[] permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }
}
