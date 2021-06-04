package com.github.garyparrot.highbrow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.layout.adapter.StoryRecyclerAdapter;
import com.github.garyparrot.highbrow.layout.view.StoryItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.module.FirebaseDatabaseModule;
import com.github.garyparrot.highbrow.room.HighbrowDatabase;
import com.github.garyparrot.highbrow.room.entity.SavedStory;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.LogUtility;
import com.github.garyparrot.highbrow.util.SaveResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity{

    @Inject
    @FirebaseDatabaseModule.HackerNews
    DatabaseReference hackerNewsDatabaseReference;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    HighbrowDatabase database;

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
        binding.topAppBar.setNavigationOnClickListener((view) -> binding.drawerLayout.openDrawer(binding.navigationView));
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationViewItemSelected);
        binding.navigationView.getMenu().findItem(R.id.topStories).setChecked(true);
        switchStorySeries(hackerNewsService::topStoryIds);

        setupItemTouchHelper();
    }

    private void setupItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            private View getForegroundView(RecyclerView.ViewHolder vh) {
                return ((StoryRecyclerAdapter.ViewHolder) vh).itemView.findViewById(R.id.foregroundFrame);
            }

            private TextView getScrollLeftHintTextView(RecyclerView.ViewHolder vh){
                return vh.itemView.findViewById(R.id.textViewScrollLeftHint);
            }

            private StoryRecyclerAdapter.ViewHolder getCustomViewHolder(RecyclerView.ViewHolder vh) {
                return ((StoryRecyclerAdapter.ViewHolder) vh);
            }

            private void setScrollHintTextStyle(RecyclerView.ViewHolder vh) {
                StoryRecyclerAdapter.ViewHolder customViewHolder = getCustomViewHolder(vh);
                SaveResult currentSaveResult = customViewHolder.getSavedResult();
                TextView textView = getScrollLeftHintTextView(vh);
                switch (currentSaveResult) {
                    case SAVED:
                        textView.setText(SaveResult.UNSAVED.getActionInfo());
                        textView.setTextColor(getResources().getColor(R.color.ATTEMPT_UNSAVE_STORY_COLOR));
                        break;
                    case UNSAVED:
                        textView.setText(SaveResult.SAVED.getActionInfo());
                        textView.setTextColor(getResources().getColor(R.color.ATTEMPT_STORY_COLOR));
                        break;
                }
            }

            @Override
            public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                if(viewHolder != null) {
                    getDefaultUIUtil().onSelected(getForegroundView(viewHolder));
                    setScrollHintTextStyle(viewHolder);
                }
            }

            @Override
            public void clearView(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView(getForegroundView(viewHolder));
            }

            @Override
            public void onChildDraw(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                getDefaultUIUtil().onDraw(c, recyclerView, getForegroundView(viewHolder), dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void onChildDrawOver(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                getDefaultUIUtil().onDrawOver(c, recyclerView, getForegroundView(viewHolder), dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                binding.recycleView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());

                // Get the story from the View object
                Story story = ((StoryItem) viewHolder.itemView).getStory();
                SavedStory savedStory = SavedStory.from(story);

                // Save it into DB if story not exists yet, otherwise delete it.
                database.savedStory().isSavedStoryExists(story.getId())
                        .subscribeOn(Schedulers.io())
                        .flatMapMaybe((b) -> {
                            if(b)
                                return database.savedStory().delete(savedStory)
                                            .andThen(Maybe.just(SaveResult.UNSAVED));
                            else
                                return database.savedStory().insert(savedStory)
                                            .andThen(Maybe.just(SaveResult.SAVED));
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((res) -> {
                            StoryRecyclerAdapter.ViewHolder customViewHolder = getCustomViewHolder(viewHolder);
                            customViewHolder.setSaved(!customViewHolder.getSaved());
                            Toast.makeText(MainActivity.this, res.toString(), Toast.LENGTH_SHORT).show();
                        });
            }
        }).attachToRecyclerView(binding.recycleView);
    }

    private boolean onNavigationViewItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);

        if(menuItem.getItemId() == R.id.topStories)
            switchStorySeries(hackerNewsService::topStoryIds);
        else if(menuItem.getItemId() == R.id.newStories)
            switchStorySeries(hackerNewsService::newStoryIds);
        else if(menuItem.getItemId() == R.id.bestStories)
            switchStorySeries(hackerNewsService::bestStoryIds);
        else if(menuItem.getItemId() == R.id.askStories)
            switchStorySeries(hackerNewsService::askStoryIds);
        else if(menuItem.getItemId() == R.id.showStories)
            switchStorySeries(hackerNewsService::showStoryIds);
        else if(menuItem.getItemId() == R.id.jobStories)
            switchStorySeries(hackerNewsService::jobStoryIDs);
        else if(menuItem.getItemId() == R.id.savedStories) {
            database.savedStory().findAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((list) -> {
                        List<Long> idList = new ArrayList<Long>();
                        for (SavedStory savedStory : list) {
                            idList.add(savedStory.getId());
                        }
                        switchStorySeries(() -> Tasks.forResult(idList));
                    }, (e) -> {
                        e.printStackTrace();
                    });
        }

        binding.drawerLayout.closeDrawer(binding.navigationView);
        return true;
    }

    private Task<List<Long>> switchStorySeries(HackerNewsService.StorySeries seriesMethod) {
        currentStorySeries = seriesMethod;
        return seriesMethod.call()
                .addOnCompleteListener(task -> {
                    List<Long> series = task.getResult();
                    StoryRecyclerAdapter newAdapter = new StoryRecyclerAdapter(this, hackerNewsService, database.savedStory(), series, gson);
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