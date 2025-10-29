package org.eu.smileyik.lls.maven;

import lombok.Data;

import java.util.List;

@Data
public class MavenConfig {
    /**
     * group:artifact:version
     */
    private String model;

    /**
     * the folder store lua files
     */
    private String outputPath;

    /**
     * cache jar folder
     */
    private String cachePath;

    /**
     * maven repository
     */
    private List<String> repositories;

    /**
     * filter
     */
    private List<String> includeGroups;

    /**
     * filter
     */
    private List<String> excludeGroups;

    /**
     * filter
     */
    private List<String> includeArtifacts;

    /**
     * filter
     */
    private List<String> excludeArtifacts;

    /**
     * filter, pattern
     */
    private List<String> includes;
    /**
     * filter, pattern
     */
    private List<String> excludes;
}
