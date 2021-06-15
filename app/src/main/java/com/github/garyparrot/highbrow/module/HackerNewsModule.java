package com.github.garyparrot.highbrow.module;

import com.github.garyparrot.highbrow.service.HackerNewsSearchService;
import com.github.garyparrot.highbrow.service.HackerNewsService;
import com.github.garyparrot.highbrow.service.impl.FirebaseHackerNewsService;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    ExecutorService ioExecutorService() {
        // Since we are doing IO intensive task on this pool, it is ok to use more thread.
        // I just assume the Blocking factor to be 0.1
        // It is just a magic number, I didn't do any math behind it.
        return new ThreadPoolExecutor(0,
                Runtime.getRuntime().availableProcessors() * 10,
                5,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
    }

    @Provides
    HackerNewsService firebaseHackerNewsService(@FirebaseDatabaseModule.HackerNews DatabaseReference firebase, ExecutorService ioExecutorService) {
        return new FirebaseHackerNewsService(ioExecutorService, firebase);
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
