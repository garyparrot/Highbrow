package com.github.garyparrot.highbrow.util;

public final class StringUtility {

    private StringUtility() {}

    public static String firstWord(String sentence) {
        int firstWhitespace = sentence.trim().indexOf(' ');
        return firstWhitespace == -1 ? sentence : sentence.substring(0, firstWhitespace - 1);
    }

}
