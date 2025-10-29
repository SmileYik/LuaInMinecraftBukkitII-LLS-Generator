package org.eu.smileyik.lls.source.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldMeta implements ModifierEntity, DescriptionEntity {
    private String name;
    private String type;
    private List<String> modifiers;
    private boolean deprecated;
    private String description;
    private List<DescriptionTag> descriptionTags;
    public synchronized void addDescription(DescriptionTag tag) {
        if (descriptionTags == null) {
            descriptionTags = new ArrayList<>();
        }
        descriptionTags.add(tag);
    }
}
