package com.github.garyparrot.highbrow.service;

import com.github.garyparrot.highbrow.model.hacker.news.item.Ask;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Item;
import com.github.garyparrot.highbrow.model.hacker.news.item.Job;
import com.github.garyparrot.highbrow.model.hacker.news.item.Poll;
import com.github.garyparrot.highbrow.model.hacker.news.item.PollOpt;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.User;
import com.google.android.gms.tasks.Task;

import java.util.List;

import javax.net.ssl.SSLEngine;

public interface HackerNewsService {

    Task<Item> getItem(long id);
    Task<Story> getStory(long id);
    Task<Comment> getComment(long id);
    Task<Job> getJob(long id);
    Task<Ask> getAsk(long id);
    Task<Poll> getPoll(long id);
    Task<PollOpt> getPollOpt(long id);

    Task<User> getUser(long id);

    Task<Long> maxId();

    Task<List<Long>> topStoryIds();
    Task<List<Long>> newStoryIds();
    Task<List<Long>> bestStoryIds();
    Task<List<Long>> askStoryIds();
    Task<List<Long>> showStoryIds();
    Task<List<Long>> jobStoryIDs();

    @FunctionalInterface
    interface StorySeries {
        Task<List<Long>> call();
    }

}
