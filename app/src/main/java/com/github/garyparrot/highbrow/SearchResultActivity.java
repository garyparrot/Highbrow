package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.util.DBUtil;

import android.content.Intent;
import android.os.Bundle;

import com.github.garyparrot.highbrow.databinding.ActivitySearchResultBinding;
import com.github.garyparrot.highbrow.layout.adapter.StoryRecyclerAdapter;
import com.github.garyparrot.highbrow.model.hacker.news.algolia.StorySearchEntry;
import com.github.garyparrot.highbrow.module.ExecutorServiceModule;
import com.github.garyparrot.highbrow.room.HighbrowDatabase;
import com.github.garyparrot.highbrow.service.HackerNewsSearchService;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class SearchResultActivity extends AppCompatActivity {

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    HackerNewsSearchService hackerNewsSearchService;

    @Inject
    HighbrowDatabase database;

    @Inject
    Gson gson;

    @Inject
    @ExecutorServiceModule.IoExecutorService
    ExecutorService ioExecutorService;


    @Inject
    @ExecutorServiceModule.TaskExecutorService
    ExecutorService taskExecutorService;

    public static final String BUNDLE_QUERY_TEXT = "QUERY_TEXT";

    private ActivitySearchResultBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_result);
        binding.topAppBar.setNavigationOnClickListener((view) -> this.finish());

        Bundle extras = getIntent().getExtras();
        String query = extras.getString(BUNDLE_QUERY_TEXT);

        if(query != null) {
            binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
            hackerNewsSearchService.searchStory(query)
                    .subscribeOn(Schedulers.io())
                    .flatMap((res) -> {
                        List<Long> targetId = new ArrayList<>();
                        for (StorySearchEntry entry : res.getHits()) {
                            targetId.add(Long.valueOf(entry.getObjectID()));
                        }
                        return Single.just(targetId);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setAdapter, Throwable::printStackTrace);
        }
    }

    private void setAdapter(List<Long> targetId) {
        binding.recycleView.setAdapter(new StoryRecyclerAdapter(
                this,
                hackerNewsService,
                database.savedStory(),
                targetId,
                gson,
                ioExecutorService,
                taskExecutorService));
    }
}