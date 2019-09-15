package com.example.youtubeapiintegration.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.youtubeapiintegration.Fragments.HomeFragment;
import com.example.youtubeapiintegration.Fragments.SubscriptionsFragment;
import com.example.youtubeapiintegration.Fragments.TrendingFragment;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.SharedPref;
import com.example.youtubeapiintegration.SuggestionProvider;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final Level LOGGING_LEVEL = Level.OFF;
    private DrawerLayout drawer;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    com.google.api.services.youtube.YouTube client;

    private SearchView searchView;
    private Toolbar toolbar;
    private boolean onBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPref sharedPref = new SharedPref(this);

        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.ProfileTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getIntent().setAction("Creating Activity");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView email = header.findViewById(R.id.email);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        // create OAuth credentials using the selected account name
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE_FORCE_SSL));
        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString("PREF_ACCOUNT_NAME", null));

        // Youtube client
        client = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential).setApplicationName(getBaseContext().getString(R.string.app_name))
                .build();

        email.setText(credential.getSelectedAccountName());

        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;

            case R.id.nav_trending:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TrendingFragment()).commit();
                break;

            case R.id.nav_subscriptions:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SubscriptionsFragment()).commit();
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.logout:
                logOutOfAccount();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {

        String action = getIntent().getAction();

        if (action != null && (action.equals("Creating Activity"))) {
            getIntent().setAction(null);
        }
        else if (onBackPressed) {
            getIntent().setAction(null);
        }
        else {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
        onBackPressed = false;
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_CANCELED) {
                onBackPressed = true;
            }
        }
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

                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(ProfileActivity.this,
                            SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query, null);

                    Intent searchIntent = new Intent(ProfileActivity.this, SearchActivity.class);
                    searchIntent.putExtra("queryParam", query);
                    startActivityForResult(searchIntent, 0);
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

                Intent searchIntent = new Intent(ProfileActivity.this, SearchActivity.class);
                searchIntent.putExtra("queryParam", query);
                startActivityForResult(searchIntent, 0);
                overridePendingTransition(R.anim.profile_activity_in, R.anim.profile_activity_out);

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.search:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void logOutOfAccount() {
        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("PREF_ACCOUNT_NAME", "");
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, AuthenticationActivity.class);
        finish();
        startActivity(intent);
    }
}
