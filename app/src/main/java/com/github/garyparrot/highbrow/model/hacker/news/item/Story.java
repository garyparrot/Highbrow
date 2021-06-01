package com.github.garyparrot.highbrow.model.hacker.news.item;

import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasScore;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasTitle;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasUrl;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveDescendants;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;

public interface Story extends Item, HaveDescendants, HaveKids, HasUrl, HasScore, HasTitle {
}
