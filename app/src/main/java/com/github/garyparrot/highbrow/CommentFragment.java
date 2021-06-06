package com.github.garyparrot.highbrow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.garyparrot.highbrow.databinding.FragmentCommentBinding;
import com.github.garyparrot.highbrow.layout.adapter.CommentRecyclerAdapter;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapStory;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class CommentFragment extends Fragment {

    public static final String BUNDLE_STORY_JSON = "BUNDLE_STORY_JSON";

    @Inject
    EventBus eventBus;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    Gson gson;

    CommentRecyclerAdapter adapter;
    FragmentCommentBinding binding;

    public CommentFragment() { }

    public static CommentFragment newInstance(Gson gson, Story story) {
        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle();
        Timber.d(story.toString());
        bundle.putString(BUNDLE_STORY_JSON, gson.toJson(story));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        Objects.requireNonNull(bundle);
        Timber.d(bundle.getString(BUNDLE_STORY_JSON));
        Story story = gson.fromJson(bundle.getString(BUNDLE_STORY_JSON), GeneralStory.class);

        adapter = new CommentRecyclerAdapter(getContext(), hackerNewsService, story.getKids());
        binding.recycleView.setAdapter(adapter);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

}