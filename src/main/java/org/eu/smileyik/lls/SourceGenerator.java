package org.eu.smileyik.lls;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.quality.NotNull;
import org.eu.smileyik.lls.entity.ClassMeta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SourceGenerator {
    private static final JavaParser PARSER = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21));

    public static interface Splitter extends Function<ClassMeta, String> {}

    public static void generate(@NotNull Path sourcePath,
                                @NotNull Path outputDir,
                                @NotNull Splitter splitter) throws IOException {
        try (Stream<Path> list = Files.list(sourcePath)) {
            list.forEach(it -> {
                if (Files.isDirectory(it)) {
                    try {
                        generate(it, outputDir, splitter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (it.getFileName().toString().toLowerCase(Locale.ENGLISH).endsWith(".java")) {
                    try {
                        PARSER.parse(it).ifSuccessful(cu -> {
                            SourceVisitor sourceVisitor = new SourceVisitor();
                            cu.accept(sourceVisitor, null);
                            Collection<ClassMeta> classMetas = sourceVisitor.getClassMetas();
                            for (ClassMeta classMeta : classMetas) {
                                String result = splitter.apply(classMeta);
                                if (result != null) {
                                    Path out = outputDir.resolve(result);
                                    try {
                                        if (!Files.exists(out)) {
                                            Files.createDirectories(out);
                                        }
                                        writeToFile(classMeta, out);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    public static void generate(@NotNull ZipFile zipFile,
                                @NotNull Path outputDir,
                                @NotNull Splitter splitter) throws IOException {
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().toLowerCase(Locale.ENGLISH).endsWith(".java")) {
                    continue;
                }
                try (InputStream is = zipFile.getInputStream(entry)) {
                    PARSER.parse(is).ifSuccessful(cu -> {
                        SourceVisitor sourceVisitor = new SourceVisitor();
                        cu.accept(sourceVisitor, null);
                        Collection<ClassMeta> classMetas = sourceVisitor.getClassMetas();
                        for (ClassMeta classMeta : classMetas) {
                            String result = splitter.apply(classMeta);
                            if (result != null) {
                                Path out = outputDir.resolve(result);
                                try {
                                    if (!Files.exists(out)) {
                                        Files.createDirectories(out);
                                    }
                                    writeToFile(classMeta, out);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
            }
        } finally {
            zipFile.close();
        }
    }

    protected static void writeToFile(ClassMeta classMeta, Path outputDir) throws IOException {
        String fileName = classMeta.getClassName() + ".lua";
        Files.write(
                outputDir.resolve(fileName),
                LuaStubGenerator.generate(classMeta).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
