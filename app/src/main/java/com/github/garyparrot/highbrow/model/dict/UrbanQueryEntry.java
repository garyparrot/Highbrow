package com.github.garyparrot.highbrow.model.dict;

import lombok.Builder;
import lombok.Data;

@Data
public class UrbanQueryEntry {

    String definition;
    String premalink;
    int thumbsUp;
    int thumbsDown;
    String author;
    String word;
    int defId;
    String example;

}
