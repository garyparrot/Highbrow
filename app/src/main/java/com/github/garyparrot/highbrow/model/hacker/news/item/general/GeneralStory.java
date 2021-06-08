package com.github.garyparrot.highbrow.model.hacker.news.item.general;

import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneralStory implements Story {

    long id;
    ItemType itemType;
    String author;
    long time;
    boolean isDeleted;
    boolean isDead;
    long score;
    String title;
    String url;
    long descendants;
    List<Long> kids;

    public List<Long> getKids() {
        if(kids == null)
            return Collections.emptyList();
        return kids;
    }

    @Override
    public long countDescendants() {
        return descendants;
    }

}
