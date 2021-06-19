package com.github.garyparrot.highbrow.module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ExecutorServiceModule {

    @Qualifier
    public @interface IoExecutorService {}

    @Qualifier
    public @interface TaskExecutorService {}

    @Provides
    @IoExecutorService
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
    @TaskExecutorService
    ExecutorService taskExecutorService() {
        // Since we are doing IO intensive task on this pool, it is ok to use more thread.
        // I just assume the Blocking factor to be 0.1
        // It is just a magic number, I didn't do any math behind it.
        return new ThreadPoolExecutor(0,
                5,
                5,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
    }


}
