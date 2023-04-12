package tn.rhdev.mathquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import tn.rhdev.mathquiz.utils.Constant;


public class won extends Activity {
    TextView tv;
    InterstitialAd adMob_interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.won);
        tv = (TextView) findViewById(R.id.congo);
        Bundle b = getIntent().getExtras();
        int y = b.getInt("score");
        tv.setText("FINAL SCORE:" + y);

        add_admob_interstitial();

    }



    // add_admob_interstitial
    void add_admob_interstitial() {
        if (Constant.show_admob_interstitial
                && ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null) {
            adMob_interstitial = new InterstitialAd(this);
            adMob_interstitial.setAdUnitId(Constant.adMob_key_interstitial);
            com.google.android.gms.ads.AdRequest.Builder builder = new AdRequest.Builder();
            adMob_interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    adMob_interstitial.show();
                }
                @Override
                public void onAdClosed() {

                }
            });
            adMob_interstitial.loadAd(builder.build());
        }
        else{

        }
    }

}
