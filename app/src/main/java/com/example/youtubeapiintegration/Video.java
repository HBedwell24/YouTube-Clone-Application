package com.example.youtubeapiintegration;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeapiintegration.Activities.ProfileActivity;
import com.example.youtubeapiintegration.Adapter.CommentsAdapter;
import com.example.youtubeapiintegration.Adapter.VideoDetailsAdapter;
import com.example.youtubeapiintegration.Models.Comments.Comment;
import com.example.youtubeapiintegration.Models.Item;
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Video extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    Bundle bundle;
    String videoID;
    YouTubePlayerView playerView;
    TextView views, likes, dislikes, commentsSize, videoTitle;
    RecyclerView recyclerViewComments;
    CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        bundle = new Bundle();
        playerView = findViewById(R.id.playerView);
        recyclerViewComments = findViewById(R.id.commentsRecyclerView);
        views = findViewById(R.id.views);
        commentsSize = findViewById(R.id.comments);
        likes = findViewById(R.id.likes);
        dislikes = findViewById(R.id.dislikes);
        videoTitle = findViewById(R.id.videoTitle);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            videoID = bundle.getString("videoID");
            videoTitle.setText(bundle.getString("videoTitle"));
            Log.e("Youtube Video ID ", videoID);
        }
        else {
            Log.e("Video ID is invalid", videoID.concat(" "));
        }
        playerView.initialize("", this);
        getStats();
        getCommentsData();
    }

    private void setUpRecyclerView(List<Comment.Item> items) {
        commentsAdapter = new CommentsAdapter(Video.this, items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Video.this);
        recyclerViewComments.setLayoutManager(layoutManager);
        recyclerViewComments.setAdapter(commentsAdapter);
    }

    private void getCommentsData() {
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<Comment.Model> commentsRequest = dataService.getCommentsData("snippet,replies", videoID, 25, "");

        commentsRequest.enqueue(new Callback<Comment.Model>() {
            @Override
            public void onResponse(Call<Comment.Model> call, Response<Comment.Model> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        commentsSize.setText(response.body().getItems().size() + " Comments");
                        Log.e("TAG", "Response Successful");
                        setUpRecyclerView(response.body().getItems());
                    }
                    else {
                        Log.e("TAG", "response body is null");
                    }
                }
                else {
                    Log.e("TAG", "Response was not successful");
                    Toast.makeText(Video.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Comment.Model> call, Throwable t) {
                Toast.makeText(Video.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                Log.e("TAG", t.getMessage());
            }
        });
    }

    private void getStats() {
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<VideoStats> videoStatsRequest = dataService.getVideoStats("statistics", "", videoID);
        videoStatsRequest.enqueue(new Callback<VideoStats>() {

            @Override
            public void onResponse(Call<VideoStats> call, Response<VideoStats> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        int number = Integer.parseInt(response.body().getItems().get(0).getStatistics().getViewCount());
                        views.setText(NumberFormat.getNumberInstance(Locale.US).format(number).concat(" views"));
                        likes.setText(response.body().getItems().get(0).getStatistics().getLikeCount());
                        dislikes.setText((response.body().getItems().get(0).getStatistics().getDislikeCount()));
                    }
                    else {
                        Log.e(">>>> RESPONSE BODY NULL", "NULL");
                    }
                }
                else {
                    Log.e(">>>> RESPONSE NOT SUCCESSFUL", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<VideoStats> call, Throwable t) {
                Toast.makeText(Video.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.cueVideo(videoID);
        youTubePlayer.play();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, "Something went wrong, please try again!", Toast.LENGTH_LONG).show();
    }
}
