package com.xiaomi.smsspam.Utils;

import java.util.List;

/**
 * Created by dy on 14-10-25.
 */
public class Strings {

    //AC automation
    public static List<int[]> find(String text, List<String> patterns) {
        if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        return null;
    }

    //KMP
    public static List<int[]> findSingle(String text, String pattern) {
        return null;
    }
}
