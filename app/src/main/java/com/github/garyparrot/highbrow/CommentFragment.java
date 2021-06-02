package com.github.garyparrot.highbrow;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.garyparrot.highbrow.databinding.FragmentCommentBinding;
import com.github.garyparrot.highbrow.layout.adapter.CommentRecyclerAdapter;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.service.HackerNewsService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class CommentFragment extends Fragment {

    public static final String BUNDLE_STORY_ID = "BUNDLE_STORY_ID";

    @Inject
    HackerNewsService hackerNewsService;

    public CommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param storyId The id of the story we want to display on this fragment.
     * @return A new instance of fragment CommentFragment.
     */
    public static CommentFragment newInstance(long storyId) {
        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(BUNDLE_STORY_ID, storyId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentCommentBinding binding = FragmentCommentBinding.inflate(inflater, container, false);

        binding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        Bundle bundle = getArguments();
        long storyId = bundle.getLong(BUNDLE_STORY_ID);

        hackerNewsService.getStory(storyId)
                .addOnCompleteListener(task -> {
                    Story story = task.getResult();
                    List<Long> comments = story.getKids();
                    binding.recycleView.setAdapter(new CommentRecyclerAdapter(CommentFragment.this.getContext(), hackerNewsService, comments));
                });

        return binding.getRoot();
    }
}