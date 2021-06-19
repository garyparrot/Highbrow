package com.github.garyparrot.highbrow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.garyparrot.highbrow.databinding.ActivityMainBinding;
import com.github.garyparrot.highbrow.layout.adapter.StoryRecyclerAdapter;
import com.github.garyparrot.highbrow.layout.view.StoryItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.module.ExecutorServiceModule;
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
import java.util.concurrent.ExecutorService;

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

    @Inject
    @ExecutorServiceModule.IoExecutorService
    ExecutorService ioExecutorService;

    private ActivityMainBinding binding;
    private HackerNewsService.StorySeries currentStorySeries;
    private ItemTouchHelper itemTouchHelper;

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
        setStorySeriesByMenuId(R.id.topStories);

        setupSearch();
        setupItemTouchHelper();
    }

    private void setupSearch() {
        SearchView searchView = (SearchView) binding.topAppBar.getMenu().findItem(R.id.searchView).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search story...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MainActivity.this.onSearchTextSubmit(query);
                binding.topAppBar.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    private void setupItemTouchHelper() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            private View getForegroundView(RecyclerView.ViewHolder vh) {
                return ((StoryRecyclerAdapter.ViewHolder) vh).itemView.findViewById(R.id.foregroundFrame);
            }

            private TextView getScrollLeftHintTextView(RecyclerView.ViewHolder vh) {
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
                if (viewHolder != null) {
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
                StoryRecyclerAdapter.ViewHolder holder = ((StoryRecyclerAdapter.ViewHolder) viewHolder);
                onStorySwiped(holder, direction);
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recycleView);

        this.itemTouchHelper = itemTouchHelper;
    ;}

    private void onStorySwiped(StoryRecyclerAdapter.ViewHolder viewHolder, int direction) {
        // Get the story from the View object
        Story story = ((StoryItem) viewHolder.itemView).getStory();
        SavedStory savedStory = SavedStory.from(story);

        // Save it into DB if story not exists yet, otherwise delete it.
        database.savedStory().isSavedStoryExists(story.getId())
                .subscribeOn(Schedulers.io())
                .flatMapMaybe((b) -> {
                    if (b)
                        return database.savedStory().delete(savedStory)
                                .andThen(Maybe.just(SaveResult.UNSAVED));
                    else
                        return database.savedStory().insert(savedStory)
                                .andThen(Maybe.just(SaveResult.SAVED));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((res) -> {
                    StoryRecyclerAdapter.ViewHolder customViewHolder = viewHolder;

                    // Update the cached saving state in view holder
                    customViewHolder.setSaved(!customViewHolder.getSaved());

                    // Hacky way to letting item swiped item move back:
                    // After the swipe event, the item will stuck outside of screen.
                    // According to my understanding, the ItemTouchHelper using the translationX
                    // to play the item swiping animation.
                    // The following code will let the swiped view move back to where it suppose to be,
                    // which act as a move back animation.
                    ObjectAnimator moveBack= ObjectAnimator.ofFloat(viewHolder.itemView.findViewById(R.id.foregroundFrame), "translationX", 0);
                    moveBack.setDuration(500);
                    // But that is not the end of the story, it feels like ItemTouchHelper store some kind
                    // of swiping state inside its object.
                    // After we doing these nasty thing outside the object's understanding, their own state is illegal
                    // now. Which is why we doing the following code to reset their state. So next time we
                    // swipe the item it won't breaks.
                    moveBack.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation, boolean isReverse) {
                            itemTouchHelper.attachToRecyclerView(null);
                            itemTouchHelper.attachToRecyclerView(binding.recycleView);
                        }
                    });
                    moveBack.start();

                    // Display a friend toast
                    Toast.makeText(MainActivity.this, res.toString(), Toast.LENGTH_SHORT).show();
                });

    }

    private void onSearchTextSubmit(String query) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.BUNDLE_QUERY_TEXT, query);
        startActivity(intent);
    }

    private boolean onNavigationViewItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        setStorySeriesByMenuId(menuItem.getItemId());
        binding.drawerLayout.closeDrawer(binding.navigationView);
        return true;
    }

    private void setStorySeriesByMenuId(int menuId) {
        if(menuId == R.id.topStories) {
            setActionBarTitle("Top Stories");
            switchStorySeries(hackerNewsService::topStoryIds);
        } else if(menuId == R.id.newStories) {
            setActionBarTitle("New Stories");
            switchStorySeries(hackerNewsService::newStoryIds);
        } else if(menuId == R.id.bestStories) {
            setActionBarTitle("Best Stories");
            switchStorySeries(hackerNewsService::bestStoryIds);
        } else if(menuId == R.id.askStories) {
            setActionBarTitle("Ask Stories");
            switchStorySeries(hackerNewsService::askStoryIds);
        } else if(menuId == R.id.showStories) {
            setActionBarTitle("Show Stories");
            switchStorySeries(hackerNewsService::showStoryIds);
        } else if(menuId == R.id.jobStories) {
            setActionBarTitle("Job Stories");
            switchStorySeries(hackerNewsService::jobStoryIDs);
        } else if(menuId == R.id.savedStories) {
            setActionBarTitle("Saved Stories");
            switchStorySeries(() -> {
                return Tasks.call(ioExecutorService, () -> database.savedStory().findAll()
                        .subscribeOn(Schedulers.io())
                        .map((x) -> {
                            ArrayList<Long> list = new ArrayList<>();
                            for (SavedStory savedStory : x) {
                                list.add(savedStory.getId());
                            }
                            return list;
                        }).blockingGet());
            });
        }
    }

    private void setActionBarTitle(int titleResourceId) {
        binding.topAppBar.setTitle(titleResourceId);
    }
    private void setActionBarTitle(String title) {
        binding.topAppBar.setTitle(title);
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