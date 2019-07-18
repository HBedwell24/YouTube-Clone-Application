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
import com.example.youtubeapiintegration.Models.Item;
import com.example.youtubeapiintegration.R;
import com.example.youtubeapiintegration.Video;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoDetailsAdapter extends RecyclerView.Adapter<VideoDetailsAdapter.VideoDetailsViewHolder> {

    private Context context;
    private List<Item> videoDetailsList;
    private String convertedDate;

    public VideoDetailsAdapter(Context context, List<Item> videoDetailsList) {
        this.context = context;
        this.videoDetailsList = videoDetailsList;
    }

    @NonNull
    @Override
    public VideoDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false);
        return new VideoDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoDetailsViewHolder holder, final int position) {

        String data = videoDetailsList.get(position).getSnippet().getTitle();
        data = data.replace("&amp;", "&");
        data = data.replace("&#39;", "'");
        data = data.replace("&quot;", "'");

        holder.title.setText(data);
        holder.channelTitle.setText(videoDetailsList.get(position).getSnippet().getChannelTitle());
        holder.publishedAt.setText(convertTimestamp(videoDetailsList.get(position).getSnippet().getPublishedAt()));
        holder.description.setText(videoDetailsList.get(position).getSnippet().getDescription());

        Glide.with(context)
                .load(videoDetailsList
                        .get(position)
                        .getSnippet().getThumbnails().getMedium().getUrl())
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Video.class);
                intent.putExtra("videoID", videoDetailsList.get(position).getId().getVideoId());
                intent.putExtra("videoTitle", videoDetailsList.get(position).getSnippet().getTitle());
                context.startActivity(intent);
            }
        });
    }

    private String convertTimestamp(String publishedAt) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm a");
            Date date = dateFormat.parse(publishedAt);
            convertedDate = format.format(date);
        }
        catch (ParseException exception) {
            Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        return convertedDate;
    }

    @Override
    public int getItemCount() {
        return videoDetailsList.size();
    }

    public class VideoDetailsViewHolder extends RecyclerView.ViewHolder {

        private TextView channelTitle;
        private TextView publishedAt, title, description;
        private ImageView thumbnail;

        public VideoDetailsViewHolder(View itemView) {
            super(itemView);

            channelTitle = itemView.findViewById(R.id.channelTitle);
            publishedAt = itemView.findViewById(R.id.publishedAt);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }
}
