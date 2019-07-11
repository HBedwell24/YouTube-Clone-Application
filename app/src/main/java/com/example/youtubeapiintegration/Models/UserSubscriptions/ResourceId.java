package com.example.youtubeapiintegration.Models.UserSubscriptions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResourceId {

    @SerializedName("kind")
    @Expose
    private String kind;
    @SerializedName("channelId")
    @Expose
    private String channelId;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

}
