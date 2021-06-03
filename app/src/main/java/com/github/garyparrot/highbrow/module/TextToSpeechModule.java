package com.github.garyparrot.highbrow.module;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.widget.Toast;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

@InstallIn(ActivityComponent.class)
@Module
public class TextToSpeechModule {

    @Provides
    TextToSpeech textToSpeech(@ApplicationContext Context context) {
        final TextToSpeech tts = new TextToSpeech(context, status -> {
            if(status != TextToSpeech.SUCCESS) {
                Timber.e("Failed to initialize TextToSpeech Engine");
                Toast.makeText(context, "Failed to initialize TextToSpeech Engine", Toast.LENGTH_SHORT).show();
            }
        });
        return tts;
    }
}
