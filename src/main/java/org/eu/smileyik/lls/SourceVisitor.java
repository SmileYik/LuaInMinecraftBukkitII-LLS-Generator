package org.eu.smileyik.lls;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.JavadocBlockTag;
import lombok.Getter;
import lombok.ToString;
import org.eu.smileyik.lls.entity.*;

import java.util.*;

@Getter
public class SourceVisitor extends VoidVisitorAdapter<Void> {
    private String currentPackage;
    @ToString.Include
    private final Map<String, ClassMeta> classMap = new HashMap<>();
//    private final Map<String, EnumMeta> enumMap = new HashMap<>();
    private final Map<String, String> imports = new HashMap<>();

    @Override
    public void visit(PackageDeclaration n, Void arg) {
        super.visit(n, arg);
        currentPackage = n.getNameAsString();
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        super.visit(n, arg);
        String simpleName = n.getName().getIdentifier();
        String name = n.getNameAsString();
        imports.put(simpleName, name);
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        ClassMeta meta = new ClassMeta();
        meta.setClassName(n.getFullyQualifiedName().orElse(n.getNameAsString()));
        meta.setPackageName(currentPackage);
        meta.setInterfaces(toFullClassList(n.getImplementedTypes()));
        meta.setModifiers(toModifierList(n.getModifiers()));
        meta.setDeprecated(n.getAnnotationByClass(Deprecated.class).isPresent());
        for (EnumConstantDeclaration entry : n.getEntries()) {
            FieldMeta fieldMeta = new FieldMeta();
            fieldMeta.setType(meta.getClassName());
            fieldMeta.setModifiers(Arrays.asList("public", "static", "final"));
            fieldMeta.setName(entry.getNameAsString());
            fieldMeta.setDeprecated(entry.isAnnotationPresent(Deprecated.class));
            entry.getJavadoc().ifPresent(javadoc -> {
                fieldMeta.setDescription(javadoc.getDescription().toText());
                for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                    fieldMeta.addDescription(javadocBlockTag2Tag(blockTag));
                }
            });
            meta.addFieldMeta(fieldMeta);
        }
        n.getJavadoc().ifPresent(javadoc -> {
            meta.setDescription(javadoc.getDescription().toText());
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                meta.addDescription(javadocBlockTag2Tag(blockTag));
            }
        });
        imports.put(n.getName().getIdentifier(), meta.getClassName());
        classMap.put(meta.getClassName(), meta);
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {

        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        String className = n.getFullyQualifiedName().orElse(n.getNameAsString());
        List<String> extents = toFullClassList(n.getExtendedTypes());
        List<String> interfaces = toFullClassList(n.getImplementedTypes());
        List<String> modifiers = toModifierList(n.getModifiers());
        List<TypeParameterMeta> typeParameters = toTypeParameterList(n.getTypeParameters());
        ClassMeta classMeta = new ClassMeta();
        classMeta.setClassName(className);
        classMeta.setExtents(extents);
        classMeta.setInterfaces(interfaces);
        classMeta.setModifiers(modifiers);
        classMeta.setTypeParameters(typeParameters);
        classMeta.setPackageName(currentPackage);
        classMeta.setDeprecated(n.getAnnotationByClass(Deprecated.class).isPresent());
        classMeta.setAnInterface(n.isInterface());
        classMap.put(className, classMeta);
        imports.put(n.getName().getIdentifier(), className);

        n.getJavadoc().ifPresent(javadoc -> {
            classMeta.setDescription(javadoc.getDescription().toText());
            for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                classMeta.addDescription(javadocBlockTag2Tag(blockTag));
            }
        });

        // fill names
        for (ClassMeta meta : classMap.values()) {
            meta.analyzeClassName(this::getFullClassName);
        }

        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);

        n.findAncestor(TypeDeclaration.class).ifPresent(ancestor -> {
            String className = (String) ancestor.getFullyQualifiedName().orElse(ancestor.getNameAsString());
            ClassMeta typeMeta = classMap.get(className);
            if (typeMeta == null) return;

            MethodMeta methodMeta = new MethodMeta();
            methodMeta.setModifiers(toModifierList(n.getModifiers()));
            methodMeta.setTypeParameters(toTypeParameterList(n.getTypeParameters()));
            methodMeta.setReturnType(getFullClassName(typeMeta, methodMeta, n.getTypeAsString()));
            methodMeta.setName(n.getNameAsString());
            methodMeta.setParams(toParamList(typeMeta, methodMeta, n.getParameters()));
            n.getAnnotationByClass(Override.class).ifPresent(override -> {
                methodMeta.setOverride(true);
            });
            methodMeta.setDeprecated(n.getAnnotationByClass(Deprecated.class).isPresent());
            n.getJavadoc().ifPresent(javadoc -> {
                methodMeta.setDescription(javadoc.getDescription().toText());
                for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                    methodMeta.addDescription(javadocBlockTag2Tag(blockTag));
                }
            });

            typeMeta.addMethodMeta(methodMeta);
        });
    }

    @Override
    public void visit(FieldDeclaration n, Void arg) {
        super.visit(n, arg);
        n.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(ancestor -> {
            String className = ancestor.getFullyQualifiedName().orElse(ancestor.getNameAsString());
            ClassMeta classMeta = classMap.get(className);
            if (classMeta == null) return;

            List<FieldMeta> fields = new ArrayList<>();
            for (VariableDeclarator variable : n.getVariables()) {
                FieldMeta fieldMeta = new FieldMeta();
                fieldMeta.setModifiers(toModifierList(n.getModifiers()));
                fieldMeta.setName(variable.getNameAsString());
                fieldMeta.setType(getFullClassName(classMeta, variable.getTypeAsString()));
                fieldMeta.setDeprecated(n.getAnnotationByClass(Deprecated.class).isPresent());
                fields.add(fieldMeta);
                classMeta.addFieldMeta(fieldMeta);
            }

            n.getJavadoc().ifPresent(javadoc -> {
                for (FieldMeta field : fields) {
                    field.setDescription(javadoc.getDescription().toText());
                    for (JavadocBlockTag blockTag : javadoc.getBlockTags()) {
                        field.addDescription(javadocBlockTag2Tag(blockTag));
                    }
                }
            });
        });
    }

    protected String getFullClassName(ClassMeta classMeta, String type) {
        List<TypeParameterMeta> typeParameters = classMeta.getTypeParameters();
        if (typeParameters != null) {
            for (TypeParameterMeta typeParameterMeta : typeParameters) {
                if (Objects.equals(typeParameterMeta.getType(), type)) {
                    return "java.lang.Object";
                }
            }
        }
        return getFullClassName(type);
    }

    protected String getFullClassName(ClassMeta classMeta, MethodMeta methodMeta, String type) {
        List<TypeParameterMeta> typeParameters = methodMeta.getTypeParameters();
        if (typeParameters != null) {
            for (TypeParameterMeta typeParameterMeta : typeParameters) {
                if (Objects.equals(typeParameterMeta.getType(), type)) {
                    return "java.lang.Object";
                }
            }
        }
        typeParameters = classMeta.getTypeParameters();
        if (typeParameters != null) {
            for (TypeParameterMeta typeParameterMeta : typeParameters) {
                if (Objects.equals(typeParameterMeta.getType(), type)) {
                    return "java.lang.Object";
                }
            }
        }
        return getFullClassName(type);
    }

    protected String getFullClassName(String name) {
        if (name.endsWith("[]")) {
            int i = name.lastIndexOf('[');
            String field = name.substring(0, i);
            field = getFullClassName(field);
            return field + name.substring(i);
        }
        if (JavaConstants.isBaseType(name)) {
            return name;
        }
        if (name.contains("<")) {
            name = name.substring(0, name.indexOf("<"));
        }
        if (!name.contains(".")) {
            name = imports.getOrDefault(name, name);
        }
        if (!name.contains(".")) {
            String clazzName = "java.lang." + name;
            boolean exists = true;
            try {
                Class.forName(clazzName);
            } catch (ClassNotFoundException ignored) {
                exists = false;
            } catch (Exception ignored) {
            }
            return exists ? clazzName : (currentPackage + "." + name);
        }
        return name;
    }

    protected List<String> toFullClassList(NodeList<ClassOrInterfaceType> list) {
        List<String> result = new ArrayList<>();
        for (ClassOrInterfaceType type : list) {
            String name = type.getNameAsString();
            Optional<ClassOrInterfaceType> scope = type.getScope();
            if (scope.isPresent()) {
                result.add(scope.get() + "." + name);
            } else {
                result.add(getFullClassName(name));
            }
        }
        return result;
    }

    protected List<String> toModifierList(NodeList<Modifier> list) {
        List<String> result = new ArrayList<>();
        for (Modifier modifier : list) {
            result.add(modifier.getKeyword().asString());
        }
        return result;
    }

    protected List<TypeParameterMeta> toTypeParameterList(NodeList<TypeParameter> list) {
        List<TypeParameterMeta> result = new ArrayList<>();
        for (TypeParameter type : list) {
            String name = type.getNameAsString();
            List<String> bounds = toFullClassList(type.getTypeBound());
            TypeParameterMeta typeMeta = new TypeParameterMeta();
            typeMeta.setType(name);
            typeMeta.setBounds(bounds);
            result.add(typeMeta);
        }
        return result;
    }

    protected DescriptionTag javadocBlockTag2Tag(JavadocBlockTag tag) {
        DescriptionTag result = new DescriptionTag();
        result.setTagName(tag.getType().name());
        result.setName(tag.getName().orElse(null));
        result.setContent(tag.getContent().toText());
        return result;
    }

    protected List<Param> toParamList(ClassMeta classMeta, MethodMeta methodMeta, NodeList<Parameter> list) {
        List<Param> result = new ArrayList<>();
        for (Parameter parameter : list) {
            String name = parameter.getNameAsString();
            String typeAsString = parameter.getTypeAsString();
            Param param = new Param();
            param.setName(name);
            param.setType(getFullClassName(classMeta, methodMeta, typeAsString));
            param.setVarArgs(parameter.isVarArgs());
            result.add(param);
        }
        return result;
    }

    public Collection<ClassMeta> getClassMetas() {
        classMap.values().forEach(classMeta -> {
            classMeta.analyzeClassName(name -> {
                if (name.contains(".")) return name;
                if (JavaConstants.isBaseType(name)) return name;
                String newName = getFullClassName(name);
                if (!Objects.equals(newName, name)) return newName;
                String clazzName = "java.lang." + name;
                boolean exists = true;
                try {
                    Class.forName(clazzName);
                } catch (ClassNotFoundException ignored) {
                    exists = false;
                } catch (Exception ignored) {
                }
                return exists ? clazzName : (currentPackage + "." + name);
            });
        });
        return classMap.values();
    }

//    public Collection<EnumMeta> getEnumMetas() {
//        return enumMap.values();
//    }
}
