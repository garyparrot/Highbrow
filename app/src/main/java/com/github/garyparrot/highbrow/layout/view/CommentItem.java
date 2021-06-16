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
import com.github.garyparrot.highbrow.event.ShareCommentRequest;
import com.github.garyparrot.highbrow.event.TextToSpeechRequestEvent;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.service.HackerNewsService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;


@AndroidEntryPoint
public class CommentItem extends FrameLayout {

    @Inject
    HackerNewsService hackerNewsService;

    EventBus eventBus;

    private CommentCardViewBinding binding;
    private List<CommentItem> childComments;
    private boolean isToolBarFold = true;
    private boolean isCardFolded = false;

    public void setOnCommentFoldingStateChangeListener(OnCommentFoldingStateChange callback) {
        this.onCommentFoldingStateChangeListener = callback;
    }

    private OnCommentFoldingStateChange onCommentFoldingStateChangeListener;

    public CommentItem(@NonNull Context context) {
        super(context);
        eventBus = EventBus.getDefault();
        inflateView();
        childComments = new ArrayList<>();

        this.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
    }

    public void setCardFolded(boolean folded, boolean shouldEmitEvent) {
        this.isCardFolded = folded;
        binding.setFold(this.isCardFolded);

        ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(layoutParams.width, 0);
        binding.commentToolBar.setLayoutParams(params);

        if (onCommentFoldingStateChangeListener != null && shouldEmitEvent)
            onCommentFoldingStateChangeListener.onFoldingStateChanged(folded);
    }

    private void inflateView() {
        binding = CommentCardViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.commentText.setCustomSelectionActionModeCallback(getOnCommentFoldingStateChangeListener());
        binding.card.setOnClickListener(this::onCardClicked);
        binding.card.setOnLongClickListener(this::onLongClick);
        binding.setSelectionMode(false);
        binding.selectionModeButton.setOnClickListener(this::onSwitchSelectionMode);
        binding.shareCommentButton.setOnClickListener(this::onShareCommentButtonClicked);
    }

    private void onShareCommentButtonClicked(View view) {
        eventBus.post(new ShareCommentRequest(binding.getItem()));
    }

    private void onSwitchSelectionMode(View view) {
        binding.setSelectionMode(!binding.getSelectionMode().booleanValue());
    }

    private boolean onLongClick(View view) {
        boolean after = !isCardFolded;
        setCardFolded(after, true);
        return true;
    }

    private void onCardClicked(View view) {

        // if the card is folded, unfold it first
        if(isCardFolded) {
            setCardFolded(false, true);
            return;
        }

        // Apply animation on the toolbar layout
        float targetHeight = getResources().getDimension(R.dimen.commentToolBarHeight);
        if(isToolBarFold) {
            ValueAnimator animator = ValueAnimator.ofInt(0, (int)targetHeight);
            animator.addUpdateListener((valueAnimator) -> {
                ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
                layoutParams.height = (int) valueAnimator.getAnimatedValue();
                binding.commentToolBar.setLayoutParams(layoutParams);
            });
            animator.setDuration(300);
            animator.start();
            isToolBarFold = !isToolBarFold;
        } else {
            ValueAnimator animator = ValueAnimator.ofInt((int)targetHeight, 0);
            animator.addUpdateListener((valueAnimator) -> {
                ViewGroup.LayoutParams layoutParams = binding.commentToolBar.getLayoutParams();
                layoutParams.height = (int) valueAnimator.getAnimatedValue();
                binding.commentToolBar.setLayoutParams(layoutParams);
            });
            animator.setDuration(300);
            animator.start();
            isToolBarFold = !isToolBarFold;
        }
    }

    private ActionMode.Callback getOnCommentFoldingStateChangeListener() {
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
    }

    public void setNumber(int number) {
        binding.setNumber(number);
    }
    public void setIndentLevel(int level) {
        binding.topLevelLinearLayout.setPadding((int)getResources().getDimension(R.dimen.commentIndent) * level, 0, 0, 0);
    }

    public void setPlaceholderMode(boolean b) {
        binding.setIsLoadMorePlaceholder(b);
    }

    @FunctionalInterface
    public interface OnCommentFoldingStateChange {
        void onFoldingStateChanged(boolean isFolded);
    }
}
