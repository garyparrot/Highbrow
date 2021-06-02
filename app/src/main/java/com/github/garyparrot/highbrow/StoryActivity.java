package com.github.garyparrot.highbrow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.content.res.Resources;
import android.os.Bundle;

import com.github.garyparrot.highbrow.databinding.ActivityStoryBinding;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryActivity extends AppCompatActivity {

    @Inject
    Gson gson;

    @Inject
    HackerNewsService hackerNewsService;

    public static final String BUNDLE_STORY_JSON = "BUNDLE_STORY_JSON";

    private Story story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        story = gson.fromJson(bundle.getString(BUNDLE_STORY_JSON), GeneralStory.class);

        ActivityStoryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_story);
        binding.setStory(story);
        binding.viewPager.setAdapter(new ScreenSlidePagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0)
                tab.setText(R.string.story_activity_tab_1);
            else if(position == 1)
                tab.setText(R.string.story_activity_tab_2);
            else
                throw new AssertionError("Suppose there is only two Tabs");
        }).attach();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(@NonNull @NotNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @NotNull
        @Override
        public Fragment createFragment(int position) {
            if(position == 0)
                return CommentFragment.newInstance(story.getId());
            else if(position == 1)
                return BrowserFragment.newInstance(story.getUrl());
            else
                throw new AssertionError("Suppose there is only two Tabs");
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}