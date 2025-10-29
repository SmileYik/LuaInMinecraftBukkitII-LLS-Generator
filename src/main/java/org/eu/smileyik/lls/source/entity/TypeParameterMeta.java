package org.eu.smileyik.lls.source.entity;

import com.github.javaparser.ast.type.TypeParameter;
import lombok.Data;

import java.util.List;

@Data
public class TypeParameterMeta {
    private String type;
    private List<String> bounds;
    private List<TypeParameter> typeParameters;
}
