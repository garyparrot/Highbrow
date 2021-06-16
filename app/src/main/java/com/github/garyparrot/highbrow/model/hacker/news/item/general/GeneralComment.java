package com.github.garyparrot.highbrow.model.hacker.news.item.general;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneralComment implements Comment {

    String author;
    long id;
    List<Long> kids;
    long parentId;
    String text;
    long time;
    ItemType itemType;

    boolean isDeleted;
    boolean isDead;

}
