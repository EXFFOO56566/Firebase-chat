package com.app.multicalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    ProgressBar pb;
    int counter = 0;


    //After completion of 10000 ms, the next activity will get started.
    private static int SPLASH_SCREEN_TIME_OUT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This method is used so that your splash activity
        //can cover the entire screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //Intent is used to switch from one activity to another.
                Intent i = new Intent(SplashScreen.this,
                        MainActivity.class);

                //invoke the SecondActivity.
                startActivity(i);


                //the current activity will get finished.
                finish();

            }
        }, SPLASH_SCREEN_TIME_OUT);


        //Progress Bar
        prog();

    }


    //progress bar setup
    public void prog() {

        pb = (ProgressBar) findViewById(R.id.pb);

        final Timer t = new Timer();
        TimerTask tt = new TimerTask() {

            @Override
            public void run() {
                counter++;
                pb.setProgress(counter);

                if (counter == 100)
                    t.cancel();


            }
        };

        t.schedule(tt, 0, 100);
    }


}