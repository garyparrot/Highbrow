package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.LogUtility;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
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
        setContentView(R.layout.activity_main);

        LogUtility.setupTimber();

        hackerNewsService.bestStoryIds()
                .addOnCompleteListener(x -> {
                    for (Long aLong : x.getResult()) {
                        hackerNewsService.getStory(aLong).addOnCompleteListener(story -> {
                            Timber.i(story.getResult().getTitle());

                        });
                    }
                });
    }
}