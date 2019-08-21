package com.example.youtubeapiintegration.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.example.youtubeapiintegration.Fragments.SearchFragment;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.SharedPref;
import com.example.youtubeapiintegration.SuggestionProvider;

import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    SharedPref sharedPref;
    private SearchView searchView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);

        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme);
        }
        else {
            setTheme(R.style.ProfileTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SearchFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.search_activity_in, R.anim.search_activity_out);
        }
        else if (item.getItemId() == R.id.search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.length() > 0) {

                    getIntent().setAction("Searching");

                    searchView.clearFocus();
                    toolbar.collapseActionView();

                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchActivity.this,
                            SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query, null);

                    Intent searchIntent = new Intent(SearchActivity.this, SearchActivity.class);
                    searchIntent.putExtra("queryParam", query);
                    startActivity(searchIntent);
                    overridePendingTransition(R.anim.profile_activity_in, R.anim.profile_activity_out);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {

                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                String query = cursor.getString(2);

                searchView.clearFocus();
                toolbar.collapseActionView();

                Intent searchIntent = new Intent(SearchActivity.this, SearchActivity.class);
                searchIntent.putExtra("queryParam", query);
                startActivity(searchIntent);
                overridePendingTransition(R.anim.profile_activity_in, R.anim.profile_activity_out);

                return true;
            }
        });
        return true;
    }
}
