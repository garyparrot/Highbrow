package com.github.garyparrot.highbrow.model.hacker.news.item;

import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasScore;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasText;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasTitle;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveDescendants;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;

public interface Poll extends Item, HaveDescendants, HaveKids, HasText, HasScore, HasTitle {
    Iterable<Long> getPollOptIds();
}
