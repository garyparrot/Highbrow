package com.github.garyparrot.highbrow.model.hacker.news.item;

import org.jetbrains.annotations.NotNull;

public interface Item {
    String DELETED_ITEM_AUTHOR = "[Deleted by user]";
    String DELETED_ITEM_TITLE = "[Deleted Item]";
    String DELETED_ITEM_TEXT = "[Deleted content]";
    String DEAD_ITEM_TITLE = "[Dead Item]";
    String DEAD_ITEM_TEXT = "[Dead Content]";
    long BAD_LONG_VALUE = -1;

    /**
     * The global unique id of this item
     * @return return the global unique id. if this is a illegal item, return {@link Item#BAD_LONG_VALUE} instead
     */
    long getId();

    ItemType getItemType();

    String getAuthor();

    long getTime();

    boolean isDeleted();

    boolean isDead();

}
