package com.example.youtubeapiintegration.Fragments;

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

import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingFragment extends Fragment {

    private final String TAG = AuthenticationActivity.class.getSimpleName();

    private static final String API_KEY = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private VideoStatsAdapter videoStatsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Trending");
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getActivity().findViewById(R.id.recyclerview);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeRefresh);

        setUpRefreshListener();
        getData();
    }

    private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getData() {
        swipeRefreshLayout.setRefreshing(true);
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<VideoStats> videoStatsRequest = dataService
                .getVideoStats("snippet, statistics", "mostPopular", "US", API_KEY, null, 25);
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

    private void setUpVideoRecyclerView(List<Item> items) {
        videoStatsAdapter = new VideoStatsAdapter(getActivity(), items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoStatsAdapter);
    }
}
