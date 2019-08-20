package com.example.youtubeapiintegration;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private SharedPreferences sharedPreferences;

    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode", state);
        editor.apply();
    }

    public Boolean loadNightModeState() {
        Boolean state = sharedPreferences.getBoolean("nightMode", false);
        return state;
    }
}
