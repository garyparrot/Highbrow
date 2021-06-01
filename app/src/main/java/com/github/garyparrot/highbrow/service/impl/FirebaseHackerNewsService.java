package com.github.garyparrot.highbrow.service.impl;

import com.github.garyparrot.highbrow.model.hacker.news.item.Ask;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Item;
import com.github.garyparrot.highbrow.model.hacker.news.item.Job;
import com.github.garyparrot.highbrow.model.hacker.news.item.MapItems;
import com.github.garyparrot.highbrow.model.hacker.news.item.Poll;
import com.github.garyparrot.highbrow.model.hacker.news.item.PollOpt;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.User;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirebaseHackerNewsService implements HackerNewsService {

    private static final GenericTypeIndicator<List<Long>> indicatorListLong
            = new GenericTypeIndicator<List<Long>>() { };
    DatabaseReference firebase;
    DatabaseReference hackerNews;

    public FirebaseHackerNewsService(DatabaseReference firebase) {
        this.firebase = firebase;
        this.hackerNews = firebase.child("v0");
    }

    private Task<Map<String, Object>> getMapOfItemIdTask(long id) {
        Task<DataSnapshot> item = hackerNews.child("item").child(String.valueOf(id)).get();
        //noinspection unchecked
        return item.continueWith((x) -> (Map<String, Object>)x.getResult().getValue());
    }

    @Override
    public Task<Item> getItem(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.itemFrom(x.getResult()));
    }

    @Override
    public Task<Story> getStory(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.storyFrom(x.getResult()));
    }

    @Override
    public Task<Comment> getComment(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.commentFrom(x.getResult()));
    }

    @Override
    public Task<Job> getJob(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.jobFrom(x.getResult()));
    }

    @Override
    public Task<Ask> getAsk(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.askFrom(x.getResult()));
    }

    @Override
    public Task<Poll> getPoll(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.pollFrom(x.getResult()));
    }

    @Override
    public Task<PollOpt> getPollOpt(long id) {
        return (getMapOfItemIdTask(id)).continueWith(x -> MapItems.pollOptFrom(x.getResult()));
    }

    @Override
    public Task<User> getUser(long id) {
        // TODO: Support this
        throw new UnsupportedOperationException();
    }

    @Override
    public Task<Long> maxId() {
        return hackerNews.child("maxitem").get().continueWith(x -> x.getResult().getValue(Long.class));
    }

    private Task<List<Long>> getStorySeriesTask(String topic) {
        Task<List<Long>> taskToGetSeries = hackerNews.child(topic).get()
                .continueWith((result) -> result.getResult().getValue(indicatorListLong));
        return taskToGetSeries;
    }

    @Override
    public Task<List<Long>> topStoryIds() {
        return getStorySeriesTask("topstories");
    }

    @Override
    public Task<List<Long>> newStoryIds() {
        return getStorySeriesTask("newstories");
    }

    @Override
    public Task<List<Long>> bestStoryIds() {
        return getStorySeriesTask("beststories");
    }

    @Override
    public Task<List<Long>> askStoryIds() {
        return getStorySeriesTask("askstories");
    }

    @Override
    public Task<List<Long>> showStoryIds() {
        return getStorySeriesTask("showstories");
    }

    @Override
    public Task<List<Long>> jobStoryIDs() {
        return getStorySeriesTask("jobstories");
    }
}
