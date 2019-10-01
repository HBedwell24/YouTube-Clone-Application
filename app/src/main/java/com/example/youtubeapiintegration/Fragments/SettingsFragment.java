package com.example.youtubeapiintegration.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.SharedPref;
import com.example.youtubeapiintegration.SuggestionProvider;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPref sharedPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Objects.requireNonNull(getActivity()).setTitle("Settings");
        setPreferencesFromResource(R.xml.pref_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(getActivity());

        super.onCreate(savedInstanceState);

        Preference clearHistory = findPreference("clearHistory");
        clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDialog();
                return true;
            }
        });

        SwitchPreference darkThemeToggle = findPreference("darkTheme");
        darkThemeToggle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object isChecked) {
                boolean isOn = (Boolean) isChecked;
                if (isOn) {
                    sharedPref.setNightModeState(true);
                    restartActivity();
                }
                else {
                    sharedPref.setNightModeState(false);
                    restartActivity();
                }
                return true;
            }
        });
    }

    private void restartActivity() {
        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        getActivity().finish();
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private void showDialog() throws Resources.NotFoundException {
        new AlertDialog.Builder(getActivity(), R.style.DialogTheme)
                .setCancelable(true)
                .setTitle("Clear Cookies")
                .setMessage("Are you sure you want to clear cookies? This action cannot be undone.")
                .setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_warning_black_24dp))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                        suggestions.clearHistory();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }
}
