package com.example.youtubeapiintegration.Retrofit

import com.example.youtubeapiintegration.Models.Comments.Comment
import com.example.youtubeapiintegration.Models.RecommendedVideos.RecommendedVideo
import com.example.youtubeapiintegration.Models.VideoDetails.VideoDetails
import com.example.youtubeapiintegration.Models.VideoStats.VideoStats

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetDataService {

    @GET("search")
    fun getVideoDetails(
            @Query("part") part: String,
            @Query("channelId") channelId: String,
            @Query("q") q: String,
            @Query("key") key: String,
            @Query("order") order: String,
            @Query("maxResults") maxResults: Int
    ): Call<VideoDetails>

    @GET("search")
    fun getRecommendedVideos(
            @Query("part") part: String,
            @Query("relatedToVideoId") relatedToVideoId: String,
            @Query("maxResults") maxResults: Int,
            @Query("type") type: String,
            @Query("key") key: String
    ): Call<RecommendedVideo.Model>

    @GET("videos")
    fun getVideoStats(
            @Query("part") part: String,
            @Query("chart") chart: String,
            @Query("regionCode") regionCode: String,
            @Query("key") key: String,
            @Query("id") id: String,
            @Query("maxResults") maxResults: Int
    ): Call<VideoStats>

    @GET("commentThreads")
    fun getCommentsData(
            @Query("part") part: String,
            @Query("videoId") videoId: String,
            @Query("maxResults") maxResults: Int,
            @Query("pageToken") pageToken: String,
            @Query("key") key: String
    ): Call<Comment.Model>
}
