package org.eu.smileyik.maven.entity;

import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.maven.ModelInfo;

@Data
@ToString
public class Dependency extends ModelInfo {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String type;

    public String toBomUrl() {
        return getGroupId().replace(".", "/") +
                "/" + getArtifactId() +
                "/" + getVersion() +
                "/" + getArtifactId() + "-" + getVersion() + ".pom";
    }

    public String toModel() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean matches(String pattern) {
        return toModel().matches(pattern);
    }
}
