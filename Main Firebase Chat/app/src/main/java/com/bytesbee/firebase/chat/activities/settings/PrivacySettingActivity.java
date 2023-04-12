package com.bytesbee.firebase.chat.activities.settings;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_HIDE_EMAIL;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_HIDE_PROFILE_PHOTO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_USERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.bytesbee.firebase.chat.activities.BaseActivity;
import com.bytesbee.firebase.chat.activities.BuildConfig;
import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PrivacySettingActivity extends BaseActivity implements View.OnClickListener {

    private SwitchCompat emailOnOff, profilePhotoOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        final String currentId = firebaseUser.getUid();

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
        getSupportActionBar().setTitle(R.string.strPrivacySetting);
        mToolbar.setNavigationOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                onBackPressed();
            }
        });

        final LinearLayout layoutEmail = findViewById(R.id.layoutEmail);
        final LinearLayout layoutProfilePhoto = findViewById(R.id.layoutProfilePhoto);

        profilePhotoOnOff = findViewById(R.id.profilePhotoOnOff);
        profilePhotoOnOff.setOnCheckedChangeListener((compoundButton, b) -> Utils.updateGenericUserField(currentId, EXTRA_HIDE_PROFILE_PHOTO, b));

        emailOnOff = findViewById(R.id.emailOnOff);
        emailOnOff.setOnCheckedChangeListener((compoundButton, b) -> Utils.updateGenericUserField(currentId, EXTRA_HIDE_EMAIL, b));

        reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(currentId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    emailOnOff.setChecked(user.isHideEmail());
                    profilePhotoOnOff.setChecked(user.isHideProfilePhoto());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        layoutEmail.setOnClickListener(this);
        layoutProfilePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.layoutEmail) {
            if (emailOnOff.isChecked()) {
                emailOnOff.setChecked(FALSE);
            } else {
                emailOnOff.setChecked(TRUE);
            }
        } else if (id == R.id.layoutProfilePhoto) {
            if (profilePhotoOnOff.isChecked()) {
                profilePhotoOnOff.setChecked(FALSE);
            } else {
                profilePhotoOnOff.setChecked(TRUE);
            }
        }
    }

}
