package tn.rhdev.mathquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import tn.rhdev.mathquiz.utils.Constant;


public class ResultActivity extends Activity {
    InterstitialAd adMob_interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textResult = (TextView) findViewById(R.id.textResult);

        Bundle b = getIntent().getExtras();

        int score = b.getInt("score");

        textResult.setText(getString(R.string.score)+" " + score);


        Button b1 = (Button) findViewById(R.id.btn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        add_admob_interstitial();

    }

    public void playagain(View o) {

        Intent intent = new Intent(this, QuestionActivity.class);

        startActivity(intent);


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