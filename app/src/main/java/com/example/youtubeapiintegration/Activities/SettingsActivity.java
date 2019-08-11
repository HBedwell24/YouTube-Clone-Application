package com.example.youtubeapiintegration.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.youtubeapiintegration.Fragments.SettingsFragment;
import com.example.youtubeapiintegration.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                settingsFragment).commit();
    }
}
