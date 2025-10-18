package org.eu.smileyik.maven.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class DependencyManagement {
    private List<Dependency> dependencies;
}
