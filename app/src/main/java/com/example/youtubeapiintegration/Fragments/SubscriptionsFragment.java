package com.example.youtubeapiintegration.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.youtubeapiintegration.Activities.AuthenticationActivity;
import com.example.youtubeapiintegration.Adapter.VideoStatsAdapter;
import com.example.youtubeapiintegration.Models.VideoStats.Item;
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionsFragment extends Fragment {

    private VideoStatsAdapter videoStatsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    static final int REQUEST_AUTHORIZATION = 1;

    private static final Level LOGGING_LEVEL = Level.OFF;
    private static final String API_KEY = "";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private final String TAG = AuthenticationActivity.class.getSimpleName();

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    com.google.api.services.youtube.YouTube client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Subscriptions");
        return inflater.inflate(R.layout.fragment_subscriptions, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getActivity().findViewById(R.id.recyclerview);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeRefresh);

        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        // create OAuth credentials using the selected account name
        credential = GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(YouTubeScopes.YOUTUBE_FORCE_SSL));
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        // Youtube client
        client = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential).setApplicationName(getActivity().getBaseContext().getString(R.string.app_name))
                .build();

        setUpRefreshListener();
        new subscriptionRequestTask(getActivity()).execute();
    }

    private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new subscriptionRequestTask(getActivity()).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private class subscriptionRequestTask extends AsyncTask<Void, Void, Void> {

        private Context c;

        public subscriptionRequestTask(Context context) {
            c = context;
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
                        .getVideoStats("snippet, statistics", null, null, API_KEY, videos, 25);
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
                                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            swipeRefreshLayout.setRefreshing(true);
                            Toast.makeText(getActivity().getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoStats> call, Throwable t) {
                        Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void setUpVideoRecyclerView(List<Item> items) {
        videoStatsAdapter = new VideoStatsAdapter(getActivity(), items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoStatsAdapter);
    }
}
