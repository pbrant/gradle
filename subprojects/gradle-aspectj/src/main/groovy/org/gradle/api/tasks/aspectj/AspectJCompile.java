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
package org.gradle.api.tasks.aspectj;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.project.IsolatedAntBuilder;
import org.gradle.api.internal.tasks.aspectj.AntAspectJCompiler;
import org.gradle.api.internal.tasks.aspectj.AspectJCompiler;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.compile.AbstractCompile;

public class AspectJCompile extends AbstractCompile {
    private FileCollection compilerClasspath;
    private FileCollection aspectPath;
    private AspectJCompiler compiler;
    
    public AspectJCompile() {
        compiler = new AntAspectJCompiler(getServices().get(IsolatedAntBuilder.class));
    }
    
    @Override
    protected void compile() {
        FileTree source = getSource();
        compiler.setSource(source);
        compiler.setDestinationDir(getDestinationDir());
        compiler.setClasspath(getClasspath());
        compiler.setCompilerClasspath(getCompilerClasspath());
        compiler.setAspectPath(getAspectPath());
        compiler.execute();
    }
    
    @Nested
    public AspectJCompileOptions getOptions() {
        return compiler.getAspectJCompileOptions();
    }

    @InputFiles
    public FileCollection getCompilerClasspath() {
        return compilerClasspath;
    }

    public void setCompilerClasspath(FileCollection compilerClasspath) {
        this.compilerClasspath = compilerClasspath;
    }
    
    @InputFiles
    public FileCollection getAspectPath() {
        return aspectPath;
    }
    
    public void setAspectPath(FileCollection aspectPath) {
        this.aspectPath = aspectPath;
    }
}
