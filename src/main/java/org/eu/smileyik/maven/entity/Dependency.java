package org.eu.smileyik.maven.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eu.smileyik.maven.ModelInfo;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
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
}
