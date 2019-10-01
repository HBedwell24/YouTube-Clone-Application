package com.example.youtubeapiintegration.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeapiintegration.Credentials;
import com.example.youtubeapiintegration.R;

import java.util.Objects;

import javax.annotation.Nullable;

public class VideoFragment extends Fragment {

    private final String TAG = VideoFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private Credentials credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_content, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.recyclerview);
        credentials = new Credentials();
    }
}
