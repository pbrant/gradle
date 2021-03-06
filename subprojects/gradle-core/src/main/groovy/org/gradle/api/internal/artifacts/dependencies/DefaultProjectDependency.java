/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.api.internal.artifacts.dependencies;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.ProjectDependenciesBuildInstruction;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.CachingDependencyResolveContext;
import org.gradle.api.internal.artifacts.DependencyResolveContext;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.Set;

/**
 * @author Hans Dockter
 */
public class DefaultProjectDependency extends AbstractModuleDependency implements ProjectDependency {
    private Project dependencyProject;
    private final ProjectDependenciesBuildInstruction instruction;
    private final TaskDependencyImpl taskDependency = new TaskDependencyImpl();

    public DefaultProjectDependency(Project dependencyProject, ProjectDependenciesBuildInstruction instruction) {
        this(dependencyProject, null, instruction);
    }

    public DefaultProjectDependency(Project dependencyProject, String configuration,
                                    ProjectDependenciesBuildInstruction instruction) {
        super(configuration);
        this.dependencyProject = dependencyProject;
        this.instruction = instruction;
    }

    public Project getDependencyProject() {
        return dependencyProject;
    }

    public String getGroup() {
        return dependencyProject.getGroup().toString();
    }

    public String getName() {
        return dependencyProject.getName();
    }

    public String getVersion() {
        return dependencyProject.getVersion().toString();
    }

    public Configuration getProjectConfiguration() {
        return dependencyProject.getConfigurations().getByName(getConfiguration());
    }

    public ProjectDependency copy() {
        DefaultProjectDependency copiedProjectDependency = new DefaultProjectDependency(dependencyProject,
                getConfiguration(), instruction);
        copyTo(copiedProjectDependency);
        return copiedProjectDependency;
    }

    public Set<File> resolve() {
        return resolve(true);
    }

    public Set<File> resolve(boolean transitive) {
        CachingDependencyResolveContext context = new CachingDependencyResolveContext(transitive);
        context.add(this);
        return context.resolve().getFiles();
    }

    @Override
    public void resolve(DependencyResolveContext context) {
        boolean transitive = isTransitive() && context.isTransitive();
        for (Dependency dependency : getProjectConfiguration().getAllDependencies()) {
            if (!(dependency instanceof ProjectDependency)) {
                context.add(dependency);
            } else if (transitive) {
                context.add(dependency);
            }
            // else project dep and non-transitive, so skip
        }
    }

    public TaskDependency getBuildDependencies() {
        return taskDependency;
    }

    public boolean contentEquals(Dependency dependency) {
        if (this == dependency) {
            return true;
        }
        if (dependency == null || getClass() != dependency.getClass()) {
            return false;
        }

        ProjectDependency that = (ProjectDependency) dependency;
        if (!isCommonContentEquals(that)) {
            return false;
        }

        return dependencyProject.equals(that.getDependencyProject());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultProjectDependency that = (DefaultProjectDependency) o;
        if (!this.getDependencyProject().equals(that.getDependencyProject())) {
            return false;
        }
        if (!this.getConfiguration().equals(that.getConfiguration())) {
            return false;
        }
        if (!this.instruction.equals(that.instruction)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getDependencyProject().hashCode() ^ getConfiguration().hashCode() ^ instruction.hashCode();
    }

    @Override
    public String toString() {
        return "DefaultProjectDependency{" + "dependencyProject='" + dependencyProject + '\'' + ", configuration='"
                + getConfiguration() + '\'' + '}';
    }

    private class TaskDependencyImpl extends AbstractTaskDependency {
        public void resolve(TaskDependencyResolveContext context) {
            if (!instruction.isRebuild()) {
                return;
            }
            Configuration configuration = getProjectConfiguration();
            context.add(configuration);
            context.add(configuration.getBuildArtifacts());
            for (String taskName : instruction.getTaskNames()) {
                context.add(dependencyProject.getTasks().getByName(taskName));
            }
        }
    }
}