package com.app.multicalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {


    ImageView btmnav;
    RelativeLayout home,contact;
    CardView cal1,cal2,cal3,cal4,cal5,cal6,cal7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bottom dialogue
        btmnav = (ImageView)findViewById(R.id.bottom_nav);
        btmnav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });


        cal1 = (CardView)findViewById(R.id.cal1);
        cal1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,NormalCalculator.class);
                startActivity(i);

            }
        });

        cal2 = (CardView)findViewById(R.id.cal2);
        cal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,BMICalculator.class);
                startActivity(i);

            }
        });


        cal3 = (CardView)findViewById(R.id.cal3);
        cal3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,AgeCalculator.class);
                startActivity(i);

            }
        });


        cal4 = (CardView)findViewById(R.id.cal4);
        cal4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,DiscountCalculator.class);
                startActivity(i);

            }
        });

        cal5 = (CardView)findViewById(R.id.cal5);
        cal5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,ScientificCalculator.class);
                startActivity(i);

            }
        });


        cal6 = (CardView)findViewById(R.id.cal6);
        cal6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,EMICalculator.class);
                startActivity(i);

            }
        });


        cal7 = (CardView)findViewById(R.id.cal7);
        cal7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(MainActivity.this,PercentageCalculator.class);
                startActivity(i);

            }
        });
















        //bottom navigation setup
        home = (RelativeLayout)findViewById(R.id.home);
        contact = (RelativeLayout)findViewById(R.id.contact);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent g =new Intent(MainActivity.this,Contact.class);
                startActivity(g);
            }
        });


    }

    //BottomSheet implimentation
    private void showBottomSheetDialog()
    {


        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);

        LinearLayout share = bottomSheetDialog.findViewById(R.id.share);
        LinearLayout rating = bottomSheetDialog.findViewById(R.id.rating);
        LinearLayout contact = bottomSheetDialog.findViewById(R.id.contact);

        //not implimented yet
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Share is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Rating is Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Update is Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        bottomSheetDialog.show();


    }

}