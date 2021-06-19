package com.github.garyparrot.highbrow.module;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

@InstallIn(ActivityComponent.class)
@Module
public class TextToSpeechModule {

    public enum EngineState {
        NOT_READY(false), READY(true), ERROR(false);

        public final boolean isWorking;

        EngineState(boolean isWorking) {
            this.isWorking = isWorking;
        }
    }

    private static volatile EngineState isEngineReady = EngineState.NOT_READY;
    public static EngineState isTextToSpeechEngineReady() {
        return isEngineReady;
    }

    @Provides
    TextToSpeech textToSpeech(@ApplicationContext Context context) {
        final TextToSpeech tts = new TextToSpeech(context, status -> {
            if(status == TextToSpeech.SUCCESS) {
                isEngineReady = EngineState.READY;
            } else {
                Timber.e("Failed to initialize TextToSpeech Engine");
                isEngineReady = EngineState.ERROR;
            }
        });
        return tts;
    }
}
