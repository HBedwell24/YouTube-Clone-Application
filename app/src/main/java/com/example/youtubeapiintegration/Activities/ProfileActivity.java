package com.example.youtubeapiintegration.Activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeapiintegration.Fragments.HomeFragment;
import com.example.youtubeapiintegration.Fragments.SearchFragment;
import com.example.youtubeapiintegration.Fragments.SettingsFragment;
import com.example.youtubeapiintegration.Fragments.SubscriptionsFragment;
import com.example.youtubeapiintegration.Fragments.TrendingFragment;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.SuggestionProvider;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    private RecyclerView recyclerView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View header;
    private ImageView profilePicture;
    private TextView username, email;

    private MaterialSearchView materialSearchView;
    private Toolbar toolbar;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    com.google.api.services.youtube.YouTube client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }

        header = navigationView.getHeaderView(0);
        profilePicture = header.findViewById(R.id.profile_pic);
        username = header.findViewById(R.id.username);
        email = header.findViewById(R.id.email);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        materialSearchView = (MaterialSearchView) findViewById(R.id.search);
        materialSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        materialSearchView.setEllipsize(true);
        materialSearchView.setCursorDrawable(R.drawable.cursor);
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.length() > 0) {

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    SearchFragment searchFragment = new SearchFragment();

                    Bundle args = new Bundle();
                    args.putString("queryParam", query);
                    searchFragment.setArguments(args);

                    transaction.replace(R.id.fragment_container, searchFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {

            }
        });

        Intent intent = new Intent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }

        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        // create OAuth credentials using the selected account name
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE_FORCE_SSL));
        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        // Youtube client
        client = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential).setApplicationName(getBaseContext().getString(R.string.app_name))
                .build();

        recyclerView = findViewById(R.id.recyclerview);

       if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
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
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, ProfileActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGooglePlayServicesAvailable()) {
            haveGooglePlayServices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                }
                else {
                    checkGooglePlayServicesAvailable();
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    // AsyncLoadTasks.run(this);
                }
                else {
                    chooseAccount();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        //AsyncLoadTasks(this);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        materialSearchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        String value = settings.getString(PREF_ACCOUNT_NAME, "");

        switch (item.getItemId()) {

            case R.id.search:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
        }
        else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    private void logOutOfAccount() {
        SharedPreferences settings = getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, "");
        editor.apply();
    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices() {

        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            //chooseAccount();
        }
        else {
            // Insert code here
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }
}
