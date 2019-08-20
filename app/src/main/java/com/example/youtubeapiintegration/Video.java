package com.example.youtubeapiintegration;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeapiintegration.Adapter.CommentsAdapter;
import com.example.youtubeapiintegration.Models.Comments.Comment;
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
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Video extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    Bundle bundle;
    String videoID;
    YouTubePlayerView playerView;
    TextView views, likes, dislikes, commentsSize, videoTitle, videoDescription, descriptionDropDown;
    RecyclerView recyclerViewComments;
    CommentsAdapter commentsAdapter;
    Credentials credentials;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);

        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.DarkTheme);
        }
        else {
            setTheme(R.style.ProfileTheme);
        }

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
        videoDescription = findViewById(R.id.videoDescription);
        credentials = new Credentials();

        descriptionDropDown = findViewById(R.id.descriptionDropDown);
        descriptionDropDown.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (videoDescription.getVisibility() == View.GONE) {
                    videoDescription.setVisibility(View.VISIBLE);
                    descriptionDropDown.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
                }
                else {
                    videoDescription.setVisibility(View.GONE);
                    descriptionDropDown.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
                }
            }
        });

        bundle = getIntent().getExtras();

        if (bundle != null) {
            videoID = bundle.getString("videoID");
            videoTitle.setText(bundle.getString("videoTitle"));
            if (bundle.getString("views") != null && bundle.getString("description") != null && bundle.getString("likes") != null && bundle.getString("dislikes") != null) {
                views.setText(NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(bundle.getString("views"))).concat(" views"));
                videoDescription.setText(bundle.getString("description"));
                likes.setText(format(Long.parseLong(bundle.getString("likes"))));
                dislikes.setText(format(Long.parseLong(bundle.getString("dislikes"))));
                Log.e("Youtube Video ID ", videoID);
            }
            else {
                getStats();
            }
        }
        else {
            Log.e("Video ID is invalid", videoID.concat(" "));
        }
        //setUpRefreshListener();

        playerView.initialize(credentials.getApiKey(), this);
        getCommentsData();
    }

    /*private void setUpRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getCommentsData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }*/

    private void setUpRecyclerView(List<Comment.Item> items) {
        commentsAdapter = new CommentsAdapter(Video.this, items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Video.this);
        recyclerViewComments.setLayoutManager(layoutManager);
        recyclerViewComments.setAdapter(commentsAdapter);
    }

    private void getStats() {
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<VideoStats> videoStatsRequest = dataService.getVideoStats("snippet, statistics", null, null, credentials.getApiKey(), videoID, 1);
        videoStatsRequest.enqueue(new Callback<VideoStats>() {

            @Override
            public void onResponse(Call<VideoStats> call, Response<VideoStats> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        int number = Integer.parseInt(response.body().getItems().get(0).getStatistics().getViewCount());
                        views.setText(NumberFormat.getNumberInstance(Locale.US).format(number).concat(" views"));
                        likes.setText(format(Long.parseLong(response.body().getItems().get(0).getStatistics().getLikeCount())));
                        dislikes.setText((format(Long.parseLong(response.body().getItems().get(0).getStatistics().getDislikeCount()))));
                        videoDescription.setText(response.body().getItems().get(0).getSnippet().getDescription());
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

    private void getCommentsData() {
        GetDataService dataService = RetrofitInstance.getRetrofit().create(GetDataService.class);
        Call<Comment.Model> commentsRequest = dataService.getCommentsData("snippet,replies", videoID, 25, null, credentials.getApiKey());
        commentsRequest.enqueue(new Callback<Comment.Model>() {

            @Override
            public void onResponse(Call<Comment.Model> call, Response<Comment.Model> response) {
                if (response.isSuccessful()) {

                    if (response.body() != null) {

                        commentsSize.setText(Html.fromHtml("<b>Comments </b>" + response.body().getItems().size()));

                        Log.e("TAG", "Response Successful");
                        setUpRecyclerView(response.body().getItems());
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                    else {
                        //swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(Video.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    //swipeRefreshLayout.setRefreshing(true);
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

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {

        if (value == Long.MIN_VALUE)
            return format(Long.MIN_VALUE + 1);

        if (value < 0)
            return "-" + format(-value);

        if (value < 1000)
            return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
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
