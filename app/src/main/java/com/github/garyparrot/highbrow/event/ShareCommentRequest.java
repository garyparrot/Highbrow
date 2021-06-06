package com.github.garyparrot.highbrow.event;

import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShareCommentRequest {
    Comment comment;
}
