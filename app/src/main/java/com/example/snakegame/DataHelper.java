package com.example.snakegame;

import android.content.SharedPreferences;

public class DataHelper {
    private final SharedPreferences sharedPreferences;

    public DataHelper(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public int getLevel() {
        return sharedPreferences.getInt("level", 1);
    }

    public boolean getSound() {
        return sharedPreferences.getBoolean("sound", true);
    }

    public int getHighScore1() {
        return sharedPreferences.getInt("highScore1", 0);
    }

    public int getHighScore2() {
        return sharedPreferences.getInt("highScore2", 0);
    }

    public int getHighScore3() {
        return sharedPreferences.getInt("highScore3", 0);
    }



    public void setLevel(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("level", value);

        editor.apply();
    }

    public void setSound(Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("sound", value);

        editor.apply();
    }

    public void setHighScore1(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("highScore1", value);

        editor.apply();
    }

     public void setHighScore2(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("highScore2", value);

        editor.apply();
    }

     public void setHighScore3(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("highScore3", value);

        editor.apply();
    }
}
