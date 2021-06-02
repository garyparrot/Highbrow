package com.github.garyparrot.highbrow.util;

import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;
import com.github.garyparrot.highbrow.model.hacker.news.item.general.GeneralStory;
import java.util.Collections;

public class MockStory {

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
