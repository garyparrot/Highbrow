package com.github.garyparrot.highbrow.model.hacker.news.item.general;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;

import java.util.Collections;
import java.util.List;

/**
 * Sometime Hacker News API return null item for no reason, even though that comment is visible
 * on the website. This class represent those item...
 */
public class NullComment implements Comment {

    private final long id;
    private final long parentId;

    public NullComment(long id, long parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    @Override
    public long getId() { return id; }

    @Override
    public ItemType getItemType() {
        return ItemType.Comment;
    }

    @Override
    public String getAuthor() { return null; }

    @Override
    public long getTime() { return 0; }

    @Override
    public boolean isDeleted() { return false; }

    @Override
    public boolean isDead() { return false; }

    @Override
    public String getText() { return ""; }

    @Override
    public List<Long> getKids() { return Collections.emptyList(); }

    @Override
    public long getParentId() { return parentId; }
}
