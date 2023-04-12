package tn.rhdev.mathquiz.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import tn.rhdev.mathquiz.MainActivity;
import tn.rhdev.mathquiz.R;


public class AppRater {

    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 1;//Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.ratenow);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ratebg));
        ll.setPadding(100,600,100,150);



        TextView tv = new TextView(mContext);
        tv.setText(R.string.ratetext);
        tv.setWidth(240);
        tv.setHeight(350);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX ,50);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setPadding(4, 0, 4, 0);
        ll.addView(tv);

        Button b1 = new Button(mContext);
        b1.setBackgroundResource(R.drawable.buttondialog);
        b1.setTextColor(Color.parseColor("#ffffff"));


        b1.setText(R.string.ratenow);
       /* b1.setBackgroundColor(Color.parseColor("#a5e24d"));
        b1.setTextColor(Color.parseColor("#ffffff"));*/

        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.PACKAGE_NAME)));
                dialog.dismiss();
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }

            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText(R.string.remindmelater);
        b2.setBackgroundResource(R.drawable.buttondialog);
        b2.setTextColor(Color.parseColor("#ffffff"));
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dialog.dismiss();
                if (editor != null) {
                    editor.putLong("launch_count", 0);
                    editor.putLong("date_firstlaunch", System.currentTimeMillis());
                    editor.commit();
                }

            }
        });
        ll.addView(b2);



        dialog.setContentView(ll);
        dialog.show();
    }
}