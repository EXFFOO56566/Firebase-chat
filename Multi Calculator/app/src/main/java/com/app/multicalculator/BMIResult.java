package com.app.multicalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BMIResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmiresult);


        Intent intent = getIntent();

        float BMI  = Math.round((intent.getFloatExtra("BMI", 0) * 100) / 100);
        String age_value = intent.getStringExtra("age");

        TextView your_bmi = findViewById(R.id.your_bmi);
        your_bmi.setText(String.valueOf(BMI));

        TextView age = findViewById(R.id.age);
        age.setText(age_value);

        TextView category = findViewById(R.id.category);
        Category category1 = new Category();
        category.setText(category1.getCategory(BMI));

        TextView condition = findViewById(R.id.condition);
        Condition condition1 = new Condition();
        condition.setText(condition1.getCategory(BMI));

        Button recalculate = findViewById(R.id.recalculate);
        recalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });

    }

    private void updateUI() {
        Intent intent1 = new Intent(BMIResult.this,BMICalculator.class);
        startActivity(intent1);
        fileList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }
}