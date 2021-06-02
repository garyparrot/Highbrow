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
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryActivity extends AppCompatActivity {

    @Inject
    HackerNewsService hackerNewsService;

    public static final String BUNDLE_STORY_ID = "BUNDLE_STORY_ID";
    public static final String BUNDLE_ARTICLE_URL = "BUNDLE_ARTICLE_URL";

    private String articleUrl;
    private long storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStoryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_story);

        Bundle bundle = getIntent().getExtras();

        storyId = bundle.getLong(BUNDLE_STORY_ID);
        articleUrl = bundle.getString(BUNDLE_ARTICLE_URL);

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
                return CommentFragment.newInstance(storyId);
            else if(position == 1)
                return BrowserFragment.newInstance(articleUrl);
            else
                throw new AssertionError("Suppose there is only two Tabs");
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}