package com.github.garyparrot.highbrow.model.hacker.news.item;

import org.jetbrains.annotations.NotNull;

public interface Item {
    long BAD_LONG_VALUE = -1;

    long getId();

    ItemType getItemType();

    String getAuthor();

    long getTime();

    boolean isDeleted();

    boolean isDead();

}
