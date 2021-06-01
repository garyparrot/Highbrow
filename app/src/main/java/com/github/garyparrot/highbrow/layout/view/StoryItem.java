package com.github.garyparrot.highbrow.layout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import com.github.garyparrot.highbrow.R;
import com.github.garyparrot.highbrow.databinding.StoryCardViewBinding;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapStory;
import com.google.android.material.card.MaterialCardView;

public class StoryItem extends FrameLayout {

    StoryCardViewBinding binding;

    public StoryItem(Context context) {
        super(context);
        inflateView();
    }
    public StoryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView();
    }
    public StoryItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView();
    }
    private void inflateView() {
        binding = StoryCardViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }
    public void setStory(Story story) {
        binding.setItem(story);
    }
    public void setNumber(int number) {
        binding.setNumber(number);
    }

}
