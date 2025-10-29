package org.eu.smileyik.lls.source.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnumMeta implements ModifierEntity, DescriptionEntity, TypeMeta {
    private String packageName;
    private String className;
    private List<String> interfaces;
    private List<String> modifiers;
    private String description;
    private List<DescriptionTag> descriptionTags;
    private boolean deprecated;

    private List<MethodMeta> methods;

    public synchronized void addDescription(DescriptionTag tag) {
        if (descriptionTags == null) {
            descriptionTags = new ArrayList<>();
        }
        descriptionTags.add(tag);
    }

    public synchronized void addMethodMeta(MethodMeta methodMeta) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(methodMeta);
    }
}
