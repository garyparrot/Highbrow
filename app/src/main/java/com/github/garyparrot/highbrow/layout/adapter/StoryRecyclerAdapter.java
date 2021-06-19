package com.github.garyparrot.highbrow.layout.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.garyparrot.highbrow.StoryActivity;
import com.github.garyparrot.highbrow.layout.view.StoryItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.room.dao.SavedStoryDao;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.HackerNewsItemUtility;
import com.github.garyparrot.highbrow.util.MapUtility;
import com.github.garyparrot.highbrow.util.SaveResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class StoryRecyclerAdapter extends RecyclerView.Adapter<StoryRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final Map<Long, Task<Story>> storyStorage = Collections.synchronizedMap(new HashMap<>());
    private final Map<Long, Boolean> storyIsSaved = Collections.synchronizedMap(new HashMap<>());
    private final List<Long> targetIds;
    private final HackerNewsService hackerNews;
    private final SavedStoryDao savedStoryDao;
    private final Context context;
    private final Gson gson;
    private final ExecutorService taskExecutorService;
    private final ExecutorService ioExecutorService;

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public StoryRecyclerAdapter(Context context, HackerNewsService service, SavedStoryDao dao, List<Long> targets, Gson gson, ExecutorService ioExecutorService, ExecutorService taskExecutorService) {
        this.context = context;
        this.savedStoryDao = dao;
        hackerNews = service;
        targetIds = targets;
        this.gson = gson;
        this.taskExecutorService = taskExecutorService;
        this.ioExecutorService = ioExecutorService;
    }

    public void askToFetchPosition(int i) {
        if(!storyStorage.containsKey(targetIds.get(i))) {
            Timber.d("Asked to preload story %d (index %d)", targetIds.get(i), i);
            loadStory(targetIds.get(i));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private volatile int currentRequestId;
        private final StoryItem item;
        private boolean isSaved;

        public ViewHolder(StoryItem item) {
            super(item);
            this.item = item;
        }

        synchronized void setCurrentRequestId(int requestId)  {
            this.currentRequestId = requestId;
        }
        synchronized int getCurrentRequestId() {
            return currentRequestId;
        }

        public boolean getSaved() { return this.isSaved; }
        public void setSaved(boolean isSaved) { this.isSaved = isSaved; }
        public SaveResult getSavedResult() {
            return isSaved ? SaveResult.SAVED : SaveResult.UNSAVED;
        }
        void setNumber(int number) {
            item.setNumber(number);
        }
        void setStory(Story story) {
            item.setStory(story);
        }
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        StoryItem item = new StoryItem(parent.getContext());
        item.setOnClickListener((view) -> {
            Intent intent = new Intent(context, StoryActivity.class);
            intent.putExtra(StoryActivity.BUNDLE_STORY_JSON, gson.toJson(item.getStory()));
            context.startActivity(intent);
        });

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull final StoryRecyclerAdapter.ViewHolder holder, int position) {
        final long storyId = targetIds.get(position);
        final Task<Story> storyTask = loadStory(storyId);

        holder.setNumber(position);
        holder.setSaved(MapUtility.getOrDefault(storyIsSaved, storyId, false));
        if(storyTask.isSuccessful() && storyTask.getResult() != null)
            holder.setStory(storyTask.getResult());
        else {
            // If story not resolved yet, we add a listener to wait for the result
            // Once we got the result, calling ``Adapter::notifyItemChanged`` to ask RecyclerView to update that item
            holder.setStory(HackerNewsItemUtility.getEmptyStory());
            storyTask.continueWithTask(ioExecutorService, (x) -> {
                        return Tasks.call(() -> savedStoryDao.isSavedStoryExists(targetIds.get(position))
                                .subscribeOn(Schedulers.io())
                                .blockingGet());
                    }).addOnSuccessListener(task -> {
                notifyItemChanged(position);
            });
        }
    }

    private Task<Story> loadStory(long id) {
        synchronized (storyStorage) {
            if(storyStorage.containsKey(id))
                return storyStorage.get(id);
            return newStoryTask(id);
        }
    }

    private Task<Story> newStoryTask(Long aLong) {
        Task<Story> story = hackerNews.getStory(aLong);
        storyStorage.put(aLong, story);
        return story;
    }

    @Override
    public int getItemCount() {
        return targetIds.size();
    }

}
