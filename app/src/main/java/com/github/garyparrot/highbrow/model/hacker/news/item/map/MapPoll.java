package com.github.garyparrot.highbrow.model.hacker.news.item.map;

import com.github.garyparrot.highbrow.model.hacker.news.item.Poll;

import java.util.Map;

public class MapPoll extends AbstractMapItem implements Poll {
    public MapPoll(Map<String, Object> map) {
        super(map);
    }
}
