package com.github.garyparrot.highbrow.util;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Item;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasUrl;

import java.util.Collections;

public class HackerNewsItemUtility {

    public static Comment getEmptyComment() {
        return GeneralComment.builder()
                .author("")
                .id(0)
                .kids(Collections.emptyList())
                .parentId(0)
                .text("")
                .time(0)
                .itemType(ItemType.Comment)
                .build();
    }

    public static Story getEmptyStory() {
        return GeneralStory.builder()
                .author("")
                .descendants(0)
                .id(0)
                .kids(Collections.emptyList())
                .score(0)
                .time(0)
                .title("")
                .itemType(ItemType.Story)
                .url("")
                .build();
    }

    public static <T extends HasUrl & Item> String resolveRealUrl(T item) {
        if(item.getUrl() == null || item.getUrl().equals(""))
            return "https://news.ycombinator.com/item?id=" + item.getId();
        return item.getUrl();
    }

}
