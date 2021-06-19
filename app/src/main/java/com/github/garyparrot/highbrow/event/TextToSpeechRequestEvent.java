package com.github.garyparrot.highbrow.event;

import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TextToSpeechRequestEvent {

    @NonNull
    final String speechText;

    @Nullable
    final UtteranceProgressListener listener;

    public TextToSpeechRequestEvent(String speechText) {
        this.speechText = speechText;
        this.listener = null;
    }

    public TextToSpeechRequestEvent(String speechText, UtteranceProgressListener listener) {
        this.speechText = speechText;
        this.listener = listener;
    }
}
