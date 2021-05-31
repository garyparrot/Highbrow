package com.github.garyparrot.highbrow.module;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public final class FirebaseDatabaseModule {

    public static final String HACKER_NEWS_FIREBASE_URL = " https://hacker-news.firebaseio.com";

    @Provides
    @HackerNews
    FirebaseDatabase hackerNewsFirebaseDatabase(@ApplicationContext Context context) {
        FirebaseApp.initializeApp(context);
        return FirebaseDatabase.getInstance(HACKER_NEWS_FIREBASE_URL);
    }

    @Provides
    @HackerNews
    DatabaseReference hackerNewsDatabaseReference(@ApplicationContext Context context) {
        return hackerNewsFirebaseDatabase(context).getReference();
    }

    @Qualifier
    public @interface HackerNews { }

}
