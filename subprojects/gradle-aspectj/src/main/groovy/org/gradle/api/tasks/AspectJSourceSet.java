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
package org.gradle.api.tasks;

import groovy.lang.Closure;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;

/**
 * A {@code AspectjSourceSetConvention} defines the properties and methods added
 * to a {@link org.gradle.api.tasks.SourceSet} by the
 * {@link org.gradle.api.plugins.aspectj.AspectjPlugin}.
 */
public interface AspectJSourceSet {
    /**
     * Returns the source to be compiled by the AspectJ compiler for this source
     * set. This may contain both Java and AspectJ source files.
     * 
     * @return The AspectJ source. Never returns null.
     */
    SourceDirectorySet getAspectJ();

    /**
     * Configures the AspectJ source for this set. The given closure is used to
     * configure the {@code SourceDirectorySet} which contains the AspectJ
     * source.
     * 
     * @param configureClosure
     *            The closure to use to configure the AspectJ source.
     * @return this
     */
    AspectJSourceSet aspectJ(Closure configureClosure);

    /**
     * All AspectJ source for this source set.
     * 
     * @return the AspectJ source. Never returns null.
     */
    FileTree getAllAspectJ();
}
