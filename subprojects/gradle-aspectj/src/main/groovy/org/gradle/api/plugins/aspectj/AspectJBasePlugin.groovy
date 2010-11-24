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
package org.gradle.api.plugins.aspectj;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.tasks.DefaultAspectJSourceSet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.aspectj.AspectJCompile;
import org.gradle.plugins.eclipse.EclipsePlugin;
import org.gradle.plugins.eclipse.EclipseProject;

public class AspectJBasePlugin implements Plugin<Project> {
    // public configurations
    public static final String ASPECTJ_TOOLS_CONFIGURATION_NAME = "aspectJTools";
    public static final String ASPECTS_CONFIGURATION_NAME = "aspects";

    public void apply(Project project) {
        JavaBasePlugin javaPlugin = project.plugins.apply(JavaBasePlugin.class);

        project.configurations.add(ASPECTJ_TOOLS_CONFIGURATION_NAME).setVisible(false).setTransitive(true).
                setDescription("The AspectJ tools libraries to be used for this AspectJ project.");
        project.configurations.add(ASPECTS_CONFIGURATION_NAME).setVisible(false).setTransitive(true).
                setDescription("Binary aspects to woven into this AspectJ project.");

        configureCompileDefaults(project, javaPlugin)
        configureSourceSetDefaults(project, javaPlugin)
        configureEclipseProject(project)
    }

    private void configureSourceSetDefaults(Project project, JavaBasePlugin javaPlugin) {
        project.convention.getPlugin(JavaPluginConvention.class).sourceSets.allObjects {SourceSet sourceSet ->
            sourceSet.convention.plugins.aspectJ = new DefaultAspectJSourceSet(sourceSet.displayName, project.fileResolver)
            sourceSet.aspectJ.srcDir { project.file("src/$sourceSet.name/aspectj")}
            sourceSet.allJava.add(sourceSet.aspectJ.matching(sourceSet.java.filter))
            sourceSet.allSource.add(sourceSet.aspectJ)
            sourceSet.resources.filter.exclude { FileTreeElement element -> sourceSet.aspectJ.contains(element.file) }

            String taskName = sourceSet.getCompileTaskName('aspectJ')
            AspectJCompile aspectJCompile = project.tasks.add(taskName, AspectJCompile.class);
            aspectJCompile.dependsOn sourceSet.compileJavaTaskName
            javaPlugin.configureForSourceSet(sourceSet, aspectJCompile);
            aspectJCompile.description = "Compiles the $sourceSet.aspectJ.";
            aspectJCompile.conventionMapping.defaultSource = { sourceSet.aspectJ }

            project.tasks[sourceSet.classesTaskName].dependsOn(taskName)
        }
    }

    private void configureCompileDefaults(final Project project, JavaBasePlugin javaPlugin) {
        project.tasks.withType(AspectJCompile.class).allTasks { AspectJCompile compile ->
            compile.compilerClasspath = project.configurations[ASPECTJ_TOOLS_CONFIGURATION_NAME]
            compile.aspectPath = project.configurations[ASPECTS_CONFIGURATION_NAME];
        }
    }
    
    private void configureEclipseProject(Project project) {
        project.tasks.withType(EclipseProject.class).allTasks { EclipseProject eclipseProject -> 
            eclipseProject.natures('org.eclipse.ajdt.ui.ajnature')
            eclipseProject.buildCommand('org.eclipse.ajdt.core.ajbuilder')
        }
    }
}