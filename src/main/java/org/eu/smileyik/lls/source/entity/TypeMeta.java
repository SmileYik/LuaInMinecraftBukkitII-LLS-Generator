package org.eu.smileyik.lls.source.entity;

public interface TypeMeta {
    String getClassName();
    void addMethodMeta(MethodMeta methodMeta);

    public default String getSimpleName() {
        String className = getClassName();
        if (className.contains(".")) {
            int idx = className.lastIndexOf('.');
            return className.substring(idx + 1);
        }
        return className;
    }
}
