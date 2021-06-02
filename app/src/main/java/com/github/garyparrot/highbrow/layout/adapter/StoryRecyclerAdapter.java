package com.github.garyparrot.highbrow.layout.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.garyparrot.highbrow.StoryActivity;
import com.github.garyparrot.highbrow.databinding.StoryCardViewBinding;
import com.github.garyparrot.highbrow.layout.view.StoryItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.MockItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StoryRecyclerAdapter extends RecyclerView.Adapter<StoryRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final List<Long> targetIds;
    private final HackerNewsService hackerNews;
    private final Context context;

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public StoryRecyclerAdapter(Context context, HackerNewsService service, List<Long> targets) {
        this.context = context;
        hackerNews = service;
        targetIds = targets;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private volatile int currentRequestId;
        private final StoryItem item;

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
            intent.putExtra(StoryActivity.BUNDLE_STORY_ID, item.getStory().getId());
            intent.putExtra(StoryActivity.BUNDLE_ARTICLE_URL, item.getStory().getUrl());
            context.startActivity(intent);
        });

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull final StoryRecyclerAdapter.ViewHolder holder, int position) {
        // Setup number and fake story, real story will replace the fake content after retrieve the real data
        holder.setNumber(position);
        holder.setStory(MockItem.getEmptyStory());

        // Allocate a request id for this binding action
        // If user scroll too fast, the target story of this request might be outdated
        // when current request finished(The view holder point to other new story).
        // So we need to use this number to determine if the request we will send
        // in next few line is outdated.
        // If so, don't apply the story.
        final int requestId = getNextRequestId();
        holder.setCurrentRequestId(requestId);

        // Request the real story and replace the mock story in async way
        hackerNews.getStory(targetIds.get(position))
                .addOnCompleteListener(task -> {
                    // Test to see if the view holder in this moment point to the right data
                    if(holder.getCurrentRequestId() == requestId) {
                        Story targetStory = task.getResult();
                        holder.setStory(targetStory);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return targetIds.size();
    }

}
