package com.github.garyparrot.highbrow.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.github.garyparrot.highbrow.room.entity.SavedStory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface SavedStoryDao {

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    Completable insert(SavedStory savedStory);

    @Delete
    Completable delete(SavedStory savedStory);

    @Query("SELECT * FROM saved_story WHERE id == :storyId")
    Maybe<SavedStory> findById(long storyId);

    @Query("SELECT * FROM saved_story")
    Single<List<SavedStory>> findAll();

    @Query("SELECT EXISTS(SELECT * FROM saved_story WHERE id == :id)")
    Single<Boolean> isSavedStoryExists(long id);

}
