package org.eu.smileyik.lls.maven.entity;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@ToString
public class Metadata {
    private String groupId;
    private String artifactId;
    private String version;
    private Versioning versioning;

    @Data
    @ToString
    public static class Versioning {
        private String lastUpdated;
        private Snapshot snapshot;
        private List<SnapshotVersion> snapshotVersions = new ArrayList<>();

        public void addSourceJar(String version) {
            addVersion("sources", "jar", version);
        }

        public void addMainJar(String version) {
            addVersion("", "jar", version);
        }

        public void addPom(String version) {
            addVersion("", "pom", version);
        }

        public void addJavadoc(String version) {
            addVersion("javadoc", "jar", version);
        }

        private void addVersion(String classifier, String extension, String version) {
            SnapshotVersion snapshotVersion = new SnapshotVersion();
            snapshotVersion.setClassifier(classifier);
            snapshotVersion.setExtension(extension);
            snapshotVersion.setValue(version);
            snapshotVersions.add(snapshotVersion);
        }
    }

    @Data
    @ToString
    public static class Snapshot {
        private String timestamp;
        private Integer buildNumber;
    }

    @Data
    @ToString
    public static class SnapshotVersion {
        private String classifier;
        private String extension;
        private String value;
        private String updated;

        public boolean isSourceJar() {
            return equalType("sources", "jar");
        }

        public boolean isPom() {
            return equalType(null, "pom");
        }

        public boolean isMainJar() {
            return equalType(null, "jar");
        }

        protected boolean equalType(String classifier, String extension) {
            return Objects.equals(this.classifier, classifier) && Objects.equals(this.extension, extension);
        }
    }
}
