package com.example.youtubeapiintegration.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Animations;
import com.example.youtubeapiintegration.Credentials;
import com.example.youtubeapiintegration.Models.VideoDetails.Item;
import com.example.youtubeapiintegration.Models.VideoDetails.VideoDetails;
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

public class HomeFragment extends Fragment {

    private final String TAG = HomeFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SharedPreferences pref;
    private Credentials credentials;
    private Animations animations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        pref = Objects.requireNonNull(getActivity()).getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle("Home");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.recyclerview);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipeRefresh);
        credentials = new Credentials();
        animations = new Animations();

        setUpRefreshListener();

        String previousData = pref.getString("homeFragmentItems", null);

        if (previousData != null) {
            Type type = new TypeToken<List<Item>>(){}.getType();
            List<Item> list = new Gson().fromJson(previousData, type);
            setUpRecyclerView(list);
            animations.runLayoutAnimation(recyclerView);
        }
        else {
            swipeRefreshLayout.setRefreshing(true);
            new dataRequestTask(getActivity()).execute();
        }
    }

    private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new dataRequestTask(getActivity()).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private class dataRequestTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        dataRequestTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            GetDataService dataService = RetrofitInstance.INSTANCE.getRetrofit().create(GetDataService.class);
            Call<VideoDetails> videoDetailsRequest = dataService
                    .getVideoDetails("snippet", null, handleIntent(getActivity().getIntent()), credentials.getApiKey(), "relevance", 25);
            videoDetailsRequest.enqueue(new Callback<VideoDetails>() {

                @Override
                public void onResponse(@NonNull Call<VideoDetails> call, @NonNull Response<VideoDetails> response) {
                    if(response.isSuccessful()) {

                        if(response.body() != null) {

                            Gson gson = new Gson();
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("homeFragmentItems", gson.toJson(response.body().getItems()));
                            editor.apply();

                            Log.e(TAG, "Response Successful");
                            setUpRecyclerView(response.body().getItems());
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
                public void onFailure(@NonNull Call<VideoDetails> call, @NonNull Throwable t) {
                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG.concat("API Request Failed"), Objects.requireNonNull(t.getMessage()));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            return null;
        }
    }

    private static String handleIntent(Intent intent) {

        String query = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        return query;
    }

    private void setUpRecyclerView(List<Item> items) {
        VideoDetailsAdapter videoDetailsAdapter = new VideoDetailsAdapter(getActivity(), items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoDetailsAdapter);
    }
}
