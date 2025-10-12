package org.eu.smileyik.lls.entity;

import lombok.Data;
import org.eu.smileyik.lls.LuaConstants;

@Data
public class Param {
    private String type;
    private String name;
    private String description;

    public String getName() {
        return LuaConstants.getName(name);
    }
}
