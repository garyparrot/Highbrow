package com.github.garyparrot.highbrow.model.hacker.news.item;

import android.util.Log;

import androidx.annotation.Nullable;

import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapAsk;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapComment;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapJob;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapPoll;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapPollOpt;
import com.github.garyparrot.highbrow.model.hacker.news.item.map.MapStory;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasScore;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasText;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasTitle;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasUrl;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveDescendants;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveParent;

import java.util.Map;

import timber.log.Timber;

public final class MapItems {
    @FunctionalInterface
    public interface MapItemTransformer {
        Item from(Map<String, Object> map);
    }

    private MapItems() {
    }

    @Nullable
    public static Item itemFrom(Map<String, Object> map) {
        if(map == null)
            return null;

        Object typeObject = map.get("type");

        if (!(typeObject instanceof String)) {
            throw new IllegalArgumentException();
        }

        ItemType itemType = ItemType.findByString((String) typeObject);

        if (itemType == null)
            throw new IllegalArgumentException();

        return itemType.fromMap(map);
    }

    @Nullable
    public static Story storyFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        Story story = new MapStory(map);
        if(isValidItem(story)) {
            return story;
        } else {
            Timber.w("Bad Story: %s", story);
            return null;
        }
    }

    @Nullable
    public static Comment commentFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        Comment comment = new MapComment(map);
        if(isValidItem(comment)) {
            return comment;
        } else {
            Timber.w("Bad Comment: %s", comment);
            return null;
        }
    }

    @Nullable
    public static Ask askFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        Ask ask = new MapAsk(map);
        if(isValidItem(ask)) {
            return ask;
        } else {
            Timber.w("Bad Ask: %s", ask);
            return null;
        }
    }

    @Nullable
    public static Job jobFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        Job job = new MapJob(map);
        if(isValidItem(job)) {
            return job;
        } else {
            Timber.w("Bad Job: %s", job);
            return null;
        }
    }

    @Nullable
    public static Poll pollFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        Poll poll = new MapPoll(map);
        if(isValidItem(poll)) {
            return poll;
        } else {
            Timber.w("Bad Poll: %s", poll);
            return null;
        }
    }

    @Nullable
    public static PollOpt pollOptFrom(Map<String, Object> map) {
        if(map == null)
            return null;
        PollOpt pollOpt = new MapPollOpt(map);
        if(isValidItem(pollOpt)) {
            return pollOpt;
        } else {
            Timber.w("Bad PollOpt: %s", pollOpt);
            return null;
        }
    }

    public static boolean isValidItem(Item item) {
        // special case for deleted item
        if(item == null)
            return false;
        if(item.isDeleted()) {
            if (item.getId() <= 0) return false;
            if (item.getItemType() == null) return false;
            if (item.getTime() < 0) return false;
            return true;
        }
        if (item.getAuthor() == null) return false;
        if (item.getId() <= 0) return false;
        if (item.getItemType() == null) return false;
        if (item.getTime() < 0) return false;
        // Check other interfaces is correct
        if (item instanceof HaveDescendants) {
            if (((HaveDescendants) item).countDescendants() < 0)
                return false;
        }
        if (item instanceof HaveKids) {
            if (((HaveKids) item).getKids() == null)
                return false;
        }
        if (item instanceof HasText) {
            if (((HasText) item).getText() == null)
                return false;
        }
        if (item instanceof HaveParent) {
            if (((HaveParent) item).getParentId() <= 0)
                return false;
        }
        // if (item instanceof HasUrl) {
        //     if (((HasUrl) item).getUrl() == null)
        //         return false;
        // }
        if (item instanceof HasScore) {
            if (((HasScore) item).getScore() < 0)
                return false;
        }
        if (item instanceof HasTitle) {
            //noinspection RedundantIfStatement
            if (((HasTitle) item).getTitle() == null)
                return false;
        }
        return true;
    }
}
