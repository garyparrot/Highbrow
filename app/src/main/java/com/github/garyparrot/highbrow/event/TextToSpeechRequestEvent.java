package com.github.garyparrot.highbrow.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TextToSpeechRequestEvent {
    final String speechText;
}
