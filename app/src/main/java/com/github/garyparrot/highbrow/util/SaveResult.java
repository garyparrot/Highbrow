package com.github.garyparrot.highbrow.util;

public enum SaveResult {
    SAVED("Story saved", "Save Story"),
    UNSAVED("Story unsaved", "Unsave Story");

    String info;
    String actionInfo;

    SaveResult(String info, String actionInfo) {
        this.info = info;
        this.actionInfo = actionInfo;
    }

    public String getActionInfo() {
        return actionInfo;
    }

    @Override
    public String toString() {
        return info;
    }
}
