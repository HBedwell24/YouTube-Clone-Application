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
import androidx.fragment.app.FragmentTransaction;

import com.example.youtubeapiintegration.Fragments.HomeFragment;
import com.example.youtubeapiintegration.Fragments.SearchFragment;
import com.example.youtubeapiintegration.Fragments.SettingsFragment;
import com.example.youtubeapiintegration.Fragments.SubscriptionsFragment;
import com.example.youtubeapiintegration.Fragments.TrendingFragment;
import com.example.youtubeapiintegration.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }

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

       if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //handleIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;

            case R.id.nav_trending:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                        new TrendingFragment()).commit();
                break;

            case R.id.nav_subscriptions:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                        new SubscriptionsFragment()).commit();
                break;

            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;

            case R.id.logout:
                logOutOfAccount();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(ProfileActivity.this,
                            SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query, null);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    SearchFragment searchFragment = new SearchFragment();

                    Bundle args = new Bundle();
                    args.putString("queryParam", query);
                    searchFragment.setArguments(args);

                    transaction.replace(R.id.fragment_container, searchFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    searchView.clearFocus();
                    toolbar.collapseActionView();

                }
                return false;
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

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                SearchFragment searchFragment = new SearchFragment();

                Bundle args = new Bundle();
                args.putString("queryParam", query);
                searchFragment.setArguments(args);

                transaction.replace(R.id.fragment_container, searchFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                searchView.clearFocus();
                toolbar.collapseActionView();

                return false;
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
        }
        else {
            super.onBackPressed();
        }
    }

    private void logOutOfAccount() {
        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("PREF_ACCOUNT_NAME", "");
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, AuthenticationActivity.class);
        startActivity(intent);
    }
}
