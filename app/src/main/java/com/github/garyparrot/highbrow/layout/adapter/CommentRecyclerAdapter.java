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
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.NullComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.util.MockItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import timber.log.Timber;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    private static int nextRequestId = 0;
    private final Map<Long, Task<Comment>> commentStorage;
    private final Map<Long, Integer> commentDepth;
    private final Map<Long, Boolean> commentFolding;
    private final Map<Long, Long> commentBloodline;
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
        this.commentFolding = Collections.synchronizedMap(new HashMap<>());
        this.commentBloodline = Collections.synchronizedMap(new HashMap<>());
        this.story = story;
        this.downloadExecutorService = downloadExecutorService;
        this.commentPreDownloadExecutorService = Executors.newFixedThreadPool(5);

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
        void setFolding(boolean isFolded) {
            item.setCardFolded(isFolded, false);
        }
        void setChildCommentNumber(int number) {
            item.setChildCommentsNumber(number);
        }
        void setFoldingStateChangeListener(CommentItem.OnCommentFoldingStateChange listener) {
            item.setOnCommentFoldingStateChangeListener(listener);
        }
        void setToolbarFolded(boolean isFolded) {
            item.setToolBarFold(isFolded, false);
        }

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
            launchCommentDownloadTask(commentId)
                    .addOnSuccessListener(comment -> {
                        commentPreDownloadExecutorService.submit(new CommentFullyDownloadLogic(comment));
                    });
        }

        Task<Comment> commentTask = commentStorage.get(commentId);
        Objects.requireNonNull(commentTask);
        if(commentTask.isComplete()) {
            holder.setComment(commentTask.getResult());
            holder.setIndent(commentDepth.get(commentTask.getResult().getId()));
            holder.setFolding(commentFolding.get(commentId));
            holder.setChildCommentNumber(countCommentChildren(commentTask.getResult()));
            holder.setToolbarFolded(true);
            holder.setFoldingStateChangeListener((isFolded) -> {
                if(isFolded)
                    hideCommentChildren(commentTask.getResult());
                else
                    showResolvedCommentChildren(commentTask.getResult());
                commentFolding.put(commentId, isFolded);
                holder.setToolbarFolded(true);
            });
            if(number instanceof CommentPlaceholder)
                holder.setPlaceholderMode(true);
        } else {
            GeneralComment comment = GeneralComment.builder()
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

    private void hideCommentChildren(Comment comment) {

        if(comment.getKids().size() == 0)
            return;

        Set<Long> children = new HashSet<>(getCommentChildrenIds(comment));

        int commentPosition = targetIds.indexOf(comment.getId());

        synchronized (targetIds) {

            int start = commentPosition + 1;
            int end = start;

            for(end = start; end < targetIds.size(); end++)
                if(!children.contains(targetIds.get(end)))
                    break;

            // At this moment, the range [start, end) is the children of given comment
            for(int i = end - 1; i >= start; i--)
                targetIds.remove(i);
            notifyItemRangeRemoved(start, end - start);
        }

    }
    private void showResolvedCommentChildren(Comment comment) {
        List<Long> children = getCommentChildrenIds(comment);

        if(children.size() == 0)
            return;

        synchronized (targetIds) {
            int targetIndex = targetIds.indexOf(comment.getId());
            if(targetIndex == -1)
                throw new IllegalArgumentException("Comment " + comment.getId() + " not exists in RecyclerView");

            targetIds.addAll(targetIndex + 1, children);
            notifyItemRangeInserted(targetIndex + 1, children.size());
        }
    }

    /**
     * Retrieve a list of children id from given comment object, Also these id should be in DFS order
     * @param comment The specified comment want to retrieve
     * @return a list of comment children id of given comment, in DSF order
     */
    private List<Long> getCommentChildrenIds(Comment comment) {
        final Stack<Long> pendingStack = new Stack<>();
        final List<Long> children = new ArrayList<>();

        for (int i = comment.getKids().size() - 1; i >= 0; i--) {
            pendingStack.push(comment.getKids().get(i));
        }

        while(!pendingStack.empty()){
            Long next = pendingStack.pop();
            Task<Comment> commentTask = commentStorage.get(next);

            if(commentTask == null || !commentTask.isComplete()) {
                Timber.w("Comment %d has an unresolved child (id %d)", comment.getId(), next);
                continue;
            }

            if(commentTask.getResult() == null) {
                Timber.w("Comment %d has an null child (id %d)", comment.getId(), next);
                continue;
            }

            Comment child = commentTask.getResult();

            for (int i = child.getKids().size() - 1; i >= 0; i--) {
                pendingStack.push(child.getKids().get(i));
            }

            children.add(child.getId());
        }

        return children;
    }

    private int countCommentChildren(Comment comment) {
        return getCommentChildrenIds(comment).size();
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

    /**
     * Ensure the item at given position is completely(including all its child comments) is load.
     * If item is not load yet, we will launch a task to download it.
     *
     * @param position the item position that you want to ensure load
     */
    public void ensureItemResolved(int position) {
        long commentId = targetIds.get(position).longValue();

        synchronized (commentStorage) {
            if(commentStorage.containsKey(commentId))
                // comment either downloaded or is in progress
                return;

            launchCommentDownloadTask(commentId)
                    .addOnSuccessListener((comment) -> {
                        commentPreDownloadExecutorService.submit(new CommentFullyDownloadLogic(comment));
                    });
        }
    }

    private Task<Comment> launchCommentDownloadTask(long commentId) {
        if(commentStorage.containsKey(commentId))
            return Tasks.forResult(commentStorage.get(commentId).getResult());

        Task<Comment> commentTask = launchCommentDownloadTaskInternal(commentId);
        commentStorage.put(commentId, commentTask);

        return commentTask;
    }
    private Task<Comment> launchCommentDownloadTaskInternal(final long commentId) {
        return hackerNews.getComment(commentId)
                .continueWith(downloadExecutorService, taskInstance -> {
                    Comment comment = taskInstance.getResult();
                    if(comment == null) {
                        // try to tracking down the parent of this comment
                        long myParent = commentBloodline.containsKey(commentId) ? commentBloodline.get(commentId) : story.getId();
                        commentDepth.put(commentId, commentDepth.get(myParent) + 1);
                        commentFolding.put(commentId, false);
                        comment = new NullComment(commentId, myParent);
                    } else if(comment.getParentId() == story.getId()) {
                        commentDepth.put(comment.getId(), 0);
                        commentFolding.put(comment.getId(), false);
                    } else {
                        commentDepth.put(comment.getId(), commentDepth.get(comment.getParentId()) + 1);
                        commentFolding.put(comment.getId(), false);
                    }

                    for (Long kid : comment.getKids()) {
                        commentBloodline.put(kid, comment.getId());
                    }
                    return comment;
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

    class CommentFullyDownloadLogic implements Runnable {

        final Comment root;

        CommentFullyDownloadLogic(Comment root) {
            this.root = root;
        }

        @Override
        public void run() {
            // Step 1: Download everything
            ensureCommentHierarchyDownloaded(root);

            // Step 2: collect result into dfs order id list
            List<Long> dfsOrder = new ArrayList<>();
            addCommentToListInDfsOrder(dfsOrder, root);

            Tasks.forResult(dfsOrder)
                    .addOnSuccessListener(commentLinear -> {
                        synchronized (targetIds) {
                            int index = targetIds.indexOf(root.getId());
                            targetIds.remove(root.getId());
                            notifyItemRemoved(index);
                            targetIds.addAll(index, commentLinear);
                            notifyItemRangeInserted(index, commentLinear.size());
                        }
                    });
        }

        private void ensureCommentHierarchyDownloaded(Comment comment) {
            try {
                Queue<Long> availableTaskQueue = new LinkedBlockingQueue<>(comment.getKids());
                BlockingQueue<Long> taskDoneQueue = new LinkedBlockingQueue<>();
                AtomicInteger pendingTasks = new AtomicInteger();

                // We only leave when is no more available comment for download, and all pending task is done
                while(!availableTaskQueue.isEmpty() || pendingTasks.get() != 0) {
                    // Accept & Launch tasks
                    while(availableTaskQueue.size() > 0) {
                        Long commentId = availableTaskQueue.poll();
                        launchCommentDownloadTask(commentId)
                                .addOnSuccessListener(downloadExecutorService, command -> {
                                    taskDoneQueue.add(commentId);
                                })
                                .addOnFailureListener(downloadExecutorService, e -> {
                                    e.printStackTrace();
                                    taskDoneQueue.add(commentId);
                                });
                        pendingTasks.incrementAndGet();
                    }

                    // some job is done because we receive the signal
                    while(taskDoneQueue.size() > 0) {
                        Long poll = taskDoneQueue.poll(1, TimeUnit.SECONDS);
                        Comment result = commentStorage.get(poll).getResult();
                        if(result != null)
                            availableTaskQueue.addAll(result.getKids());
                        pendingTasks.decrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        // TODO: Tech debt, duplicated function
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
