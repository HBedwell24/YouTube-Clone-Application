package com.example.youtubeapiintegration.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Models.Item;
import com.example.youtubeapiintegration.Models.VideoDetails;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationActivity extends AppCompatActivity {

    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String API_KEY = "";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;

    private MenuItem login;
    private MenuItem logout;
    private RecyclerView recyclerView;

    private MaterialSearchView materialSearchView;
    private Toolbar toolbar;

    private VideoDetailsAdapter videoDetailsAdapter;
    private final String TAG = AuthenticationActivity.class.getSimpleName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    com.google.api.services.youtube.YouTube client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        // Youtube client
        client = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential).setApplicationName(getBaseContext().getString(R.string.app_name))
                .build();

        recyclerView = findViewById(R.id.recyclerview);

        new subscriptionRequestTask(this).execute();
    }

    private class subscriptionRequestTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AuthenticationActivity> activityWeakReference;

        subscriptionRequestTask(AuthenticationActivity authenticationActivity) {
            activityWeakReference = new WeakReference<>(authenticationActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);

            try {
                YouTube.Subscriptions.List subscriptionList = client.subscriptions().list("snippet").setMine(true).setMaxResults(50L).setKey(API_KEY);
                SubscriptionListResponse response = subscriptionList.execute();
                java.util.List<Subscription> subscriptions = response.getItems();

                for (Subscription subscription : subscriptions) {
                    String channelId = subscription.getSnippet().getChannelId();
                    Call<VideoDetails> videoDetailsRequest = dataService
                            .getVideoDetails("snippet", channelId, handleIntent(getIntent()), API_KEY, "date", 2);
                    videoDetailsRequest.enqueue(new Callback<VideoDetails>() {

                        @Override
                        public void onResponse(Call<VideoDetails> call, Response<VideoDetails> response) {
                            if(response.isSuccessful()) {

                                if(response.body() != null) {
                                    Log.e(TAG, "Response Successful");
                                    Toast.makeText(AuthenticationActivity.this, "Loading...", Toast.LENGTH_LONG).show();
                                    setUpRecyclerView(response.body().getItems());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VideoDetails> call, Throwable t) {
                            Toast.makeText(AuthenticationActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG.concat("API Request Failed"), t.getMessage());
                        }
                    });
                }
            }

            catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, AuthenticationActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
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
                    // Insert code here
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

        login = menu.findItem(R.id.login);
        logout = menu.findItem(R.id.logout);

        if (credential.getSelectedAccountName() != null) {
            login.setVisible(false);
            logout.setVisible(true);
        }

        else {
            login.setVisible(true);
            logout.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        String value = settings.getString(PREF_ACCOUNT_NAME, "");

        switch (item.getItemId()) {

            case R.id.login:
                chooseAccount();
                if (value != null) {
                    logout.setVisible(true);
                    login.setVisible(false);
                }
                return true;

            case R.id.logout:
                logOutOfAccount();
                logout.setVisible(false);
                login.setVisible(true);
                return true;

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

    private void setUpRecyclerView(List<Item> items) {
        videoDetailsAdapter = new VideoDetailsAdapter(AuthenticationActivity.this, items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AuthenticationActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoDetailsAdapter);
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
            chooseAccount();
        }
        else {
            // Insert code here
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);

        if (credential.getSelectedAccountName() != null) {
            logout.setVisible(true);
            login.setVisible(false);
        }
    }
}
