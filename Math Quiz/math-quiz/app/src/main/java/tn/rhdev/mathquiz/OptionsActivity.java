package tn.rhdev.mathquiz;


import android.app.ActivityManager;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import tn.rhdev.mathquiz.utils.CheckBackground;
import tn.rhdev.mathquiz.utils.Constant;


public class OptionsActivity extends FragmentActivity {
    LinearLayout adLayout;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    Switch soundMode;
    AdView adMob_banner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        adLayout = (LinearLayout) findViewById(R.id.adVieww);
        add_admob_banner(adLayout);
        ImageView back =  findViewById(R.id.img_back);
        // preferences
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ed = sp.edit();
        TextView ttitle =  findViewById(R.id.title);
        TextView sound =  findViewById(R.id.sound);
        soundMode = (Switch) findViewById(R.id.sound_mode);



        /*********************************************************************/

        boolean backgroundMusic = sp.getBoolean("play",false);
        if (backgroundMusic) {
            soundMode.setChecked(true);
        }
        // mode switch listener
        (soundMode).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ed.putBoolean("play", true);
                    Intent svc=new Intent(OptionsActivity.this, BackgroundSoundService.class);
                    startService(svc);
                    soundMode.setChecked(true);
                } else {
                    ed.putBoolean("play", false);
                    stopService(new Intent(OptionsActivity.this, BackgroundSoundService.class));
                }
                ed.commit();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                finish();
            }
        });

    }//oncreate


    @Override
    public void onStart() {
        super.onStart();
        if (!isMyServiceRunning(BackgroundSoundService.class)) {
            boolean backgroundMusic = sp.getBoolean("play",false);
            if (backgroundMusic ) {
                Intent svc=new Intent(this, BackgroundSoundService.class);
                startService(svc);
            }
        }
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
