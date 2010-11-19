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
package org.gradle.api.internal.tasks;

import groovy.lang.Closure;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.tasks.AspectJSourceSet;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.ConfigureUtil;

public class DefaultAspectJSourceSet implements AspectJSourceSet {
    private final SourceDirectorySet aspectJ;
    private final UnionFileTree allAspectJ;
    private final PatternFilterable aspectJPatterns = new PatternSet();

    public DefaultAspectJSourceSet(String displayName, FileResolver fileResolver) {
        aspectJ = new DefaultSourceDirectorySet(String.format("%s AspectJ source", displayName), fileResolver);
        aspectJ.getFilter().include("**/*.java", "**/*.aj");
        aspectJPatterns.include("**/*.aj");
        allAspectJ = new UnionFileTree(String.format("%s AspectJ source", displayName), aspectJ.matching(aspectJPatterns));
    }

    public SourceDirectorySet getAspectJ() {
        return aspectJ;
    }

    public AspectJSourceSet aspectJ(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getAspectJ());
        return this;
    }

    public PatternFilterable getAspectJSourcePatterns() {
        return aspectJPatterns;
    }

    public FileTree getAllAspectJ() {
        return allAspectJ;
    }
}