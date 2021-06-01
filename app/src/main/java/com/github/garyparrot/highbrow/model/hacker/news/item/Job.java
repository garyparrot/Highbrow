package com.github.garyparrot.highbrow.model.hacker.news.item;

import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasScore;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasText;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasTitle;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasUrl;

public interface Job extends Item, HasText, HasUrl, HasScore, HasTitle {
}
