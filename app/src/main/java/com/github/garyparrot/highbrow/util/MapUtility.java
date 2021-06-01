package com.github.garyparrot.highbrow.util;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class MapUtility {

    private MapUtility() {}

    public static <Key, Value> Value getOrDefault(Map<Key, Value> map, Key key, Value defaultValue) {
        if(map.containsKey(key))
            return map.get(key);
        return defaultValue;
    }
}
