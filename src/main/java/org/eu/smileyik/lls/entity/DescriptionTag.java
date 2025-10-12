package org.eu.smileyik.lls.entity;

import lombok.Data;
import org.eu.smileyik.lls.LuaConstants;

@Data
public class DescriptionTag {
    private String tagName;
    private String name;
    private String content;

    public String getName() {
        return LuaConstants.getName(name);
    }
}
