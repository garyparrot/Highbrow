package com.github.garyparrot.highbrow.model.hacker.news.item.map;

import com.github.garyparrot.highbrow.model.hacker.news.item.Item;
import com.github.garyparrot.highbrow.model.hacker.news.item.ItemType;
import com.github.garyparrot.highbrow.util.MapUtility;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractMapItem implements Item {
    final long id;
    final ItemType type;
    final String author;
    final long time;
    final boolean isDeleted;
    final boolean isDead;

    // optional fields
    String text;
    long parent;
    long parentPoll;
    List<Long> kids;
    String url;
    long score;
    String title;
    List<Long> pollOptIds;
    long descendants;

    @Override
    public @NotNull String toString() {
        return "AbstractMapItem{" +
                "id=" + id +
                ", type=" + type +
                ", author='" + author + '\'' +
                ", time=" + time +
                ", isDeleted=" + isDeleted +
                ", isDead=" + isDead +
                ", text='" + text + '\'' +
                ", parent=" + parent +
                ", parentPoll=" + parentPoll +
                ", kids=" + kids +
                ", url='" + url + '\'' +
                ", score=" + score +
                ", title='" + title + '\'' +
                ", pollOptIds=" + pollOptIds +
                ", descendants=" + descendants +
                '}';
    }

    protected AbstractMapItem(Map<String, Object> map) {
        try {
            id = (long) Objects.requireNonNull(map.get("id"));
            type = Objects.requireNonNull(ItemType.findByString((String) map.get("type")));
            author = (String) map.get("by");
            time = (long) Objects.requireNonNull(map.get("time"));
            //noinspection ConstantConditions
            isDead = map.containsKey("dead") && ((boolean) map.get("dead"));
            //noinspection ConstantConditions
            isDeleted = map.containsKey("deleted") && ((boolean) map.get("deleted"));

            text = (String) MapUtility.getOrDefault(map, "text", null);
            parent = (long) MapUtility.getOrDefault(map, "parent", BAD_LONG_VALUE);
            parentPoll = (long) MapUtility.getOrDefault(map, "poll", BAD_LONG_VALUE);
            url = (String) MapUtility.getOrDefault(map, "url", null);
            score = (long) MapUtility.getOrDefault(map, "score", BAD_LONG_VALUE);
            title = (String) MapUtility.getOrDefault(map, "title", null);
            kids = (List<Long>) MapUtility.getOrDefault(map, "kids", null);
            pollOptIds = (List<Long>) MapUtility.getOrDefault(map, "parts", null);
            descendants = (long) MapUtility.getOrDefault(map, "descendants", 0L);
        } catch (NullPointerException | ClassCastException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Bad Map: " + map.toString(), e);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ItemType getItemType() {
        return type;
    }

    @Override
    public String getAuthor() {
        if(isDeleted)
            return DELETED_ITEM_AUTHOR;
        return author;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    public long countDescendants() {
        return descendants;
    }

    public List<Long> getKids() {
        if (kids != null)
            return Collections.unmodifiableList(kids);
        return Collections.emptyList();
    }

    public String getUrl() {
        return url;
    }

    public long getScore() {
        return score;
    }

    public String getTitle() {
        if(isDeleted || title == null)
            return DELETED_ITEM_TITLE;
        return title;
    }

    public String getText() {
        if(isDeleted || text == null)
            return DELETED_ITEM_TEXT;
        return text;
    }

    public long getParentId() {
        return parent;
    }

    public Iterable<Long> getPollOptIds() {
        if (pollOptIds != null)
            return Collections.unmodifiableCollection(pollOptIds);
        return Collections.emptyList();
    }

    public long getPollTargetId() {
        return parentPoll;
    }
}
