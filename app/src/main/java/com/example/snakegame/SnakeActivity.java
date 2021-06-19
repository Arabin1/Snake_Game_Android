package com.example.snakegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SnakeActivity extends AppCompatActivity {

    private SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        SharedPreferences sharedPreferences = getSharedPreferences("snake", Context.MODE_PRIVATE);

        // hide the action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        snakeView = new SnakeView(this, height, width, sharedPreferences);

        setContentView(snakeView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        snakeView.onDestroy();
    }
}
