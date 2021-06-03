package com.github.garyparrot.highbrow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.layout.adapter.StoryRecyclerAdapter;
import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.LogUtility;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    @FirebaseDatabaseModule.HackerNews
    DatabaseReference hackerNewsDatabaseReference;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    Gson gson;

    private ActivityMainBinding binding;
    private HackerNewsService.StorySeries currentStorySeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_main);

        LogUtility.setupTimber();

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        binding.swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        switchStorySeries(hackerNewsService::topStoryIds);
    }

    private Task<List<Long>> switchStorySeries(HackerNewsService.StorySeries seriesMethod) {
        currentStorySeries = seriesMethod;
        return seriesMethod.call()
                .addOnCompleteListener(task -> {
                    List<Long> series = task.getResult();
                    StoryRecyclerAdapter newAdapter = new StoryRecyclerAdapter(this, hackerNewsService, series, gson);
                    binding.recycleView.setAdapter(newAdapter);
                });
    }

    private void onRefresh() {
        switchStorySeries(currentStorySeries)
                .addOnCompleteListener((x) -> {
                    binding.swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener((x) -> {
                    Toast.makeText(this, "Failed to refresh stories", Toast.LENGTH_SHORT).show();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
    }
}