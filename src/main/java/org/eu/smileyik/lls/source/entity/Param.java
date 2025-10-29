package org.eu.smileyik.lls.source.entity;

import lombok.Data;
import org.eu.smileyik.lls.LuaConstants;

@Data
public class Param {
    private String type;
    private String name;
    private String description;
    private boolean varArgs;

    public String getName() {
        return LuaConstants.getName(name);
    }
}
