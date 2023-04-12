package com.app.multicalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Contact extends AppCompatActivity {

    RelativeLayout home,contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);





        //bottom navigation setup
        home = (RelativeLayout)findViewById(R.id.home);
        contact = (RelativeLayout)findViewById(R.id.contact);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent g =new Intent(Contact.this,MainActivity.class);
                startActivity(g);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(Contact.this, "Contact", Toast.LENGTH_SHORT).show();

            }
        });

    }





}