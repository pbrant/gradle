/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.tasks.aspectj


import org.gradle.api.file.FileCollection
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.aspectj.AspectJCompileOptions;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AntAspectJCompiler implements AspectJCompiler {
    private static Logger logger = LoggerFactory.getLogger(AntAspectJCompiler)

    private final IsolatedAntBuilder antBuilder
    private final Iterable<File> bootclasspathFiles
    private final Iterable<File> extensionDirs
    
    FileCollection source
    
    FileCollection compilerClasspath;
    FileCollection aspectPath
    
    File destinationDir
    Iterable<File> classpath
    
    AspectJCompileOptions aspectJCompileOptions = new AspectJCompileOptions()
    
    
    def AntAspectJCompiler(IsolatedAntBuilder antBuilder) {
        this.antBuilder = antBuilder
        this.bootclasspathFiles = []
        this.extensionDirs = []
    }

    def AntAspectJCompiler(IsolatedAntBuilder antBuilder, Iterable<File> bootclasspathFiles, Iterable<File> extensionDirs) {
        this.antBuilder = antBuilder
        this.bootclasspathFiles = bootclasspathFiles
        this.extensionDirs = extensionDirs
    }

    WorkResult execute() {
        Map options = ['destDir': destinationDir] + aspectJCompileOptions.optionMap()
        
        antBuilder.withClasspath(compilerClasspath).execute { ant ->
            taskdef(resource: 'org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties')

            iajc(options) {
                source.addToAntBuilder(ant, 'sourceRoots', FileCollection.AntType.MatchingTask)
                
                aspectPath.each { file ->
                    aspectPath(location: file)
                }
                
                classpath(location: destinationDir)
                classpath.each { file ->
                    classpath(location: file)
                }
                
                bootclasspathFiles.each {file ->
                    bootclasspath(location: file)
                }
                
                extensionDirs.each {dir ->
                    extdirs(location: dir)
                }
            }
        }

        return { true } as WorkResult
    }
}
