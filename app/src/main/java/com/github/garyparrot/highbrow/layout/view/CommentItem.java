package com.github.garyparrot.highbrow.layout.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.github.garyparrot.highbrow.R;
import com.github.garyparrot.highbrow.databinding.CommentCardViewBinding;
import com.github.garyparrot.highbrow.event.DictionaryLookupEvent;
import com.github.garyparrot.highbrow.event.TextToSpeechRequestEvent;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.service.HackerNewsService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;


@AndroidEntryPoint
public class CommentItem extends FrameLayout {

    @Inject
    HackerNewsService hackerNewsService;

    EventBus eventBus;

    private boolean isIndentEnabled;
    private CommentCardViewBinding binding;
    private List<CommentItem> childComments;
    private boolean isToolBarFold = true;
    private boolean isCardFolded = false;

    public CommentItem(@NonNull Context context) {
        super(context);
        eventBus = EventBus.getDefault();
        inflateView();
        childComments = new ArrayList<>();
    }

    private void setCardFolded(boolean folded) {
        this.isCardFolded = folded;
        binding.setFold(this.isCardFolded);

        ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutParams.width, 0);
        binding.commentToolBar.setLayoutParams(params);
    }

    private void inflateView() {
        binding = CommentCardViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.commentText.setCustomSelectionActionModeCallback(getCallback());
        binding.card.setOnClickListener(this::onCardClicked);
        binding.card.setOnLongClickListener(this::onLongClick);
    }

    private boolean onLongClick(View view) {
        setCardFolded(!isCardFolded);
        return true;
    }

    private void onCardClicked(View view) {

        // if the card is folded, unfold it first
        if(isCardFolded) {
            setCardFolded(false);
            return;
        }

        // Apply animation on the toolbar layout
        float targetHeight = getResources().getDimension(R.dimen.commentToolBarHeight);
        if(isToolBarFold) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, targetHeight);
            animator.addUpdateListener((valueAnimator) -> {
                float value = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
                ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutParams.width, (int)value);
                binding.commentToolBar.setLayoutParams(params);
            });
            animator.setDuration(300);
            animator.start();
            isToolBarFold = !isToolBarFold;
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(targetHeight, 0);
            animator.addUpdateListener((valueAnimator) -> {
                float value = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
                ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutParams.width, (int)value);
                binding.commentToolBar.setLayoutParams(params);
            });
            animator.setDuration(300);
            animator.start();
            isToolBarFold = !isToolBarFold;
        }
    }

    private ActionMode.Callback getCallback() {
        return new ActionMode.Callback() {

            private boolean isCustomMenuItemPrepared = false;
            private MenuItem dictionaryMenuItem;
            private MenuItem speechMenuItem;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                isCustomMenuItemPrepared = false;
                dictionaryMenuItem = null;
                speechMenuItem = null;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if(!isCustomMenuItemPrepared) {
                    dictionaryMenuItem = menu.add("Dictionary");
                    speechMenuItem = menu.add("Speak");
                    isCustomMenuItemPrepared = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if(dictionaryMenuItem.getTitle().equals(item.getTitle())) {
                    int start = CommentItem.this.binding.commentText.getSelectionStart();
                    int end = CommentItem.this.binding.commentText.getSelectionEnd();
                    CharSequence content = CommentItem.this.binding.commentText.getText().subSequence(start, end);
                    Timber.i("Dictionary look up for: %s", content);

                    // Post the Dictionary lookup event
                    eventBus.post(new DictionaryLookupEvent(content.toString()));

                    mode.finish();
                    return true;
                } else if(speechMenuItem.getTitle().equals(item.getTitle())) {
                    int start = CommentItem.this.binding.commentText.getSelectionStart();
                    int end = CommentItem.this.binding.commentText.getSelectionEnd();
                    CharSequence content = CommentItem.this.binding.commentText.getText().subSequence(start, end);
                    Timber.i("Speech up for: %s", content);

                    // Post the speech request event
                    eventBus.post(new TextToSpeechRequestEvent(content.toString()));

                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        };
    }

    public void setComment(Comment comment) {
        Timber.d("Set Comment %s", comment);
        binding.setItem(comment);

        // Attempt to load subComments
        loadChildComment(comment.getId(), comment.getKids());
    }

    private void loadChildComment(long parentId, List<Long> kids) {
        final CountDownLatch latch = new CountDownLatch(kids.size());
        final Comment[] collects = new Comment[kids.size()];
        int count = 0;
        for (Long kid : kids) {
            final int index = count++;
            Timber.d("Send request for kids[%d], target id %d", index, kid);
            hackerNewsService.getComment(kid)
                    .addOnSuccessListener((c) -> {
                        latch.countDown();
                        collects[index] = c;

                        if(latch.getCount() == 0) {
                            Timber.d("Collection done, now attempt to add child comments");
                            addChildComment(parentId, collects);
                        }
                    });
        }
    }
    private void addChildComment(long parentId, Comment[] comments) {
        int number = 0;
        for (Comment comment : comments) {
            if(comment != null) {
                Timber.d("Adding comment object to layout: %s", comment);
                CommentItem item = new CommentItem(getContext());
                item.setComment(comment);
                item.setNumber(number++);
                item.setIndentEnabled(true);
                addChildCommentToLayout(item);
            } else {
                Timber.e("Null comment found: Parent index: %d, Item index: %d", parentId, number++);
            }
        }
    }

    public void setNumber(int number) {
        binding.setNumber(number);
    }
    public void setIndentEnabled(boolean isEnabled) {
        this.isIndentEnabled = isEnabled;
        if(isEnabled)
            binding.topLevelLinearLayout.setPadding((int)getResources().getDimension(R.dimen.commentIndent), 0, 0, 0);
        else
            binding.topLevelLinearLayout.setPadding(0,0,0,0);
    }
    public void addChildCommentToLayout(CommentItem commentItem) {
        childComments.add(commentItem);
        binding.childCommentLinearLayout.addView(commentItem);
    }

}
