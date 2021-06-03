package com.github.garyparrot.highbrow.layout.view;

import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.github.garyparrot.highbrow.databinding.CommentCardViewBinding;
import com.github.garyparrot.highbrow.event.DictionaryLookupEvent;
import com.github.garyparrot.highbrow.event.TextToSpeechRequestEvent;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

public class CommentItem extends FrameLayout {

    EventBus eventBus;

    private CommentCardViewBinding binding;

    public CommentItem(@NonNull Context context) {
        super(context);
        eventBus = EventBus.getDefault();
        inflateView();
    }
    private void inflateView() {
        binding = CommentCardViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.commentText.setCustomSelectionActionModeCallback(getCallback());
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
        binding.setItem(comment);
    }
    public void setNumber(int number) {
        binding.setNumber(number);
    }

}
