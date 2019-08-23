package com.example.youtubeapiintegration.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.youtubeapiintegration.Activities.VideoActivity;
import com.example.youtubeapiintegration.Models.RecommendedVideos.RecommendedVideo;
import com.example.youtubeapiintegration.R;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecommendedVideoAdapter extends RecyclerView.Adapter<RecommendedVideoAdapter.VideoDetailsViewHolder> {

    private Context context;
    private List<RecommendedVideo.Item> recommendedVideoList;
    private DateTime dateTime;

    public RecommendedVideoAdapter(Context context, List<RecommendedVideo.Item> recommendedVideoList) {
        this.context = context;
        this.recommendedVideoList = recommendedVideoList;
    }

    private String timestampFormatter(DateTime publishedAt) {

        DateTime now = DateTime.now();
        Minutes minutesBetween = Minutes.minutesBetween(publishedAt, now);

        if (minutesBetween.isLessThan(Minutes.ONE)) {
            return "Just now";
        }

        Hours hoursBetween = Hours.hoursBetween(publishedAt, now);

        if (hoursBetween.isLessThan(Hours.ONE)) {
            return formatMinutes(minutesBetween.getMinutes());
        }

        Days daysBetween = Days.daysBetween(publishedAt, now);

        if (daysBetween.isLessThan(Days.ONE)) {
            return formatHours(hoursBetween.getHours());
        }

        Weeks weeksBetween = Weeks.weeksBetween(publishedAt, now);

        if (weeksBetween.isLessThan(Weeks.ONE)) {
            return formatDays(daysBetween.getDays());
        }

        Months monthsBetween = Months.monthsBetween(publishedAt, now);

        if (monthsBetween.isLessThan(Months.ONE)) {
            return formatWeeks(weeksBetween.getWeeks());
        }

        Years yearsBetween = Years.yearsBetween(publishedAt, now);

        if (yearsBetween.isLessThan(Years.ONE)) {
            return formatMonths(monthsBetween.getMonths());
        }

        return formatYears(yearsBetween.getYears());
    }

    private String formatMinutes(long minutes) {
        return format(minutes, " minute ago", " minutes ago");
    }

    private String formatHours(long hours) {
        return format(hours, " hour ago", " hours ago");
    }

    private String formatDays(long days) {
        return format(days, " day ago", " days ago");
    }

    private String formatWeeks(long weeks) {
        return format(weeks, " week ago", " weeks ago");
    }

    private String formatMonths(long months) {
        return format(months, " month ago", " months ago");
    }

    private String formatYears(long years) {
        return format(years, " year ago", " years ago");
    }

    private String format(long hand, String singular, String plural) {

        if (hand == 1) {
            return hand + singular;
        }
        else {
            return hand + plural;
        }
    }

    @NonNull
    @Override
    public VideoDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommendation_row_item, parent, false);
        return new VideoDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoDetailsViewHolder holder, final int position) {

        //holder.avatar_image.setText(videoDetailsList.get(position).getSnippet().)

        String data = recommendedVideoList.get(position).getSnippet().getTitle();
        data = data.replace("&amp;", "&");
        data = data.replace("&#39;", "'");
        data = data.replace("&quot;", "'");

        holder.title.setText(data);
        holder.channelTitle.setText(recommendedVideoList.get(position).getSnippet().getChannelTitle());
        holder.publishedAt.setText(timestampFormatter(convertTimestamp(recommendedVideoList.get(position).getSnippet().getPublishedAt())));

        Glide.with(context).load(recommendedVideoList.get(position).getSnippet().getThumbnails().getMedium().getUrl())
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("videoID", recommendedVideoList.get(position).getId().getVideoId());
                intent.putExtra("videoTitle", recommendedVideoList.get(position).getSnippet().getTitle());
                intent.putExtra("author", recommendedVideoList.get(position).getSnippet().getChannelTitle());
                context.startActivity(intent);
            }
        });
    }

    private DateTime convertTimestamp(String publishedAt) {

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            dateTime = new DateTime(dateFormat.parse(publishedAt));
        }
        catch (ParseException exception) {
            Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        return dateTime;
    }

    @Override
    public int getItemCount() {
        return recommendedVideoList.size();
    }

    class VideoDetailsViewHolder extends RecyclerView.ViewHolder {

        private TextView channelTitle;
        private TextView publishedAt, title;
        private ImageView thumbnail;

        VideoDetailsViewHolder(View itemView) {
            super(itemView);

            channelTitle = itemView.findViewById(R.id.channelTitle);
            publishedAt = itemView.findViewById(R.id.publishedAt);
            title = itemView.findViewById(R.id.title);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }
}
