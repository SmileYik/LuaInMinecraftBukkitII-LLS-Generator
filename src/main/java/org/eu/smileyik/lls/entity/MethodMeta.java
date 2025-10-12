package org.eu.smileyik.lls.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MethodMeta implements ModifierEntity, DescriptionEntity {
    private List<String> modifiers;
    private List<TypeParameterMeta> typeParameters;
    private String returnType;
    private String name;
    private List<Param> params;
    private String description;
    private boolean override;
    private List<DescriptionTag> descriptionTags;

    public synchronized void addDescription(DescriptionTag tag) {
        if (descriptionTags == null) {
            descriptionTags = new ArrayList<>();
        }
        descriptionTags.add(tag);
    }

    public boolean hasReturn() {
        // return returnType != null && !returnType.isEmpty() && !returnType.equals("void") && !returnType.equals("Void") && !returnType.equals("java.lang.Void") && !returnType.equals("java.lang.void");
        return true;
    }
}
