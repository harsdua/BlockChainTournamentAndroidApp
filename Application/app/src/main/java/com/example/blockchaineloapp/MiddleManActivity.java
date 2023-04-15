package com.example.blockchaineloapp;

import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public abstract class MiddleManActivity extends AppCompatActivity {

    protected void associateActivity(int buttonID, Class<?> activity) {
        Button button = findViewById(buttonID);
        button.setOnClickListener(view -> launchActivity(activity));
    }

    protected void launchActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}
