package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.databinding.ActivityStoryBinding;
import com.github.garyparrot.highbrow.layout.adapter.CommentRecyclerAdapter;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.service.HackerNewsService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryActivity extends AppCompatActivity {

    @Inject
    HackerNewsService hackerNewsService;

    public static final String BUNDLE_STORY_ID = "BUNDLE_STORY_ID";

    private long storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStoryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_story);

        Bundle bundle = getIntent().getExtras();

        storyId = bundle.getLong(BUNDLE_STORY_ID);

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        hackerNewsService.getStory(storyId)
                .addOnCompleteListener(task -> {
                    Story story = task.getResult();
                    binding.topAppBar.setTitle(story.getTitle());

                    List<Long> comments = story.getKids();
                    binding.recycleView.setAdapter(new CommentRecyclerAdapter(StoryActivity.this, hackerNewsService, comments));
                });
    }
}