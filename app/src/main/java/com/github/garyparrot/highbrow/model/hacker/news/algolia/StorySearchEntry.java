package com.github.garyparrot.highbrow.model.hacker.news.algolia;

import java.util.List;

import lombok.Data;

@Data
public class StorySearchEntry {

    String objectID;
    String title;
    String url;
    String author;
    long points;
    String story_text;
    String comment_text;
    List<String> _tags;
    long num_comments;

}
