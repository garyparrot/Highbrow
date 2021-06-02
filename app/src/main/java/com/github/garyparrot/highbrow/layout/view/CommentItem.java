package com.github.garyparrot.highbrow.layout.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.github.garyparrot.highbrow.databinding.CommentCardViewBinding;
import com.github.garyparrot.highbrow.databinding.StoryCardViewBinding;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;

public class CommentItem extends FrameLayout {

    private CommentCardViewBinding binding;

    public CommentItem(@NonNull Context context) {
        super(context);
        inflateView();
    }
    private void inflateView() {
        binding = CommentCardViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }
    public void setComment(Comment comment) {
        binding.setItem(comment);
    }
    public void setNumber(int number) {
        binding.setNumber(number);
    }

}
