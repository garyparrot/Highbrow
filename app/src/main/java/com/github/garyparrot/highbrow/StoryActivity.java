package com.github.garyparrot.highbrow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.github.garyparrot.highbrow.databinding.ActivityStoryBinding;
import com.github.garyparrot.highbrow.event.DictionaryLookupEvent;
import com.github.garyparrot.highbrow.event.DictionaryLookupResultEvent;
import com.github.garyparrot.highbrow.event.TextToSpeechRequestEvent;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.service.UrbanDictionaryService;
import com.github.garyparrot.highbrow.util.StringUtility;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

@AndroidEntryPoint
public class StoryActivity extends AppCompatActivity {

    @Inject
    EventBus eventBus;

    @Inject
    Gson gson;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    TextToSpeech ttsEngine;

    @Inject
    UrbanDictionaryService urbanDictionaryService;

    public static final String BUNDLE_STORY_JSON = "BUNDLE_STORY_JSON";

    private Story story;
    private ActivityStoryBinding binding;

    @Subscribe
    public void onTextToSpeechRequest(TextToSpeechRequestEvent event) {
        ttsEngine.setLanguage(Locale.ENGLISH);
        ttsEngine.speak(event.getSpeechText(), TextToSpeech.QUEUE_FLUSH, null, event.getSpeechText());
    }

    @Subscribe
    public void onDictionaryLookup(DictionaryLookupEvent event) {
        String lookupString = event.getText();

        urbanDictionaryService.query(lookupString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((urbanQueryResult, throwable) -> {
                    if(throwable != null)
                        throwable.printStackTrace();
                    eventBus.post(new DictionaryLookupResultEvent(lookupString, urbanQueryResult, throwable));
                    setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED);
                });
    }

    private void setBottomSheetState(int state) {
        if(state == BottomSheetBehavior.STATE_HIDDEN) {
            binding.bottomSheet.setVisibility(View.GONE);
            binding.shadow.setVisibility(View.GONE);
        } else {
            binding.bottomSheet.setVisibility(View.VISIBLE);
            binding.shadow.setVisibility(View.VISIBLE);
        }
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(binding.bottomSheet);
        behavior.setState(state);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        story = gson.fromJson(bundle.getString(BUNDLE_STORY_JSON), GeneralStory.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_story);
        binding.setStory(story);
        binding.viewPager.setAdapter(new ScreenSlidePagerAdapter(this));
        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
        binding.topAppBar.setNavigationOnClickListener((view) -> this.finish());

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0)
                tab.setText(R.string.story_activity_tab_1);
            else if(position == 1)
                tab.setText(R.string.story_activity_tab_2);
            else
                throw new AssertionError("Suppose there is only two Tabs");
        }).attach();
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();
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
                return CommentFragment.newInstance(gson, story);
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