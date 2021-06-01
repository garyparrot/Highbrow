package com.github.garyparrot.highbrow.model.hacker.news.item;

import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasScore;
import com.github.garyparrot.highbrow.model.hacker.news.item.modifier.HasText;

public interface PollOpt extends Item, HasText, HasScore {
    long getPollTargetId();
}
