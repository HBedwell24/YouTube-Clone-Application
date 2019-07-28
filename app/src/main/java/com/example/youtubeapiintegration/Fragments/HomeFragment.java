package com.example.youtubeapiintegration.Fragments;

import android.app.SearchManager;
import android.content.Intent;
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
import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Models.Item;
import com.example.youtubeapiintegration.Models.VideoDetails;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private final String TAG = AuthenticationActivity.class.getSimpleName();

    private static final String API_KEY = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private VideoDetailsAdapter videoDetailsAdapter;

    Call<VideoDetails> videoDetailsRequest;
    String dataBackup;

    Response<VideoDetails> savedResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            dataBackup = savedInstanceState.getString("jsonData", null);
            Response<VideoDetails> object = new Gson().fromJson(dataBackup, Response.class);
            setUpRecyclerView(object.body().getItems());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Home");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        recyclerView = getActivity().findViewById(R.id.recyclerview);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeRefresh);

        if (savedInstanceState == null) {
            setUpRefreshListener();
            getData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("jsonData", new Gson().toJson(savedResponse));
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
        videoDetailsRequest = dataService
                .getVideoDetails("snippet", null, handleIntent(getActivity().getIntent()), API_KEY, "relevance", 50);
        videoDetailsRequest.enqueue(new Callback<VideoDetails>() {

            @Override
            public void onResponse(Call<VideoDetails> call, Response<VideoDetails> response) {
                if(response.isSuccessful()) {

                    if(response.body() != null) {
                        savedResponse = response;
                        Log.e(TAG, "Response Successful");
                        setUpRecyclerView(response.body().getItems());
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
            public void onFailure(Call<VideoDetails> call, Throwable t) {
                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG.concat("API Request Failed"), t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private static String handleIntent(Intent intent) {

        String query = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        return query;
    }

    private void setUpRecyclerView(List<Item> items) {
        videoDetailsAdapter = new VideoDetailsAdapter(getActivity(), items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoDetailsAdapter);
    }
}
