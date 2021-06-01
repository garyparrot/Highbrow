package com.github.garyparrot.highbrow.model.hacker.news.item;

import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasText;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveKids;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HaveParent;

public interface Comment extends Item, HaveKids, HasText, HaveParent {
}
