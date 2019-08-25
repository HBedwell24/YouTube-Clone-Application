package com.example.youtubeapiintegration.Adapter;

import android.app.Activity;
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
import com.example.youtubeapiintegration.Models.VideoStats.Item;
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
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class VideoStatsAdapter extends RecyclerView.Adapter<VideoStatsAdapter.VideoStatsViewHolder> {

    private Activity activity;
    private List<Item> videoStatsList;
    private DateTime dateTime;

    public VideoStatsAdapter(Activity activity, List<Item> videoStatsList) {
        this.activity = activity;
        this.videoStatsList = videoStatsList;
    }

    @NonNull
    @Override
    public VideoStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.row_item, parent, false);
        return new VideoStatsViewHolder(view);
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

    @Override
    public void onBindViewHolder(@NonNull VideoStatsViewHolder holder, final int position) {

        String data = videoStatsList.get(position).getSnippet().getTitle();
        data = data.replace("&amp;", "&");
        data = data.replace("&#39;", "'");
        data = data.replace("&quot;", "'");

        holder.title.setText(data);
        holder.channelTitle.setText(videoStatsList.get(position).getSnippet().getChannelTitle());
        holder.views.setText(format(Long.parseLong(videoStatsList.get(position).getStatistics().getViewCount())));
        holder.publishedAt.setText(timestampFormatter(convertTimestamp(videoStatsList.get(position).getSnippet().getPublishedAt())));

        Glide.with(activity).load(videoStatsList.get(position).getSnippet().getThumbnails().
                getHigh().getUrl()).into(holder.thumbnail);

        final String finalData = data;
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, VideoActivity.class);
                intent.putExtra("videoID", videoStatsList.get(position).getId());
                intent.putExtra("videoTitle", finalData);
                intent.putExtra("description", videoStatsList.get(position).getSnippet().getDescription());
                intent.putExtra("views", videoStatsList.get(position).getStatistics().getViewCount());
                intent.putExtra("likes", videoStatsList.get(position).getStatistics().getLikeCount());
                intent.putExtra("dislikes", videoStatsList.get(position).getStatistics().getDislikeCount());
                intent.putExtra("author", videoStatsList.get(position).getSnippet().getChannelTitle());
                activity.startActivityForResult(intent, 0);
            }
        });
    }

    private DateTime convertTimestamp(String publishedAt) {

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            dateTime = new DateTime(dateFormat.parse(publishedAt));
        }
        catch (ParseException exception) {
            Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        return dateTime;
    }

    @Override
    public int getItemCount() {
        return videoStatsList.size();
    }

    class VideoStatsViewHolder extends RecyclerView.ViewHolder {

        private TextView channelTitle, publishedAt, title, views;
        private ImageView thumbnail;

        VideoStatsViewHolder(View itemView) {
            super(itemView);

            channelTitle = itemView.findViewById(R.id.channelTitle);
            publishedAt = itemView.findViewById(R.id.publishedAt);
            title = itemView.findViewById(R.id.title);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            views = itemView.findViewById(R.id.views);
        }
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

    private static String format(long value) {

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
}

