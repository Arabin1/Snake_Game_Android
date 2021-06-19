package com.example.snakegame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DataHelper dataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        SharedPreferences sharedPreferences = getSharedPreferences("snake", Context.MODE_PRIVATE);
        dataHelper = new DataHelper(sharedPreferences);

        Button buttonStartGame = findViewById(R.id.buttonStartGameId);
        Button buttonSound = findViewById(R.id.buttonSoundId);
        Button buttonLevel = findViewById(R.id.buttonLevelId);
        Button buttonHighScore = findViewById(R.id.buttonHighScoreId);
        Button buttonHelp = findViewById(R.id.buttonHelpId);
        Button buttonAbout = findViewById(R.id.buttonAboutId);

        buttonStartGame.setOnClickListener(this);
        buttonSound.setOnClickListener(this);
        buttonLevel.setOnClickListener(this);
        buttonHighScore.setOnClickListener(this);
        buttonHelp.setOnClickListener(this);
        buttonAbout.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartGameId :
                startNewGame();
                break;
            case R.id.buttonSoundId:
                soundMethod();
                break;
            case R.id.buttonLevelId:
                levelMethod();
                break;
            case R.id.buttonHelpId:
                helpMethod();
                break;
            case R.id.buttonHighScoreId:
                highScoreMethod();
                break;
            case R.id.buttonAboutId:
                aboutMethod();
        }
    }

    private void aboutMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.about_layout, null);

        builder.setTitle("About");

        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void highScoreMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.high_score_layout, null);

        builder.setView(view);
        builder.setCancelable(false);

        TextView level1TextView = view.findViewById(R.id.level1TextViewId);
        TextView level2TextView = view.findViewById(R.id.level2TextViewId);
        TextView level3TextView = view.findViewById(R.id.level3TextViewId);

        level1TextView.setText("" + dataHelper.getHighScore1());
        level2TextView.setText("" + dataHelper.getHighScore2());
        level3TextView.setText("" + dataHelper.getHighScore3());

        builder.setTitle("Current High Score");

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Clear High Score", (dialog, which) -> {
            dataHelper.setHighScore1(0);
            dataHelper.setHighScore2(0);
            dataHelper.setHighScore3(0);

            level1TextView.setText("" + 0);
            level2TextView.setText("" + 0);
            level3TextView.setText("" + 0);
        });

        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void levelMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.level_layout, null);

        builder.setView(view);
        builder.setCancelable(false);

        SeekBar seekBar = view.findViewById(R.id.levelSeekBarId);
        seekBar.setProgress(dataHelper.getLevel() - 1); // set initial progress from shared Preference
        TextView textView = view.findViewById(R.id.levelTextViewId);
        textView.setText("" + (seekBar.getProgress() + 1)); // set the initial progress

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("" + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setTitle("Select Level");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dataHelper.setLevel(seekBar.getProgress() + 1); // save the level
            dialog.dismiss();
        } );
        builder.show();
    }

    private void helpMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Help?");
        builder.setMessage(R.string.description);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void soundMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.sound_layout, null);

        builder.setView(view);

        ToggleButton toggleButton = view.findViewById(R.id.toggleButtonId);
        // set initial text from shared preference
        toggleButton.setText(dataHelper.getSound() ? toggleButton.getTextOn() : toggleButton.getTextOff());

        builder.setTitle("Sound");

        builder.setPositiveButton("OK", (dialog, which) -> {
            dataHelper.setSound(toggleButton.getText().equals("On"));
            dialog.dismiss();
        });
        builder.setCancelable(false);

        builder.show();
    }

    private void startNewGame() {
        Intent intent = new Intent(MainActivity.this, SnakeActivity.class);
        startActivity(intent);
    }

}