package com.github.garyparrot.highbrow.util;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;

import java.util.Collections;

public class MockItem {

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
                .url(null)
                .build();
    }


}
