package org.eu.smileyik;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
    }

    public void extractAndProcess(String sourceFilePath) throws FileNotFoundException {
        CompilationUnit cu = new JavaParser().parse(new File(sourceFilePath)).getResult().orElse(null);

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                n.getJavadoc().ifPresent(javadoc ->
                        processJavadoc("Class: " + n.getNameAsString(), javadoc)
                );
            }

            // 访问方法声明
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                n.getJavadoc().ifPresent(javadoc ->
                        processJavadoc("Method: " + n.getNameAsString(), javadoc)
                );
            }

        }, null);
    }

    private void processJavadoc(String element, Javadoc javadoc) {
        System.out.println("\n--- Element: " + element + " ---");

        String description = javadoc.getDescription().toString();
        System.out.println("Main Description:\n" + description);

        for (JavadocBlockTag tag : javadoc.getBlockTags()) {
            System.out.printf("Tag: @%s (Name: %s, Content: %s)\n",
                    tag.getType().name().toLowerCase(),
                    tag.getName().orElse("N/A"),
                    tag.getContent().toString().trim());
        }
    }
}