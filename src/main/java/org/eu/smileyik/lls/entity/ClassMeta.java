package org.eu.smileyik.lls.entity;

import lombok.Data;
import org.eu.smileyik.lls.JavaConstants;

import java.util.*;
import java.util.function.Function;

@Data
public class ClassMeta implements ModifierEntity, DescriptionEntity {
    private boolean finishedAnalyzeClass = false;

    private String packageName;
    private String className;
    private List<String> extents;
    private List<String> interfaces;
    private List<String> modifiers;
    private List<TypeParameterMeta> typeParameters;
    private String description;
    private List<DescriptionTag> descriptionTags;
    private boolean deprecated;

    private List<FieldMeta> fields;
    private List<MethodMeta> methods;

    public synchronized void addDescription(DescriptionTag tag) {
        if (descriptionTags == null) {
            descriptionTags = new ArrayList<>();
        }
        descriptionTags.add(tag);
    }

    public synchronized void addFieldMeta(FieldMeta fieldMeta) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(fieldMeta);
    }

    public synchronized void addMethodMeta(MethodMeta methodMeta) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(methodMeta);
    }

    public synchronized void analyzeClassName(Function<String, String> transform) {
        if (finishedAnalyzeClass) {
            return;
        }
        analyzeClassName(extents, transform);
        analyzeClassName(interfaces, transform);
        if (methods != null) {
            for (MethodMeta methodMeta : methods) {
                String type = transform.apply(methodMeta.getReturnType());
                if (!Objects.equals(methodMeta.getReturnType(), type)) {
                    methodMeta.setReturnType(type);
                }
                for (Param param : methodMeta.getParams()) {
                    type = transform.apply(param.getType());
                    if (!Objects.equals(param.getType(), type)) {
                        param.setType(type);
                    }
                }
            }
        }

        for (String str : extents) {
            if (!str.contains(".") && !JavaConstants.isBaseType(str)) {
                finishedAnalyzeClass = false;
                return;
            }
        }
        for (String str : interfaces) {
            if (!str.contains(".") && !JavaConstants.isBaseType(str)) {
                finishedAnalyzeClass = false;
                return;
            }
        }
        if (methods != null) {
            for (MethodMeta methodMeta : methods) {
                if (!methodMeta.getReturnType().contains(".") && !JavaConstants.isBaseType(methodMeta.getReturnType())) {
                    finishedAnalyzeClass = false;
                    return;
                }
                for (Param param : methodMeta.getParams()) {
                    if (!param.getType().contains(".") && !JavaConstants.isBaseType(param.getType())) {
                        finishedAnalyzeClass = false;
                        return;
                    }
                }
            }
        }
        finishedAnalyzeClass = true;
    }

    private void analyzeClassName(List<String> list, Function<String, String> transform) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String old = list.get(i);
            String apply = transform.apply(old);
            if (!Objects.equals(apply, old)) {
                list.set(i, apply);
            }
        }
    }

    public List<String> getAllSupperClasses() {
        List<String> list = new ArrayList<>();
        if (!"java.lang.Object".equals(className)) {
            list.add("java.lang.Object");
        }
        list.addAll(extents);
        list.addAll(interfaces);
        return list;
    }

    public String getSimpleName() {
        if (className.contains(".")) {
            int idx = className.lastIndexOf('.');
            return className.substring(idx + 1);
        }
        return className;
    }
}
