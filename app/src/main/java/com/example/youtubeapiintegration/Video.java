package com.example.youtubeapiintegration;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;
import com.example.youtubeapiintegration.Retrofit.GetDataService;
import com.example.youtubeapiintegration.Retrofit.RetrofitInstance;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Video extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    Bundle bundle;
    String videoID;
    YouTubePlayerView playerView;
    TextView views, likes, dislikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        bundle = new Bundle();
        playerView = findViewById(R.id.playerView);
        views = findViewById(R.id.views);
        likes = findViewById(R.id.likes);
        dislikes = findViewById(R.id.dislikes);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            videoID = bundle.getString("videoID");
            Log.e("Youtube Video ID ", videoID);
        }
        else {
            Log.e("Video ID is invalid", videoID.concat(" "));
        }
        playerView.initialize("", this);
        getStats();
    }

    private void getStats() {
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<VideoStats> videoStatsRequest = dataService.getVideoStats("statistics", "", videoID);
        videoStatsRequest.enqueue(new Callback<VideoStats>() {

            @Override
            public void onResponse(Call<VideoStats> call, Response<VideoStats> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        views.setText(response.body().getItems().get(0).getStatistics().getViewCount());
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
