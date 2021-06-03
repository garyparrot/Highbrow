package com.github.garyparrot.highbrow.module;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class EventBusModule {

    @Provides
    EventBus eventBus() {
        return EventBus.getDefault();
    }

}
