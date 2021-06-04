package com.github.garyparrot.highbrow.room.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.github.garyparrot.highbrow.model.hacker.news.item.Story;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "saved_story")
@Builder
@Data
@AllArgsConstructor
public class SavedStory {
    @PrimaryKey
    public long id;
    public long postedTime;
    public String url;
    public String author;
    public String title;

    public static SavedStory from(Story story) {
        Objects.requireNonNull(story);

        return SavedStory.builder()
                .id(story.getId())
                .postedTime(story.getTime())
                .url(story.getUrl())
                .author(story.getAuthor())
                .title(story.getTitle())
                .build();
    }
}
