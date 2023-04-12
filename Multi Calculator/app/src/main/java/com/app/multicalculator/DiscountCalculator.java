package com.app.multicalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class DiscountCalculator extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_calculator);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });


    }

    public void applyDiscount(View view){
        EditText priceText = (EditText) findViewById(R.id.price);
        EditText percentageText = (EditText) findViewById(R.id.percentage);
        TextView calcResult = (TextView) findViewById(R.id.calcResult);
        if (priceText.getText().toString().equals("")){
            Toast.makeText(DiscountCalculator.this, "Price field is empty", Toast.LENGTH_SHORT).show();
        }
        else if (percentageText.getText().toString().equals("")){
            Toast.makeText(DiscountCalculator.this, "Discount field is empty", Toast.LENGTH_SHORT).show();
        }
        else if (Double.valueOf(percentageText.getText().toString()) >= 101) {
            Toast.makeText(DiscountCalculator.this, "You cannot discount beyond 101%", Toast.LENGTH_SHORT).show();
        }
        else {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            double price = Double.valueOf(priceText.getText().toString());
            double percentage = Double.valueOf(percentageText.getText().toString());
            double onePerc = price / 100;
            double PercOff = 100 - percentage;
            double result = onePerc * PercOff;
            double finalResult = Math.round(result * 100.0) / 100.0;
            calcResult.setText(String.valueOf("Discounted price:" + '\n' + finalResult));
        }
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(DiscountCalculator.this);

            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {

                    DiscountCalculator.super.onBackPressed();

                }
            });
        }else {
            super.onBackPressed();
        }
    }
}
