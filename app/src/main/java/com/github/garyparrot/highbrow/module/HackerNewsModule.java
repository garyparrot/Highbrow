package com.github.garyparrot.highbrow.module;

import com.github.garyparrot.highbrow.service.HackerNewsSearchService;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.service.impl.FirebaseHackerNewsService;
import com.google.firebase.database.DatabaseReference;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class HackerNewsModule {

    @Provides
    HackerNewsService firebaseHackerNewsService(@FirebaseDatabaseModule.HackerNews DatabaseReference firebase) {
        return new FirebaseHackerNewsService(firebase);
    }

    @Provides
    HackerNewsSearchService hackerNewsSearchService(OkHttpClient loggingHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("https://hn.algolia.com/api/v1/")
                .client(loggingHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build().create(HackerNewsSearchService.class);
    }

}
