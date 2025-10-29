package org.eu.smileyik.lls;

import java.io.IOException;
import java.nio.file.Paths;

public class LIIGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java LIIGenerator <source-path> <output-path>");
            return;
        }
        String srcPath = args[0];
        String outputPath = args[1];
        SourceGenerator.generate(
                Paths.get(srcPath),
                Paths.get(outputPath),
                (path, clazz) -> {
                    if (clazz.getClassName().startsWith("org.eu.smileyik.luaInMinecraftBukkitII.api.lua")) {
                        return "LII-api";
                    }
                    return null;
                }
        );
    }
}
