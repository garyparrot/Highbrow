package com.github.garyparrot.highbrow.service;

import com.github.garyparrot.highbrow.model.hacker.news.algolia.StorySearchResult;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HackerNewsSearchService {

    @GET("search")
    Single<StorySearchResult> searchStory(
            @Query("query") String query,
            @Query("tags") String targetType,
            @Query("numericFilters") Rank rankBy);

    @GET("search?tags=story")
    Single<StorySearchResult> searchStory(@Query("query") String query);

    enum Rank {
        CreatedTime("created_at_i"),
        Points("point"),
        NumComments("num_comments");

        String name;

        Rank(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
