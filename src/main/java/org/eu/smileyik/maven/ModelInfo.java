package org.eu.smileyik.maven;

import lombok.Getter;
import org.eu.smileyik.maven.entity.Metadata;

@Getter
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
        this.version = split[2];
    }

    public String getMavenMetadataPath() {
        return getModelPath() + "maven-metadata.xml";
    }

    public String getModelPath() {
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/";
    }

    public String getUrl(Metadata.SnapshotVersion snapshotVersion) {
        String classifier = snapshotVersion.getClassifier();
        String extension = snapshotVersion.getExtension();
        String value = snapshotVersion.getValue();
        return getModelPath() + artifactId + "-" + value + (classifier == null ? "" : "-" + classifier) + "." + extension;
    }
}
