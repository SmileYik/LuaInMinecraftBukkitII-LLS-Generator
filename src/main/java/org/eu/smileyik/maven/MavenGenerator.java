package org.eu.smileyik.maven;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class MavenGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java MavenGenerator <config-file>");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MavenConfig[] mavenConfigs = objectMapper.readValue(new File(args[0]), MavenConfig[].class);
        Maven.generate(mavenConfigs);
    }
}
