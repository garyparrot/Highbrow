package com.github.garyparrot.highbrow.model.hacker.news.item;

import androidx.annotation.Nullable;

import java.util.Map;

public enum ItemType {
    Story("story", MapItems::storyFrom),
    Comment("comment", MapItems::commentFrom),
    Ask("ask", MapItems::askFrom),
    Job("job", MapItems::jobFrom),
    Poll("poll", MapItems::pollFrom),
    PollOpt("pollopt", MapItems::pollOptFrom);

    final String rawValue;
    final MapItems.MapItemTransformer mapItemTransformer;

    ItemType(String value, MapItems.MapItemTransformer mapTransformer) {
        this.rawValue = value;
        this.mapItemTransformer = mapTransformer;
    }

    @Nullable
    public static ItemType findByString(String value) {
        if (value == null)
            return null;

        for (ItemType itemType : values()) {
            if (itemType.rawValue.equals(value))
                return itemType;
        }
        return null;
    }

    public Item fromMap(Map<String, Object> map) {
        return mapItemTransformer.from(map);
    }
}
