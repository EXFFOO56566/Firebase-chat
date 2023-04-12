package com.devlomi.fireapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.devlomi.fireapp.interfaces.FragmentCallback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public abstract class BaseFragment extends Fragment {
    FragmentCallback fragmentCallback;
    AdView adView;

    public abstract boolean showAds();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentCallback = (FragmentCallback) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    public void adViewInitialized(AdView mAdView) {
        this.adView = mAdView;
        AdListener adListener = new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adView.setVisibility(View.GONE);
                if (fragmentCallback != null)
                    fragmentCallback.addMarginToFab(false);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
                if (fragmentCallback != null)
                    fragmentCallback.addMarginToFab(true);
            }
        };


        adView.setAdListener(adListener);

    }

    public boolean isAdShowing() {
        return adView != null &&
                adView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (adView != null && showAds())
            adView.loadAd(new AdRequest.Builder().build());
    }

    public void onQueryTextChange(String newText) {

    }

    public void onSearchClose() {

    }

}
