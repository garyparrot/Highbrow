package com.github.garyparrot.highbrow.model.hacker.news.item.map;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;

import java.util.Map;

public class MapComment extends AbstractMapItem implements Comment {
    public MapComment(Map<String, Object> map) {
        super(map);
    }
}
