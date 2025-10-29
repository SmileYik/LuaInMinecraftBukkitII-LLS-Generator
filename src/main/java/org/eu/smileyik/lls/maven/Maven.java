package org.eu.smileyik.lls.maven;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Setter;
import org.eu.smileyik.lls.SourceGenerator;
import org.eu.smileyik.lls.maven.entity.Dependency;
import org.eu.smileyik.lls.maven.entity.DependencyManagement;
import org.eu.smileyik.lls.maven.entity.Metadata;
import org.eu.smileyik.lls.maven.entity.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class Maven {
    private static final ObjectMapper MAPPER = new XmlMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static interface Callback<T> {
        public T callback(String repo) throws Exception;
    }

    private static final class Info {
        private boolean source;
        private Path path;
        private ModelInfo modelInfo;
    }

    private List<String> repositories = new ArrayList<>();
    private LinkedList<String> currentRepos = new LinkedList<>();
    @Setter
    private List<String> excludes = new ArrayList<>();
    @Setter
    private List<String> includes = new ArrayList<>();

    private final ModelInfo modelInfo;
    private final Path outPath;
    private final Path downloadPath;
    private Project project;
    private Metadata metadata;

    public Maven(String model, Path outPath, Path downloadPath) {
        this.modelInfo = new ModelInfo(model);
        this.outPath = outPath;
        this.downloadPath = downloadPath;
    }

    public void generate() {
        this.metadata = findMetadata(modelInfo);
        this.project = findProject(modelInfo, this.metadata);

        Set<String> checked = new HashSet<>();
        List<ModelInfo> dependencies = getDependencies(modelInfo, this.project)
                .stream()
                .filter(it -> checked.add(it.toModel()))
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
        Set<Info> collect = dependencies.stream()
                .map(modelInfo -> {
                    Metadata meta;
                    try {
                        meta = findMetadata(modelInfo);
                    } catch (Exception e) {
                        meta = new Metadata();
                        Metadata.Versioning versioning = new Metadata.Versioning();
                        versioning.addSourceJar(modelInfo.getVersion());
                        versioning.addMainJar(modelInfo.getVersion());
                        versioning.addJavadoc(modelInfo.getVersion());
                        versioning.addPom(modelInfo.getVersion());
                        meta.setVersioning(versioning);
                    }
                    Metadata.SnapshotVersion target = meta.getVersioning()
                            .getSnapshotVersions()
                            .stream()
                            .filter(Metadata.SnapshotVersion::isSourceJar)
                            .findFirst()
                            .orElse(meta.getVersioning()
                                    .getSnapshotVersions()
                                    .stream()
                                    .filter(Metadata.SnapshotVersion::isMainJar)
                                    .findFirst()
                                    .orElse(null));
                    if (target == null) return null;
                    Path filePath = find(repo -> {
                        String pathStr = modelInfo.getUrl(target);
                        Path path = downloadPath.resolve(pathStr);
                        if (Files.notExists(path)) {
                            try {
                                String url = repo + pathStr;
                                Path parent = path.getParent();
                                if (!Files.exists(parent)) {
                                    Files.createDirectories(parent);
                                }
                                System.out.println(url);
                                Downloader.download(url, 8192, 60, path.toFile());
                            } catch (Exception e) {
                                return null;
                            }
                        }
                        return path;
                    });
                    Info info = new Info();
                    info.path = filePath;
                    info.source = target.isSourceJar();
                    info.modelInfo = modelInfo;
                    return info;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Info info : collect) {
            if (info.source && info.path != null) {
                try {
                    SourceGenerator.generate(new ZipFile(info.path.toFile()), outPath, (a, b) -> {
                        return info.modelInfo.getGroupId() + "." + info.modelInfo.getArtifactId();
                    });
                } catch (IOException e) {
                    System.err.println("Failed to generate " + info.modelInfo);
                    e.printStackTrace();
                }
            }
        }
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
        System.out.println(modelInfo);
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

    public List<ModelInfo> getDependencies(ModelInfo modelInfo, Project project) {
        return getDependencies(modelInfo, project, new HashSet<>());
    }

    public List<ModelInfo> getDependencies(ModelInfo modelInfo, Project project, Set<String> checked) {
        List<ModelInfo> dependencies = new ArrayList<>();
        if (project.getDependencies() != null) {
            dependencies.addAll(project.getDependencies());
        }
        dependencies.add(modelInfo);


        List<ModelInfo> managementDepends = getDependencies(project.getDependencyManagement(), checked);
        Map<String, ModelInfo> map = new HashMap<>();
        for (ModelInfo m : managementDepends) {
            map.put(m.toModelNoVersion(), m);
        }
        for (ModelInfo model : dependencies) {
            ModelInfo managerModel = map.remove(model.toModelNoVersion());
            if (managerModel != null) {
                model.setVersion(managerModel.getVersion());
            }
        }
        dependencies.addAll(map.values());

        return dependencies;
    }

    private List<ModelInfo> getDependencies(DependencyManagement dependencyManagement, Set<String> checked) {
        List<ModelInfo> dependencies = new ArrayList<>();
        if (dependencyManagement != null) {
            for (Dependency dependency : dependencyManagement.getDependencies()) {
                Project requireProject = find(repo -> {
                    String url = repo + dependency.toBomUrl();
                    byte[] bytes = Downloader.download(url, 8196, 60);
                    return MAPPER.readValue(bytes, Project.class);
                });
                if (requireProject != null && checked.add(dependency.toModelNoVersion())) {
                    String model = dependency.toModel();
                    ModelInfo info = new ModelInfo(model);
                    dependencies.addAll(getDependencies(info, requireProject, checked));
                }
            }
        }
        return dependencies;
    }


    public Maven setRepositories(List<String> repositories) {
        this.repositories.clear();
        repositories = new ArrayList<>(repositories);
        for (String repository : repositories) {
            this.repositories.add(repository.endsWith("/") ? repository : repository + "/");
        }
        this.currentRepos = new LinkedList<>(this.repositories);
        return this;
    }

    public Maven addRepository(String repoUrl) {
        List<String> list = new ArrayList<>(this.repositories);
        list.add(repoUrl);
        return setRepositories(list);
    }

    public Maven includeGroup(String group) {
        this.includes.add(String.format("%s:.+:.+", group.replace("-", "\\-").replace(".", "\\.")));
        return this;
    }

    public Maven includeArtifact(String artifact) {
        this.includes.add(String.format(".+:%s:.+", artifact.replace("-", "\\-")));
        return this;
    }

    public Maven includes(String ... patterns) {
        this.includes.addAll(Arrays.asList(patterns));
        return this;
    }

    public Maven excludeGroup(String group) {
        this.excludes.add(String.format("%s:.+:.+", group.replace("-", "\\-").replace(".", "\\.")));
        return this;
    }

    public Maven excludeArtifact(String artifact) {
        this.excludes.add(String.format(".+:%s:.+", artifact.replace("-", "\\-")));
        return this;
    }

    public Maven excludes(String ... patterns) {
        this.excludes.addAll(Arrays.asList(patterns));
        return this;
    }

    public static void generate(MavenConfig config) {
        Path outputPath = Paths.get(config.getOutputPath());
        Path cachePath = Paths.get(config.getCachePath());
        Maven maven = new Maven(config.getModel(), outputPath, cachePath);
        maven.setRepositories(config.getRepositories());
        if (config.getIncludeGroups() != null) {
            for (String include : config.getIncludeGroups()) {
                maven.includeGroup(include);
            }
        }
        if (config.getExcludeGroups() != null) {
            for (String exclude : config.getExcludeGroups()) {
                maven.excludeGroup(exclude);
            }
        }
        if (config.getIncludeArtifacts() != null) {
            for (String include : config.getIncludeArtifacts()) {
                maven.includeArtifact(include);
            }
        }
        if (config.getExcludeArtifacts() != null) {
            for (String exclude : config.getExcludeArtifacts()) {
                maven.excludeArtifact(exclude);
            }
        }
        if (config.getIncludes() != null) {
            for (String include : config.getIncludes()) {
                maven.includes(include);
            }
        }
        if (config.getExcludes() != null) {
            for (String exclude : config.getExcludes()) {
                maven.excludes(exclude);
            }
        }
        maven.generate();
    }

    public static void generate(MavenConfig ... configs) {
        for (MavenConfig config : configs) {
            generate(config);
        }
    }
}
