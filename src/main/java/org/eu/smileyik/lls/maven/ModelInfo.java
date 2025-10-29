package org.eu.smileyik.lls.maven;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eu.smileyik.lls.maven.entity.Metadata;

@Getter
@ToString
@EqualsAndHashCode
@Setter
public class ModelInfo {
    private String groupId;
    private String artifactId;
    private String version;

    public ModelInfo() {

    }

    public ModelInfo(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public ModelInfo(String model) {
        String[] split = model.split(":");
        this.groupId = split[0];
        this.artifactId = split[1];
        boolean isNull = split.length < 3 || split[2].isEmpty() || split[2].equals("null");
        this.version = isNull ? null : split[2];
    }

    public String getMavenMetadataPath() {
        return getModelPath() + "maven-metadata.xml";
    }

    public String getModelPath() {
        return getGroupId().replace('.', '/') + "/" + getArtifactId() + "/" + getVersion() + "/";
    }

    public String getUrl(Metadata.SnapshotVersion snapshotVersion) {
        String classifier = snapshotVersion.getClassifier();
        String extension = snapshotVersion.getExtension();
        String value = snapshotVersion.getValue();
        return getModelPath() + getArtifactId() + "-" + value + (classifier == null ? "" : "-" + classifier) + "." + extension;
    }

    public String toModel() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
    }

    public String toModelNoVersion() {
        return getGroupId() + ":" + getArtifactId();
    }

    public boolean matches(String pattern) {
        return toModel().matches(pattern);
    }
}
