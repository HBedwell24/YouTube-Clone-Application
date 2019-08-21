package com.example.youtubeapiintegration.Retrofit;

import com.example.youtubeapiintegration.Models.Comments.Comment;
import com.example.youtubeapiintegration.Models.VideoDetails.VideoDetails;
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataService {

    @GET("search")
    Call<VideoDetails> getVideoDetails(
            @Query("part") String part,
            @Query("channelId") String channelId,
            @Query("q") String q,
            @Query("key") String key,
            @Query("order") String order,
            @Query("maxResults") int maxResults
    );

    @GET("videos")
    Call<VideoStats> getVideoStats(
            @Query("part") String part,
            @Query("chart") String chart,
            @Query("regionCode") String regionCode,
            @Query("key") String key,
            @Query("id") String id,
            @Query("maxResults") int maxResults
    );

    @GET("commentThreads")
    Call<Comment.Model> getCommentsData(
            @Query("part") String part,
            @Query("videoId") String videoId,
            @Query("maxResults") int maxResults,
            @Query("pageToken") String pageToken,
            @Query("key") String key
    );
}
