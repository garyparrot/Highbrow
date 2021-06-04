package com.github.garyparrot.highbrow.module;

import android.content.Context;

import androidx.room.Room;

import com.github.garyparrot.highbrow.room.HighbrowDatabase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class RoomDatabaseModule {

    public static final String DatabaseName = "highbrow";

    @Provides
    HighbrowDatabase database(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, HighbrowDatabase.class, DatabaseName).build();
    }

}
