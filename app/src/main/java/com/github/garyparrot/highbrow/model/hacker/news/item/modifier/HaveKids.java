package com.github.garyparrot.highbrow.model.hacker.news.item.modifier;

import java.util.List;

public interface HaveKids {

    /**
     * Return the item id list of child items.
     *
     * If specific type have zero child exists, a empty list should be returned.
     * @return the list contain all the child item id
     */
    List<Long> getKids();
}
