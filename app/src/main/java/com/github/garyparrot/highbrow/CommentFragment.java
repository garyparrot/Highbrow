package com.github.garyparrot.highbrow;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.garyparrot.highbrow.databinding.FragmentCommentBinding;
import com.github.garyparrot.highbrow.event.ShareCommentRequest;
import com.github.garyparrot.highbrow.layout.adapter.CommentRecyclerAdapter;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasUrl;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class CommentFragment extends Fragment {

    public static final String BUNDLE_STORY_JSON = "BUNDLE_STORY_JSON";

    @Inject
    EventBus eventBus;

    @Inject
    HackerNewsService hackerNewsService;

    @Inject
    Gson gson;

    private Story story;

    @Subscribe
    public void subscribeShareRequest(ShareCommentRequest request) {
        Comment comment = request.getComment();
        StringBuilder shareContent = new StringBuilder();
        shareContent
                .append("HackerNews Story:")
                .append(System.getProperty("line.separator"))
                .append(story.getTitle())
                .append(System.getProperty("line.separator"))
                .append("-----")
                .append(System.getProperty("line.separator"))
                .append(comment.getAuthor()).append(" ")
                .append("wrote:").append(System.getProperty("line.separator"))
                .append(Html.fromHtml(comment.getText()).toString())
                .append(System.getProperty("line.separator"))
                .append("-----")
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"));
        if(story.getUrl() != null && !story.getUrl().equals(""))
            shareContent
                .append("Article URL:")
                .append(System.getProperty("line.separator"))
                .append(story.getUrl())
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"));
        shareContent
                .append("Story URL:")
                .append(System.getProperty("line.separator"))
                .append("https://news.ycombinator.com/item?id=").append(story.getId())
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"))
                .append("Comment URL:")
                .append(System.getProperty("line.separator"))
                .append("https://news.ycombinator.com/item?id=").append(comment.getId());

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

    }

    CommentRecyclerAdapter adapter;
    FragmentCommentBinding binding;

    public CommentFragment() { }

    public static CommentFragment newInstance(Gson gson, Story story) {
        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle();
        Timber.d(story.toString());
        bundle.putString(BUNDLE_STORY_JSON, gson.toJson(story));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus.register(this);
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        Objects.requireNonNull(bundle);
        Timber.d(bundle.getString(BUNDLE_STORY_JSON));
        story = gson.fromJson(bundle.getString(BUNDLE_STORY_JSON), GeneralStory.class);

        adapter = new CommentRecyclerAdapter(getContext(), hackerNewsService, story.getKids());
        binding.recycleView.setAdapter(adapter);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

}