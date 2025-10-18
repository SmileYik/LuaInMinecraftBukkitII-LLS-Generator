package org.eu.smileyik.maven;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eu.smileyik.maven.entity.Dependency;
import org.eu.smileyik.maven.entity.DependencyManagement;
import org.eu.smileyik.maven.entity.Metadata;
import org.eu.smileyik.maven.entity.Project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Maven {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private List<String> repositories = new ArrayList<>();
    private LinkedList<String> currentRepos = new LinkedList<>();
    private List<String> excludes = new ArrayList<>();
    private List<String> includes = new ArrayList<>();

    private final ModelInfo modelInfo;
    private final Path outPath;
    private Project project;
    private Metadata metadata;

    public Maven(String model, Path outPath) {
        this.modelInfo = new ModelInfo(model);
        this.outPath = outPath;
    }

    private void init() {
        this.metadata = findMetadata(modelInfo);
        this.project = findProject(modelInfo, this.metadata);

        List<ModelInfo> dependencies = getDependencies(modelInfo, this.project)
                .parallelStream()
                .filter(it -> {
                    for (String exclude : excludes) {
                        if (it.matches(exclude)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(it -> {
                    for (String include : includes) {
                        if (it.matches(include)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public static interface Callback<T> {
        public T callback(String repo) throws Exception;
    }

    private <T> T find(Callback<T> callback) {
        for (String repo : currentRepos) {
            try {
                T result = callback.callback(repo);
                if (result != null) {
                    repoPopup(repo);
                    return result;
                }
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    private Project findProject(ModelInfo modelInfo) {
        Metadata metadata = findMetadata(modelInfo);
        return findProject(modelInfo, metadata);
    }

    private Project findProject(ModelInfo modelInfo, Metadata metadata) {
        List<Metadata.SnapshotVersion> snapshotVersions = metadata.getVersioning().getSnapshotVersions();
        if (snapshotVersions != null) {
            Optional<Metadata.SnapshotVersion> any = snapshotVersions.parallelStream().filter(Metadata.SnapshotVersion::isPom).findAny();
            if (any.isPresent()) {
                Metadata.SnapshotVersion snapshotVersion = any.get();
                Project project = find(repo -> {
                    String url = repo + modelInfo.getUrl(snapshotVersion);
                    byte[] bytes = Downloader.download(url, 8196, 60);
                    return MAPPER.readValue(bytes, Project.class);
                });
                if (project != null) {
                    return project;
                }
            }
        }
        throw new NullPointerException("Not found project");
    }

    private Metadata findMetadata(ModelInfo modelInfo) {
        Metadata metadata = find(repo -> {
            String url = repo + modelInfo.getMavenMetadataPath();
            byte[] bytes = Downloader.download(url, 8196, 60);
            return MAPPER.readValue(bytes, Metadata.class);
        });
        if (metadata == null) throw new NullPointerException("Not found metadata");
        return metadata;
    }

    private synchronized void repoPopup(String repo) {
        this.currentRepos.remove(repo);
        this.currentRepos.addFirst(repo);
    }

    public List<Dependency> getDependencies(ModelInfo modelInfo, Project project) {
        List<Dependency> dependencies = new ArrayList<>();
        if (project.getDependencies() != null) {
            dependencies.addAll(project.getDependencies());
        }
        DependencyManagement dependencyManagement = project.getDependencyManagement();
        if (dependencyManagement != null) {
            for (Dependency dependency : dependencyManagement.getDependencies()) {
                Project requireProject = find(repo -> {
                    String url = repo + dependency.toBomUrl();
                    byte[] bytes = Downloader.download(url, 8196, 60);
                    return MAPPER.readValue(bytes, Project.class);
                });
                if (requireProject != null) {
                    String model = dependency.toModel();
                    ModelInfo info = new ModelInfo(model);
                    dependencies.addAll(getDependencies(info, requireProject));
                }
            }
        }
        return dependencies;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories.clear();
        for (String repository : repositories) {
            repositories.add(repository.endsWith("/") ? repository : repository + "/");
        }
        this.currentRepos = new LinkedList<>(repositories);
    }
}
