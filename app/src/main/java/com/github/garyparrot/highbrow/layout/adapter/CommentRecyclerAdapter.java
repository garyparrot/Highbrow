package com.github.garyparrot.highbrow.layout.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.garyparrot.highbrow.layout.util.CommentPlaceholder;
import com.github.garyparrot.highbrow.layout.view.CommentItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Item;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneraComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.MockItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import timber.log.Timber;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final Map<Long, Task<Comment>> commentStorage;
    private final Map<Long, Integer> commentDepth;
    private final Story story;
    /**
     * The list of comment id in recycler view.
     *
     * suppose there are only two type of number in this list: {@link Long} or {@link com.github.garyparrot.highbrow.layout.util.CommentPlaceholder}.
     */
    private final List<Number> targetIds;
    private final HackerNewsService hackerNews;
    private final Context context;
    private final ExecutorService downloadExecutorService;
    private final ExecutorService commentPreDownloadExecutorService;
    private final AtomicBoolean isWarmUpDone = new AtomicBoolean();

    private static int getNextRequestId() {
        return nextRequestId++;
    }

    public CommentRecyclerAdapter(Context context, HackerNewsService service, ExecutorService downloadExecutorService, Story story) {
        this.context = context;
        this.commentStorage = Collections.synchronizedMap(new HashMap<>());
        this.commentDepth = Collections.synchronizedMap(new HashMap<>());
        this.story = story;
        this.downloadExecutorService = downloadExecutorService;
        this.commentPreDownloadExecutorService = Executors.newSingleThreadExecutor();

        hackerNews = service;
        targetIds = Collections.synchronizedList(new ArrayList<>());

        commentPreDownloadExecutorService.submit(new CommentWarmUpLogic(story, 3, 30));
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

        public void setPlaceholderMode(boolean b) {
            item.setPlaceholderMode(b);
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
        Timber.d("Binding for item at position %d", position);
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

        Number number = targetIds.get(position);
        long commentId = targetIds.get(position).longValue();

        if(!commentStorage.containsKey(commentId)) {
            launchCommentDownloadTask(commentId);
        }

        Task<Comment> commentTask = commentStorage.get(commentId);
        Objects.requireNonNull(commentTask);
        if(commentTask.isComplete()) {
            holder.setComment(commentTask.getResult());
            holder.setIndent(commentDepth.get(commentTask.getResult().getId()));
            if(number instanceof CommentPlaceholder)
                holder.setPlaceholderMode(true);
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
            if(isWarmUpDone.get())
                return (int) targetIds.size();
            else
                return 0;
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
                .continueWith(downloadExecutorService, taskInstance -> {
                    Comment comment = taskInstance.getResult();
                    if(comment.getParentId() == story.getId()) {
                        commentDepth.put(comment.getId(), 0);
                    } else {
                        commentDepth.put(comment.getId(), commentDepth.get(comment.getParentId()) + 1);
                    }
                    return taskInstance.getResult();
                })
                .addOnSuccessListener((comment) -> {
                    // During warm up process, we won't notify adapter that there is a change.
                    // Frequently change will likely to cause CPU intensive re-draw event on main thread
                    if(isWarmUpDone.get()) {
                        int index;
                        // No need to sync this since we are running on main thread at this moment.
                        index = targetIds.indexOf(comment.getId());
                        targetIds.addAll(index + 1, comment.getKids());

                        notifyItemChanged(index);
                        notifyItemRangeInserted(index + 1, comment.getKids().size());
                    }
                });
    }

    class CommentWarmUpLogic implements Runnable {

        final HaveKids root;
        final int warmUpCommentSize;
        final int downloadLimit;

        CommentWarmUpLogic(HaveKids root) {
            this(root, root.getKids().size(), Integer.MAX_VALUE);
        }
        CommentWarmUpLogic(HaveKids root, int warmUp, int downloadLimit) {
            this.root = root;
            this.warmUpCommentSize = warmUp;
            this.downloadLimit = downloadLimit;
        }

        @Getter
        @AllArgsConstructor
        class DownloadResult {
            final List<Long> nextCandidates;
            final long downloadLaunchedCount;
        }

        @Override
        public void run() {
            // Only attempt to download top n'th comments in this story
            int warmUpCandidateSize = Math.min(root.getKids().size(), this.warmUpCommentSize);
            // Only attempt to send at most n download request
            int downloadLimit = this.downloadLimit;

            int round = 0;

            try {
                // Perform warm up
                List<Long> warmUpCandidate = root.getKids().subList(0, warmUpCandidateSize);
                while(warmUpCandidate.size() > 0 && downloadLimit > 0) {
                    Timber.d("Warm up round #%d: candidates %d, remaining quota %d", ++round, warmUpCandidate.size(), downloadLimit);
                    DownloadResult downloadResult = preDownloadComments(warmUpCandidate, downloadLimit);
                    warmUpCandidate = downloadResult.nextCandidates;
                    downloadLimit -= downloadResult.downloadLaunchedCount;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                List<Long> preloadedComments = new ArrayList<>();
                List<Long> topPart = root.getKids().subList(0, warmUpCandidateSize);
                List<Long> bottomPart = root.getKids().subList(warmUpCandidateSize, root.getKids().size());

                for (Long aLong : topPart) {
                    Comment comment = commentStorage.get(aLong).getResult();
                    addCommentToListInDfsOrder(preloadedComments, comment);
                }

                targetIds.addAll(preloadedComments);
                targetIds.addAll(bottomPart);

                isWarmUpDone.set(true);
                Tasks.call(() -> { notifyItemRangeChanged(0,targetIds.size()); return 0; });
            }
        }

        /**
         * Pre-download the given list of comment from HackerNews.
         *
         * This method will block current thread until all given comment is completed(both succeed one and
         * failed one). This method won't retry failed comment download attempt.
         * @param candidates the given id list of comment that want to pre-download
         * @return the next candidate, which is their child comments
         * @throws InterruptedException if thread get interrupt
         */
        private DownloadResult preDownloadComments(List<Long> candidates, int downloadLimit) throws InterruptedException {
            final List<Long> children = Collections.synchronizedList(new ArrayList<>());
            int launchedDownloadCount = 0;
            CountDownLatch latch = new CountDownLatch(Math.min(candidates.size(), downloadLimit));
            for (Long candidate : candidates) {

                if(launchedDownloadCount < downloadLimit) {
                    launchedDownloadCount++;
                } else {
                    break; // due to download limit exceed.
                }

                launchCommentDownloadTask(candidate)
                        .addOnSuccessListener(downloadExecutorService, (c) -> {
                            children.addAll(c.getKids());
                            latch.countDown();
                        })
                        .addOnFailureListener(downloadExecutorService, (e) -> {
                            e.printStackTrace();
                            latch.countDown();
                        });
            }
            latch.await();

            return new DownloadResult(children, launchedDownloadCount);
        }

        private <T extends Item & HaveKids> void addCommentToListInDfsOrder(List<Long> targetList, T root) {
            // Add myself
            targetList.add(root.getId());

            // DFS transversal
            for (Long kid : root.getKids()) {
                Task<Comment> commentTask = commentStorage.get(kid);
                if(commentTask == null) {
                    continue;       // This comment is not load.
                }
                addCommentToListInDfsOrder(targetList, commentTask.getResult());
            }
        }
    }
}
