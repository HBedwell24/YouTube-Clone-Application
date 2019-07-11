package com.example.youtubeapiintegration.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Models.Item;
import com.example.youtubeapiintegration.Models.VideoDetails;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoDetailsAdapter videoDetailsAdapter;
    private final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerView = findViewById(R.id.recyclerview);

        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<VideoDetails> videoDetailsRequest = dataService
                .getVideoDetails("snippet", handleIntent(getIntent()), "", "relevance", 25);

        videoDetailsRequest.enqueue(new Callback<VideoDetails>() {

            @Override
            public void onResponse(Call<VideoDetails> call, Response<VideoDetails> response) {
                if(response.isSuccessful()) {

                    if(response.body() != null) {
                        Log.e(TAG, "Response Successful");
                        Toast.makeText(ProfileActivity.this, "Loading...", Toast.LENGTH_LONG).show();
                        setUpRecyclerView(response.body().getItems());
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoDetails> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG.concat("API Request Failed"), t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.login:

            case R.id.logout:

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private String handleIntent(Intent intent) {

        String query = null;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        return query;
    }

    private void setUpRecyclerView(List<Item> items) {
        videoDetailsAdapter = new VideoDetailsAdapter(ProfileActivity.this, items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoDetailsAdapter);
    }
}
