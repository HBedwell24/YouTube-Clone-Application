package com.example.youtubeapiintegration.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.youtubeapiintegration.Models.Comments.Comment;
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

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context context;
    private List<Comment.Item> commentsList;
    private DateTime dateTime;

    public CommentsAdapter(Context context, List<Comment.Item> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
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
    public CommentsAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_row_item, parent, false);
        return new CommentsAdapter.CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentsViewHolder holder, int position) {
        holder.username.setText(commentsList.get(position).getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName());
        holder.comment.setText(commentsList.get(position).getSnippet().getTopLevelComment().getSnippet().getTextOriginal());
        holder.publishedAt.setText(timestampFormatter(convertTimestamp(commentsList.get(position).getSnippet().getTopLevelComment().getSnippet().getPublishedAt())));
        Glide.with(context).load(commentsList.get(position).getSnippet().getTopLevelComment().getSnippet().getAuthorProfileImageUrl())
                .into(holder.user_thumbnail);
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
        return commentsList.size();
    }

    class CommentsViewHolder extends RecyclerView.ViewHolder {

        private ImageView user_thumbnail;
        private TextView username, comment, publishedAt;

        CommentsViewHolder(View itemView) {
            super(itemView);

            user_thumbnail = itemView.findViewById(R.id.user_thumbnail);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            publishedAt = itemView.findViewById(R.id.publishedAt);
        }
    }
}
