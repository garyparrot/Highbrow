package com.github.garyparrot.highbrow.module;

import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.service.impl.FirebaseHackerNewsService;
import com.google.firebase.database.DatabaseReference;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class HackerNewsModule {

    @Provides
    HackerNewsService firebaseHackerNewsService(@FirebaseDatabaseModule.HackerNews DatabaseReference firebase) {
        return new FirebaseHackerNewsService(firebase);
    }

}
