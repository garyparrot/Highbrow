package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.layout.adapter.StoryRecyclerAdapter;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryEntry;
import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.service.UrbanDictionaryService;
import com.github.garyparrot.highbrow.util.LogUtility;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    @FirebaseDatabaseModule.HackerNews
    DatabaseReference hackerNewsDatabaseReference;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        LogUtility.setupTimber();

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        hackerNewsService.topStoryIds()
                .addOnCompleteListener(topStoriesTask -> {
                    List<Long> topStories = topStoriesTask.getResult();
                    StoryRecyclerAdapter adapter = new StoryRecyclerAdapter(this, hackerNewsService, topStories, gson);
                    binding.recycleView.setAdapter(adapter);
                });
    }
}