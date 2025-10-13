package org.eu.smileyik.lls;

import java.util.*;

public class JavaConstants {
    public static final Set<String> BASE_TYPE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "void", "byte", "short", "int", "long", "float", "double", "boolean", "char"
    )));

    public static boolean isBaseType(String type) {
        return BASE_TYPE.contains(type);
    }
}
