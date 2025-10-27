package org.eu.smileyik;

import org.eu.smileyik.lls.SourceGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class JavaSourceGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java JavaSourceGenerator <output folder>");
            return;
        }
        SourceGenerator.generate(
            new ZipFile(new File(System.getProperty("java.home") + "/lib/src.zip")),
            Paths.get(args[0]),
            (path, classMeta) -> {
                if (!classMeta.getClassName().startsWith("java.")) {
                    return null;
                }
                String[] split = classMeta.getClassName().split("\\.");
                if(split.length > 2) {
                    return String.format("%s.%s", split[0], split[1]);
                }
                return null;
            }
        );
    }
}
