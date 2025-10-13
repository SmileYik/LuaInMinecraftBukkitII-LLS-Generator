package org.eu.smileyik.lls;

import org.eu.smileyik.lls.entity.*;

import java.util.*;

public class LuaStubGenerator {
    private static final Map<String, String> TYPES;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("void", "nil");
        map.put("java.lang.Void", "nil");
        map.put("java.lang.void", "nil");
        map.put("java.lang.Object", "any");
        map.put("java.lang.Boolean", "boolean");
        map.put("boolean", "boolean");
        map.put("java.lang.boolean", "boolean");
        map.put("java.lang.String", "string");
        map.put("String", "string");
        map.put("java.lang.Byte", "number");
        map.put("byte", "number");
        map.put("java.lang.byte", "number");
        map.put("java.lang.Short", "number");
        map.put("java.lang.short", "number");
        map.put("short", "number");
        map.put("java.lang.int", "number");
        map.put("java.lang.integer", "number");
        map.put("java.lang.Integer", "number");
        map.put("int", "number");
        map.put("java.lang.Long", "number");
        map.put("java.lang.long", "number");
        map.put("long", "number");
        map.put("java.lang.Float", "number");
        map.put("java.lang.float", "number");
        map.put("float", "number");
        map.put("java.lang.Double", "number");
        map.put("java.lang.double", "number");
        map.put("double", "number");
        map.put("org.eu.smileyik.luajava.type.ILuaCallable", "function");
        map.put("org.eu.smileyik.luajava.type.LuaTable", "table");
        map.put("org.eu.smileyik.luajava.type.LuaArray", "table");
        TYPES = Collections.unmodifiableMap(map);
    }

    public static String generate(ClassMeta classMeta) {
        List<String> lines = new ArrayList<>();
        String simpleClassName = classMeta.getSimpleName();

        lines.add("---@meta");
        if (classMeta.hasDescription()) {
            lines.add("---" + classMeta.getOneLineDescription());
        }
        List<String> allSupperClasses = classMeta.getAllSupperClasses();
        lines.add("---@class " + classMeta.getClassName() + (allSupperClasses.isEmpty() ? "" : ": " + String.join(", ", allSupperClasses)));
        List<FieldMeta> fields = classMeta.getFields();
        if (fields != null) {
            fields.forEach(fieldMeta -> {
                lines.add("---@field " + fieldMeta.getScope() + " " + fieldMeta.getName() + " " + getType(fieldMeta.getType()) + " " + (String.format("%s", fieldMeta.isStatic() ? "[STATIC] " : "")) + fieldMeta.getOneLineDescription());
            });
        }
        if (classMeta.isDeprecated()) {
            lines.add("---@deprecated");
        }
        lines.add("local " + simpleClassName + " = {}");

        lines.addAll(generateMethods(simpleClassName, classMeta.getMethods()));
        lines.add("");
        lines.add("return " + simpleClassName);
        return String.join("\n", lines);
    }

    public static String generate(EnumMeta enumMeta) {
        List<String> lines = new ArrayList<>();
        String simpleClassName = enumMeta.getSimpleName();

        return String.join("\n", lines);
    }

    protected static List<String> generateMethods(String classSimpleName, List<MethodMeta> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        methods.forEach(methodMeta -> {
            lines.add("");
            lines.add("---@" + methodMeta.getScope());
            if (methodMeta.hasDescription()) lines.add("---" + methodMeta.getOneLineDescription());
            List<DescriptionTag> paramsDescription = methodMeta.getDescriptionTags(DescriptionEntity.Type.PARAM);
            List<Param> params = methodMeta.getParams();
            List<String> paramNames = new ArrayList<>();
            Map<String, DescriptionTag> paramDes = new HashMap<>();
            for (DescriptionTag param : paramsDescription) {
                paramDes.put(param.getName(), param);
            }
            for (Param param : params) {
                paramNames.add(param.getName());
                DescriptionTag descriptionTag = paramDes.get(param.getName());
                if (descriptionTag == null) continue;
                String desc = descriptionTag.getContent();
                if (desc == null) continue;
                desc = desc.replaceAll("[\n\r]", "").trim();
                if (desc.isEmpty()) continue;
                lines.add("---@param " + param.getName() + " " + getType(param.getType()) + " " + desc);
            }
            if (methodMeta.hasReturn()) {
                lines.add("---@return " + getType(methodMeta.getReturnType()) + " " + methodMeta.getReturnDescription());
            }
            if (methodMeta.isDeprecated()) {
                lines.add("---@deprecated");
            }
            lines.add("function " + classSimpleName + ":" + methodMeta.getName() + "(" + String.join(", ", paramNames) + ") end");
        });
        return lines;
    }

    protected static String getType(String type) {
        if (type == null || type.isEmpty()) {
            return "any";
        }
        String any = TYPES.get(type);
        if (any == null) {
            if (type.endsWith("[]")) {
                return "table";
            }
            return type;
        }
        return any;
    }

    protected static String overrideMethodLine(MethodMeta methodMeta) {
        return "---@overload fun(" +
                String.join(", ", paramsToString(methodMeta.getParams())) +
                "): " +
                getType(methodMeta.getReturnType());
    }

    protected static List<String> paramsToString(List<Param> list) {
        List<String> strings = new ArrayList<>();
        for (Param param : list) {
            strings.add(String.format("%s: %s", param.getName(), getType(param.getType())));
        }
        return strings;
    }
}
