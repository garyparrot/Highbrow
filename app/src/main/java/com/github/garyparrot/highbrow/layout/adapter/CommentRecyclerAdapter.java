package com.github.garyparrot.highbrow.layout.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.garyparrot.highbrow.layout.view.CommentItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.MockItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final List<Long> targetIds;
    private final HackerNewsService hackerNews;
    private final Context context;

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public CommentRecyclerAdapter(Context context, HackerNewsService service, List<Long> targets) {
        this.context = context;
        hackerNews = service;
        targetIds = targets;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private volatile int currentRequestId;
        private final CommentItem item;

        public ViewHolder(CommentItem item) {
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
        void setComment(Comment comment) {
            item.setComment(comment);
        }

    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        CommentItem item = new CommentItem(parent.getContext());

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentRecyclerAdapter.ViewHolder holder, int position) {
        // Setup number and fake story, real story will replace the fake content after retrieve the real data
        holder.setNumber(position);
        holder.setComment(MockItem.getEmptyComment());

        // Allocate a request id for this binding action
        // If user scroll too fast, the target story of this request might be outdated
        // when current request finished(The view holder point to other new story).
        // So we need to use this number to determine if the request we will send
        // in next few line is outdated.
        // If so, don't apply the story.
        final int requestId = getNextRequestId();
        holder.setCurrentRequestId(requestId);

        // Request the real story and replace the mock story in async way
        hackerNews.getComment(targetIds.get(position))
                .addOnCompleteListener(task -> {
                    // Test to see if the view holder in this moment point to the right data
                    if(holder.getCurrentRequestId() == requestId) {
                        Comment targetComment = task.getResult();
                        holder.setComment(targetComment);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return targetIds.size();
    }

}
