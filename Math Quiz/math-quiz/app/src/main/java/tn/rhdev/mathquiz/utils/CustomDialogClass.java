package tn.rhdev.mathquiz.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import tn.rhdev.mathquiz.QuestionActivity;
import tn.rhdev.mathquiz.R;
import tn.rhdev.mathquiz.ResultActivity;

public class CustomDialogClass extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    private RewardedVideoAd mAd;

    public CustomDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        mAd = MobileAds.getRewardedVideoAdInstance(c);
        mAd.setRewardedVideoAdListener((RewardedVideoAdListener) c);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_yes:
                dismiss();

                QuestionActivity.pDialog.show();
                loadRewardedVideo();
                break;
            case R.id.btn_no:
                dismiss();
               // QuestionActivity.closeQ();
                Intent intent = new Intent(c, ResultActivity.class);
                Bundle b = new Bundle();
                b.putInt("score", QuestionActivity.getMyValue());
                intent.putExtras(b);
                c.startActivity(intent);
                c.finish();

                break;
            default:
                break;
        }


    }

    public void loadRewardedVideo() {
        mAd.loadAd(Constant.rewarded_ad_id,
                new AdRequest.Builder()
                        .build());
    }


}