package com.github.garyparrot.highbrow.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class GsonModule {

    @Provides
    Gson gson() {
        return new GsonBuilder().create();
    }

}
