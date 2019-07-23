package com.example.youtubeapiintegration.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Adapter.VideoStatsAdapter;
import com.example.youtubeapiintegration.Models.VideoDetails;
import com.example.youtubeapiintegration.Models.VideoStats.Item;
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String API_KEY = "";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    private RecyclerView recyclerView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View header;
    private ImageView profilePicture;
    private TextView username, email;
    private SwipeRefreshLayout swipeRefreshLayout;

    private MaterialSearchView materialSearchView;
    private Toolbar toolbar;

    private VideoStatsAdapter videoStatsAdapter;
    private final String TAG = AuthenticationActivity.class.getSimpleName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    com.google.api.services.youtube.YouTube client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

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
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
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

        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        // create OAuth credentials using the selected account name
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE_FORCE_SSL));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        // Youtube client
        client = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential).setApplicationName(getBaseContext().getString(R.string.app_name))
                .build();

        recyclerView = findViewById(R.id.recyclerview);

        if (credential.getSelectedAccount() != null) {
            setUpRefreshListener();
            new subscriptionRequestTask(SubscriptionsActivity.this).execute();
        }
        else {
            chooseAccount();
            if (credential.getSelectedAccount() != null) {
                setUpRefreshListener();
                new subscriptionRequestTask(SubscriptionsActivity.this).execute();
            }
        }


    }

    private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new subscriptionRequestTask(SubscriptionsActivity.this).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent navigationIntent;

        switch (item.getItemId()) {

            case R.id.nav_home:
                navigationIntent = new Intent(this, ProfileActivity.class);
                startActivity(navigationIntent);
                break;

            case R.id.nav_trending:
                break;

            case R.id.nav_subscriptions:
                navigationIntent = new Intent(this, SubscriptionsActivity.class);
                startActivity(navigationIntent);
                break;

            case R.id.nav_settings:
                break;

            case R.id.nav_logout:
                logOutOfAccount();
                navigationIntent = new Intent(this, AuthenticationActivity.class);
                startActivity(navigationIntent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class subscriptionRequestTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<SubscriptionsActivity> activityWeakReference;

        subscriptionRequestTask(SubscriptionsActivity subscriptionActivity) {
            activityWeakReference = new WeakReference<>(subscriptionActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                YouTube.Subscriptions.List subscriptionList = client.subscriptions().list("snippet").
                        setOauthToken(credential.getToken()).setMaxResults(25L).setMine(true).setKey(API_KEY);
                SubscriptionListResponse response = subscriptionList.execute();
                java.util.List<Subscription> subscriptions = response.getItems();

                String publishedAfter = LocalDate.now().minusDays(3).atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                DateTime date = DateTime.parseRfc3339(publishedAfter);

                final ArrayList<String> videoIdList = new ArrayList<>();

                for (Subscription subscription : subscriptions) {

                    String channelId = subscription.getSnippet().getResourceId().getChannelId();

                    YouTube.Search.List channelSearch = client.search().list("snippet").setChannelId(channelId).
                            setOauthToken(credential.getToken()).setPublishedAfter(date).setOrder("date").setKey(API_KEY);

                    SearchListResponse channelSearchResponse = channelSearch.execute();

                    java.util.List<SearchResult> searchs = channelSearchResponse.getItems();

                    for (SearchResult search : searchs) {
                        String videoId = search.getId().getVideoId();
                        videoIdList.add(videoId);
                    }
                }
                String videos = android.text.TextUtils.join(",", videoIdList);

                GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
                Call<VideoStats> videoStatsRequest = dataService
                        .getVideoStats("snippet", null, null, API_KEY, videos, 25);
                videoStatsRequest.enqueue(new Callback<VideoStats>() {

                    @Override
                    public void onResponse(Call<VideoStats> call, Response<VideoStats> response) {
                        if(response.isSuccessful()) {

                            if(response.body() != null) {
                                Log.e(TAG, "Response Successful");
                                setUpVideoRecyclerView(response.body().getItems());
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            else {
                                swipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(SubscriptionsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            swipeRefreshLayout.setRefreshing(true);
                            Toast.makeText(SubscriptionsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoStats> call, Throwable t) {
                        Toast.makeText(SubscriptionsActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG.concat("API Request Failed"), t.getMessage());
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }

            catch (IOException e) {
                e.printStackTrace();
            }

            catch (GoogleAuthException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, SubscriptionsActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
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
                    //AsyncLoadTasks.run(this);
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
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
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

        //login = menu.findItem(R.id.login);
        //logout = menu.findItem(R.id.logout);

        if (credential.getSelectedAccountName() != null) {
            //login.setVisible(false);
            //logout.setVisible(true);
        }

        else {
            //login.setVisible(true);
            //logout.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
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

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void logOutOfAccount() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, "");
        editor.apply();
    }

    private static String handleIntent(Intent intent) {

        String query = null;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        return query;
    }

    private void setUpVideoRecyclerView(List<Item> items) {
        videoStatsAdapter = new VideoStatsAdapter(SubscriptionsActivity.this, items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SubscriptionsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoStatsAdapter);
    }

    /** Check that Google Play services APK is installed and up to date. */
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
            // chooseAccount();
        }
        else {
            // Insert code here
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }
}
