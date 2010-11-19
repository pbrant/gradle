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
package org.gradle.api.tasks.aspectj

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.compile.AbstractOptions;

class AspectJCompileOptions extends AbstractOptions {
    boolean fork = false
    
    String maxMem = null
    
    boolean emacsSym = false
    
    boolean crossRefs = false
    
    boolean verbose = true
    
    String lintSettings = null
    
    boolean failOnError = true
    
    boolean showWeaveInfo = true
    
    List<String> warn
    
    @Input @Optional
    List<String> experimentalOptions
    
    boolean debug = true
    
    @Input @Optional
    String debugLevel = null
    
    boolean preserveAllLocals
    
    @Input @Optional
    String encoding
    
    boolean time = false
    
    @Input
    String sourceCompatibility = "1.6"
    
    @Input
    String targetCompatbility = "1.6"
    
    Map fieldName2AntMap() {
        [
            maxMem: 'maxmem',
            emacsSym: 'emacssym',
            crossRef: 'crossrefs',
            lintSettings: 'Xlint',
            failOnError: 'failonerror',
            experimentalOptions: 'X',
            sourceCompatibility: 'source',
            targetCompatbility: 'target',
        ]
    }
    
    @Override
    public Map fieldValue2AntMap() {
        [ 
            warn: { listToString(warn) },
            experimentalOptions: { listToString(experimentalOptions) }
        ] 
    }
    
    private String listToString(List<String> list) {
        if (list != null) {
            return list.isEmpty() ? ' ' : list.join(',')
        } else {
            return null;
        }
    }
}
