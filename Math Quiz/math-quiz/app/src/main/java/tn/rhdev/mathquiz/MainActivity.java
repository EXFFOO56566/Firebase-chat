package tn.rhdev.mathquiz;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.security.Policy;

import tn.rhdev.mathquiz.utils.CheckBackground;
import tn.rhdev.mathquiz.utils.Constant;


public class MainActivity extends Activity {
    private Button gameButton;
    public static String PACKAGE_NAME;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    AdView adMob_banner;
    LinearLayout adLayout;
    RelativeLayout bgnim,bgnim2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        PACKAGE_NAME = getApplicationContext().getPackageName();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ed = sp.edit();
        boolean initmusic = sp.getBoolean("initmusic",false);
        if(!initmusic){
            ed.putBoolean("initmusic", true);
            ed.putBoolean("play", true);
            ed.commit();
        }
        boolean backgroundMusic = sp.getBoolean("play",false);
        if (backgroundMusic ) {
            Intent svc=new Intent(this, BackgroundSoundService.class);
            startService(svc);
        } else {
            stopService(new Intent(this, BackgroundSoundService.class));
        }


        gameButton = findViewById(R.id.btn_play);
        gameButton.setOnClickListener(v -> {

            Intent intentnew=new Intent(this,MenuOptions.class);
            startActivity(intentnew);
        });

        adLayout = (LinearLayout) findViewById(R.id.adVieww);
        add_admob_banner(adLayout);



        bgnim = (RelativeLayout) findViewById(R.id.bganimation);
        bgnim2 = (RelativeLayout) findViewById(R.id.bganimation2);

        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 0.5f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(9000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = bgnim.getWidth();
                final float translationX = width * progress;
                bgnim.setTranslationX(translationX);
                bgnim2.setTranslationX(translationX - width);
            }
        });
        animator.start();

    }//oncreate



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
    }
    @Override
    public void onStop() {
        super.onStop();
        if(CheckBackground.appInBackground(this) ){
            stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
        }
        else{
            PowerManager kgMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean showing = kgMgr.isScreenOn();
            if(!showing){
                stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(CheckBackground.appInBackground(this) ){
            stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
        }
        else{
            PowerManager kgMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean showing = kgMgr.isScreenOn();
            if(!showing){
                stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
            }
        }

    }
    @Override
    public void onResume() {
        super.onResume();


    }
    @Override
    public void onStart() {
        super.onStart();
        // preferences
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ed = sp.edit();
        boolean backgroundMusic = sp.getBoolean("play",false);

        if (backgroundMusic ) {

            Intent svc=new Intent(this, BackgroundSoundService.class);
            startService(svc);
        }

    }


    // onClick
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.btn_options:
                Intent intentnew=new Intent(this,OptionsActivity.class);
                startActivity(intentnew);
                break;
            case R.id.img_politics:
                Intent intentpolitics=new Intent(this, PrivacyActivity.class);
                startActivity(intentpolitics);
                break;


            case R.id.btn_quit:
                finish();
                break;

            case R.id.img_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_msg) + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
                break;

            case R.id.img_more:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
                break;

            case R.id.img_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                stopService(new Intent(getApplicationContext(), BackgroundSoundService.class));
                break;



        }
    }



    // add_admob_smart
    void add_admob_banner(LinearLayout adLayout) {
        if (Constant.show_admob_banner
                && ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null) {
            adMob_banner = new AdView(this);
            adMob_banner.setAdSize(AdSize.SMART_BANNER);
            AdRequest adRequest = new AdRequest.Builder().build();
            adMob_banner.setAdUnitId(Constant.adMob_key_banner);
            adMob_banner.loadAd(new AdRequest.Builder().build());
            adLayout.addView(adMob_banner);
            adMob_banner.loadAd(adRequest);
        }//if
        else {
            adLayout.setVisibility(View.GONE);
        }

    }//add_admob_smart
}
