package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_STATUS;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.github.appintro.AppIntro2;
import com.github.appintro.AppIntroCustomLayoutFragment;
import com.github.appintro.AppIntroPageTransformerType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.Nullable;

public class OnBoardingActivity extends AppIntro2 {

    private Screens screens;
    private boolean isTakeTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screens = new Screens(getApplicationContext());

        isTakeTour = getIntent().getBooleanExtra(EXTRA_STATUS, false);

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_1));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_2));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_3));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_4));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_5));

        setVibrate(true);
        setTransformer(new AppIntroPageTransformerType.Parallax(1.0, -1.0, 2.0));
        setNavBarColor(ContextCompat.getColor(this, R.color.navGrayColor));
        setImmersive(true);
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        nextScreen();
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        nextScreen();
    }

    private void nextScreen() {
        if (isTakeTour) {
            finish();
        } else {
            SessionManager.get().setOnBoardingDone(true);
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                screens.showClearTopScreen(MainActivity.class);
            } else {
                screens.showClearTopScreen(LoginActivity.class);
            }
        }
    }
}