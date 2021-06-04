package com.github.garyparrot.highbrow.util;

public enum SaveResult {
    SAVED("Story saved"), UNSAVED("Story unsaved");

    String info;

    SaveResult(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return info;
    }
}
