package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.layout.view.StoryItem;
import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.LogUtility;
import com.google.firebase.database.DatabaseReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    @FirebaseDatabaseModule.HackerNews
    DatabaseReference hackerNewsDatabaseReference;

    @Inject
    HackerNewsService hackerNewsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        LogUtility.setupTimber();

        hackerNewsService.topStoryIds()
                .addOnCompleteListener(x -> {
                    int number = 1;
                    for (Long aLong : x.getResult()) {
                        final int refNumbering = number++;
                        hackerNewsService.getStory(aLong).addOnCompleteListener(story -> {
                            StoryItem storyItem = new StoryItem(this);
                            storyItem.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            storyItem.setStory(story.getResult());
                            storyItem.setNumber(refNumbering);
                            binding.storyList.addView(storyItem);
                        });
                    }
                });
    }
}