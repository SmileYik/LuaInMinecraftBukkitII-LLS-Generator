package org.eu.smileyik.lls.entity;

import java.util.*;

public interface DescriptionEntity {
    public enum Type {
        AUTHOR,
        DEPRECATED,
        EXCEPTION,
        PARAM,
        RETURN,
        SEE,
        SERIAL,
        SERIAL_DATA,
        SERIAL_FIELD,
        SINCE,
        THROWS,
        VERSION,
        UNKNOWN;
    }

    public String getDescription();

    List<DescriptionTag> getDescriptionTags();

    public default boolean hasDescription() {
        String desc = getDescription();
        return desc != null && !desc.isEmpty();
    }

    public default String getOneLineDescription() {
        return getDescription() == null ? "" : getDescription().replaceAll("[\n\r]", "");
    }

    public default List<DescriptionTag> getDescriptionTags(Type type) {
        List<DescriptionTag> descriptionTags = getDescriptionTags();
        if (descriptionTags == null) return Collections.emptyList();
        List<DescriptionTag> tags = new ArrayList<>();
        for (DescriptionTag tag : descriptionTags) {
            if (Objects.equals(tag.getTagName(), type.name())) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public default String getReturnDescription() {
        List<DescriptionTag> descriptionTags = getDescriptionTags(Type.RETURN);
        for  (DescriptionTag tag : descriptionTags) {
            String content = tag.getContent();
            if (content != null) {
                return content.replaceAll("[\n\r]", "");
            }
        }
        return "";
    }
}
