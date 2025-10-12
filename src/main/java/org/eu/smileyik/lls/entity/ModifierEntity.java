package org.eu.smileyik.lls.entity;

import java.util.*;

public interface ModifierEntity {

    public static Set<String> SCOPES = new HashSet<>(Arrays.asList("public", "protected", "private"));

    List<String> getModifiers();

    default String getScope() {
        List<String> modifiers = getModifiers();
        String scope = "private";
        if (modifiers != null) {
            for (String modifier : modifiers) {
                if (SCOPES.contains(modifier)) {
                    scope = modifier;
                    break;
                }
            }
        }
        return scope;
    }

    default boolean isStatic() {
        List<String> modifiers = getModifiers();
        if (modifiers != null) {
            for (String modifier : modifiers) {
                if ("static".equals(modifier)) {
                    return true;
                }
            }
        }
        return false;
    }
}
