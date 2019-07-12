package com.example.youtubeapiintegration.Retrofit;

import com.example.youtubeapiintegration.Models.UserSubscriptions.UserSubscriptions;
import com.example.youtubeapiintegration.Models.VideoDetails;
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
            @Query("key") String key,
            @Query("id") String id
    );

    @GET("subscriptions")
    Call<UserSubscriptions> getUserSubscriptions(
            @Query("part") String part,
            @Query("maxResults") String maxResults,
            @Query("mine") String mine,
            @Query("key") String key
    );
}
