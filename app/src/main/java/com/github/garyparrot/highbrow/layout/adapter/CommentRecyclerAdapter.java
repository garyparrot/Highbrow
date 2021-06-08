package com.github.garyparrot.highbrow.layout.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.garyparrot.highbrow.layout.view.CommentItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneraComment;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.MockItem;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import timber.log.Timber;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private Map<Long, Task<Comment>> commentStorage;
    private Map<Long, Integer> commentDepth;
    private final Story story;
    private final List<Long> targetIds;
    private final HackerNewsService hackerNews;
    private final Context context;

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public CommentRecyclerAdapter(Context context, HackerNewsService service, Story story) {
        this.context = context;
        this.commentStorage = Collections.synchronizedMap(new HashMap<>());
        this.commentDepth = Collections.synchronizedMap(new HashMap<>());
        this.story = story;
        hackerNews = service;
        targetIds = Collections.synchronizedList(new ArrayList<>(story.getKids()));
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
        void setIndent(int level) { item.setIndentLevel(level); }

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

        long commentId = targetIds.get(position);

        if(!commentStorage.containsKey(commentId)) {
            launchCommentDownloadTask(commentId);
        }

        Task<Comment> commentTask = commentStorage.get(commentId);
        Objects.requireNonNull(commentTask);
        if(commentTask.isComplete()) {
            holder.setComment(commentTask.getResult());
            holder.setIndent(commentDepth.get(commentTask.getResult().getId()));
        } else {
            GeneraComment comment = GeneraComment.builder()
                    .author("")
                    .id(0)
                    .kids(Collections.emptyList())
                    .parentId(0)
                    .text(String.valueOf(commentId))
                    .time(0)
                    .itemType(ItemType.Comment)
                    .build();
            holder.setComment(comment);
            holder.setIndent(0);
        }
        Timber.d("Current items: %d", targetIds.size());
    }

    @Override
    public int getItemCount() {
        synchronized (targetIds) {
            return (int) targetIds.size();
        }
    }

    private Task<Comment> launchCommentDownloadTask(long commentId) {
        if(commentStorage.containsKey(commentId))
            return commentStorage.get(commentId);

        Task<Comment> commentTask = launchCommentDownloadTaskInternal(commentId);
        commentStorage.put(commentId, commentTask);

        return commentTask;
    }
    private Task<Comment> launchCommentDownloadTaskInternal(long commentId) {
        return hackerNews.getComment(commentId)
                .continueWith(taskInstance -> {
                    Comment comment = taskInstance.getResult();
                    if(comment.getParentId() == story.getId()) {
                        commentDepth.put(comment.getId(), 0);
                    } else {
                        commentDepth.put(comment.getId(), commentDepth.get(comment.getParentId()) + 1);
                    }
                    return taskInstance.getResult();
                })
                .addOnSuccessListener((comment) -> {
                    synchronized (targetIds) {
                        int index = targetIds.indexOf(comment.getId());
                        notifyItemChanged(index);
                        targetIds.addAll(index + 1, comment.getKids());
                        notifyItemRangeInserted(index + 1, comment.getKids().size());
                    }
                });
    }
}
