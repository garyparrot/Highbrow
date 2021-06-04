package com.github.garyparrot.highbrow.room;

import androidx.room.RoomDatabase;

import com.github.garyparrot.highbrow.room.dao.SavedStoryDao;
import com.github.garyparrot.highbrow.room.entity.SavedStory;

@androidx.room.Database(entities={SavedStory.class}, version = 1)
public abstract class HighbrowDatabase extends RoomDatabase {

    public abstract SavedStoryDao savedStory();

}
