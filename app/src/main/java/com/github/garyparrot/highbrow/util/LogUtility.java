package com.github.garyparrot.highbrow.util;

import com.github.garyparrot.highbrow.BuildConfig;

import timber.log.Timber;

public final class LogUtility {

    private LogUtility() {}

    public static void setupTimber() {
        if(BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }
}
