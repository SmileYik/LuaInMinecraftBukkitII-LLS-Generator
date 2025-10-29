package org.eu.smileyik.lls.source.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ModifierEntity {

    public static Set<String> SCOPES = new HashSet<>(Arrays.asList("public", "protected", "private"));

    List<String> getModifiers();

    default boolean isAnInterface() {
        return false;
    }

    default String getScope() {
        if (isAnInterface()) {
            return "public";
        }
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
