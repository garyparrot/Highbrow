package com.github.garyparrot.highbrow.layout.util;

/**
 * If you saw this in comment recycler view, then it mean this
 * number point to a placeholder comment.
 *
 * Where the long value is the actual comment id
 *
 * Which you need to click expand or something else to see all content.
 */
public class CommentPlaceholder extends Number {

    final long placeholderId;

    public CommentPlaceholder(long placeholderId) {
        this.placeholderId = placeholderId;
    }

    @Override
    public int intValue() {
        return (int) placeholderId;
    }

    @Override
    public long longValue() {
        return placeholderId;
    }

    @Override
    public float floatValue() {
        return (float) placeholderId;
    }

    @Override
    public double doubleValue() {
        return (double) placeholderId;
    }
}
