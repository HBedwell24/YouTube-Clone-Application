package com.example.youtubeapiintegration;

import android.content.SearchRecentSuggestionsProvider;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.youtubeapiintegration";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
