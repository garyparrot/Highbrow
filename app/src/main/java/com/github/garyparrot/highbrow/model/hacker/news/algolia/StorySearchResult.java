package com.github.garyparrot.highbrow.model.hacker.news.algolia;

import java.util.List;

import lombok.Data;

@Data
public class StorySearchResult {

    List<StorySearchEntry> hits;

    long page;
    long nbHits;
    long nbPages;
    long hitsPerPage;
    String query;
    String params;

}