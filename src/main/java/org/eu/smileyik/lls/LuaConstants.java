package org.eu.smileyik.lls;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LuaConstants {
    public static final Set<String> KEY_WORLDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while", "goto"
    )));

    public static String getName(String name) {
        if (name == null) {
            return null;
        }
        if (LuaConstants.KEY_WORLDS.contains(name)) {
            return name + "_";
        }
        return name;
    }
}
