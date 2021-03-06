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

package org.gradle.api.internal.tasks.testing.worker;

import org.gradle.api.Action;
import org.gradle.api.internal.Factory;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.internal.WorkerProcess;
import org.gradle.process.internal.WorkerProcessBuilder;

import java.io.File;

public class ForkingTestClassProcessor implements TestClassProcessor {
    private final Factory<? extends WorkerProcessBuilder> workerFactory;
    private final WorkerTestClassProcessorFactory processorFactory;
    private final JavaForkOptions options;
    private final Iterable<File> classPath;
    private final Action<WorkerProcessBuilder> buildConfigAction;
    private RemoteTestClassProcessor remoteProcessor;
    private WorkerProcess workerProcess;
    private TestResultProcessor resultProcessor;

    public ForkingTestClassProcessor(Factory<? extends WorkerProcessBuilder> workerFactory, WorkerTestClassProcessorFactory processorFactory, JavaForkOptions options, Iterable<File> classPath, Action<WorkerProcessBuilder> buildConfigAction) {
        this.workerFactory = workerFactory;
        this.processorFactory = processorFactory;
        this.options = options;
        this.classPath = classPath;
        this.buildConfigAction = buildConfigAction;
    }

    public void startProcessing(TestResultProcessor resultProcessor) {
        this.resultProcessor = resultProcessor;
    }

    public void processTestClass(TestClassRunInfo testClass) {
        if (remoteProcessor == null) {
            WorkerProcessBuilder builder = workerFactory.create();
            builder.applicationClasspath(classPath);
            builder.setLoadApplicationInSystemClassLoader(true);
            builder.worker(new TestWorker(processorFactory));
            options.copyTo(builder.getJavaCommand());
            buildConfigAction.execute(builder);
            
            workerProcess = builder.build();
            workerProcess.start();

            workerProcess.getConnection().addIncoming(TestResultProcessor.class, resultProcessor);
            remoteProcessor = workerProcess.getConnection().addOutgoing(RemoteTestClassProcessor.class);

            remoteProcessor.startProcessing();
        }

        remoteProcessor.processTestClass(testClass);
    }

    public void stop() {
        if (remoteProcessor != null) {
            remoteProcessor.stop();
            workerProcess.waitForStop();
        }
    }
}
