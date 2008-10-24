/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.dependencies.maven.deploy;

import org.apache.ivy.plugins.resolver.ResolverSettings;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.plugins.namespace.Namespace;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.search.OrganisationEntry;
import org.apache.ivy.core.search.ModuleEntry;
import org.apache.ivy.core.search.RevisionEntry;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.artifact.ant.DeployTask;
import org.apache.maven.artifact.ant.Pom;
import org.gradle.util.AntUtil;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.dependencies.maven.PublishFilter;
import org.gradle.api.dependencies.maven.MavenPom;
import org.gradle.api.dependencies.maven.MavenUploader;
import org.gradle.api.internal.dependencies.maven.MavenPomFactory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

import java.text.ParseException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Hans Dockter
 */
public class BaseMavenUploader implements MavenUploader {

    private String name;

    private RemoteRepository remoteRepository;

    private RemoteRepository remoteSnapshotRepository;

    private DeployTaskFactory deployTaskFactory = new DefaultDeployTaskFactory();

    private ArtifactPomContainer artifactPomContainer;

    private MavenPomFactory mavenPomFactory;

    private List<File> protocolProviderJars = new ArrayList<File>();

    public BaseMavenUploader(String name, ArtifactPomContainer artifactPomContainer, MavenPomFactory mavenPomFactory) {
        this.name = name;
        this.artifactPomContainer = artifactPomContainer;
        this.mavenPomFactory = mavenPomFactory;
        this.artifactPomContainer.setDefaultArtifactPom(new DefaultArtifactPom(DEFAULT_ARTIFACT_POM_NAME,
                mavenPomFactory.createMavenPom(),
                PublishFilter.ALWAYS_ACCEPT));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public DownloadReport download(Artifact[] artifacts, DownloadOptions options) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ArtifactDownloadReport download(ArtifactOrigin artifact, DownloadOptions options) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public boolean exists(Artifact artifact) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ArtifactOrigin locate(Artifact artifact) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        if (isIgnorable(artifact)) {
            return;
        }
        artifactPomContainer.addArtifact(artifact, src);
    }

    private boolean isIgnorable(Artifact artifact) {
        return artifact.getType().equals("ivy");
    }

    public void beginPublishTransaction(ModuleRevisionId module, boolean overwrite) throws IOException {
        // do nothing
    }

    public void abortPublishTransaction() throws IOException {
        // do nothing
    }

    public void commitPublishTransaction() throws IOException {
        DeployTaskWithVisibleContainerProperty deployTask = deployTaskFactory.createDeployTask();
        deployTask.setProject(AntUtil.createProject());
        addRemoteRepositories(deployTask);
        addProtocolProvider(deployTask);

        Map<File,File> deployableUnits = artifactPomContainer.createDeployableUnits();
        for (File pomFile : deployableUnits.keySet()) {
            addPomAndArtifact(deployTask, pomFile, deployableUnits.get(pomFile));
            deployTask.execute();
        }
    }

    private void addProtocolProvider(DeployTaskWithVisibleContainerProperty deployTask) {
        PlexusContainer plexusContainer = deployTask.getContainer();
        for (File wagonProviderJar : protocolProviderJars) {
            try {
                plexusContainer.addJarResource(wagonProviderJar);
            } catch (PlexusContainerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addPomAndArtifact(DeployTask deployTask, File pomFile, File artifactFile) {
        Pom pom = new Pom();
        pom.setFile(pomFile);
        deployTask.addPom(pom);
        deployTask.setFile(artifactFile);
    }

    private void addRemoteRepositories(DeployTask deployTask) {
        deployTask.addRemoteRepository(remoteRepository);
        deployTask.addRemoteSnapshotRepository(remoteSnapshotRepository);
    }

    public void reportFailure() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void reportFailure(Artifact art) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public String[] listTokenValues(String token, Map otherTokenValues) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public Map[] listTokenValues(String[] tokens, Map criteria) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public OrganisationEntry[] listOrganisations() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public ModuleEntry[] listModules(OrganisationEntry org) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public RevisionEntry[] listRevisions(ModuleEntry module) {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public Namespace getNamespace() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void dumpSettings() {
        throw new UnsupportedOperationException("A MavenPublishOnlyResolver can only publish artifacts.");
    }

    public void setSettings(ResolverSettings settings) {
        // do nothing
    }

    public RepositoryCacheManager getRepositoryCacheManager() {
        return new DefaultRepositoryCacheManager();
    }

    public RemoteRepository getRepository() {
        return remoteRepository;
    }

    public void setRepository(RemoteRepository remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    public RemoteRepository getSnapshotRepository() {
        return remoteSnapshotRepository;
    }

    public void setSnapshotRepository(RemoteRepository remoteSnapshotRepository) {
        this.remoteSnapshotRepository = remoteSnapshotRepository;
    }

    public DeployTaskFactory getDeployTaskFactory() {
        return deployTaskFactory;
    }

    public void setDeployTaskFactory(DeployTaskFactory deployTaskFactory) {
        this.deployTaskFactory = deployTaskFactory;
    }

    public PublishFilter getFilter() {
        return artifactPomContainer.getDefaultArtifactPom().getFilter();
    }

    public void setFilter(PublishFilter defaultFilter) {
        this.artifactPomContainer.getDefaultArtifactPom().setFilter(defaultFilter);
    }

    public MavenPom getPom() {
        return artifactPomContainer.getDefaultArtifactPom().getPom();
    }

    public void setPom(MavenPom defaultPom) {
        this.artifactPomContainer.getDefaultArtifactPom().setPom(defaultPom);
    }

    public MavenPomFactory getMavenPomFactory() {
        return mavenPomFactory;
    }

    public void setMavenPomFactory(MavenPomFactory mavenPomFactory) {
        this.mavenPomFactory = mavenPomFactory;
    }

    public void addProtocolProviderJars(List<File> jars) {
        protocolProviderJars.addAll(jars);
    }

    public MavenPom addFilter(String name, PublishFilter publishFilter) {
        if (name == null || publishFilter == null) {
            throw new InvalidUserDataException("Name and Filter must not be null.");
        }
        MavenPom pom = mavenPomFactory.createMavenPom();
        artifactPomContainer.addArtifactPom(new DefaultArtifactPom(name, pom, publishFilter));
        return pom;
    }

    public PublishFilter filter(String name) {
        if (name == null) {
            throw new InvalidUserDataException("Name must not be null.");
        }
        return artifactPomContainer.getArtifactPom(name).getFilter();
    }

    public MavenPom pom(String name) {
        if (name == null) {
            throw new InvalidUserDataException("Name must not be null.");
        }
        return artifactPomContainer.getArtifactPom(name).getPom();
    }

    public ArtifactPomContainer getArtifactPomContainer() {
        return artifactPomContainer;
    }

    public void setArtifactPomContainer(ArtifactPomContainer artifactPomContainer) {
        this.artifactPomContainer = artifactPomContainer;
    }
}