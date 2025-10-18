package org.eu.smileyik.maven.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Project {
    private String modelVersion;
    private String groupId;
    private String artifactId;
    private String version;
    private DependencyManagement dependencyManagement;
    private List<Dependency> dependencies;
}
