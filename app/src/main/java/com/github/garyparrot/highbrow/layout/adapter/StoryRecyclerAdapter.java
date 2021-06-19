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
import com.github.garyparrot.highbrow.util.SaveResult;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class StoryRecyclerAdapter extends RecyclerView.Adapter<StoryRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final Map<Long, Task<Story>> storyStorage = Collections.synchronizedMap(new HashMap<>());
    private final List<Long> targetIds;
    private final HackerNewsService hackerNews;
    private final SavedStoryDao savedStoryDao;
    private final Context context;
    private final Gson gson;

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public StoryRecyclerAdapter(Context context, HackerNewsService service, SavedStoryDao dao, List<Long> targets, Gson gson) {
        this.context = context;
        this.savedStoryDao = dao;
        hackerNews = service;
        targetIds = targets;
        this.gson = gson;
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
        // Setup number and fake story, real story will replace the fake content after retrieve the real data
        holder.setNumber(position);
        holder.setStory(HackerNewsItemUtility.getEmptyStory());

        // Allocate a request id for this binding action
        // If user scroll too fast, the target story of this request might be outdated
        // when current request finished(The view holder point to other new story).
        // So we need to use this number to determine if the request we will send
        // in next few line is outdated.
        // If so, don't apply the story.
        final int requestId = getNextRequestId();
        holder.setCurrentRequestId(requestId);


        synchronized (storyStorage) {
            Task<Story> storyTask;

            if(storyStorage.containsKey(targetIds.get(position)))
                storyTask= storyStorage.get(targetIds.get(position));
            else
                storyTask = newStoryTask(targetIds.get(position));
            // Request the real story and replace the mock story in async way

           storyTask.addOnCompleteListener(task -> {
                // Test to see if the view holder in this moment point to the right data
                if (holder.getCurrentRequestId() == requestId) {
                    Story targetStory = task.getResult();
                    if (targetStory == null) {
                        Timber.e("Bad story at id %d", targetIds.get(position));
                        return;
                    }
                    holder.setStory(targetStory);

                    // Update saved info
                    savedStoryDao.isSavedStoryExists(targetIds.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((isSaved) -> {
                                if (holder.getCurrentRequestId() == requestId)
                                    holder.setSaved(isSaved);
                            }, Throwable::printStackTrace);
                }
            });
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
