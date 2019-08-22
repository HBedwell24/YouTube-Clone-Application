package com.example.youtubeapiintegration.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.youtubeapiintegration.Adapter.VideoStatsAdapter;
import com.example.youtubeapiintegration.Animations;
import com.example.youtubeapiintegration.Credentials;
import com.example.youtubeapiintegration.Models.VideoStats.Item;
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingFragment extends Fragment {

    private final String TAG = TrendingFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Credentials credentials;
    private Animations animations;

    private SharedPreferences pref;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        pref = Objects.requireNonNull(getActivity()).getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle("Trending");
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.recyclerview);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeRefresh);
        credentials = new Credentials();
        animations = new Animations();

        setUpRefreshListener();

        String previousData = pref.getString("trendingFragmentItems", null);

        if (previousData != null) {
            Type type = new TypeToken<List<Item>>(){}.getType();
            List<Item> list = new Gson().fromJson(previousData, type);
            setUpVideoRecyclerView(list);
            animations.runLayoutAnimation(recyclerView);
        }
        else {
            swipeRefreshLayout.setRefreshing(true);
            new trendingDataRequestTask(getActivity()).execute();
        }
    }

    private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new trendingDataRequestTask(getActivity()).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private class trendingDataRequestTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        trendingDataRequestTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
            Call<VideoStats> videoStatsRequest = dataService
                    .getVideoStats("snippet, statistics", "mostPopular", "US", credentials.getApiKey(), null, 25);
            videoStatsRequest.enqueue(new Callback<VideoStats>() {

                @Override
                public void onResponse(@NonNull Call<VideoStats> call, @NonNull Response<VideoStats> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {

                            Gson gson = new Gson();
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("trendingFragmentItems", gson.toJson(response.body().getItems()));
                            editor.apply();

                            Log.e(TAG, "Response Successful");
                            setUpVideoRecyclerView(response.body().getItems());
                            animations.runLayoutAnimation(recyclerView);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        else {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        swipeRefreshLayout.setRefreshing(true);
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VideoStats> call, @NonNull Throwable t) {
                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG.concat("API Request Failed"), Objects.requireNonNull(t.getMessage()));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            return null;
        }
    }

    private void setUpVideoRecyclerView(List<Item> items) {
        VideoStatsAdapter videoStatsAdapter = new VideoStatsAdapter(getActivity(), items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoStatsAdapter);
    }
}
