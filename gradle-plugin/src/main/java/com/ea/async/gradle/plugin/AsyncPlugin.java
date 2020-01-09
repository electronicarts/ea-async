/*
 Copyright (C) 2020 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ea.async.gradle.plugin;

import com.ea.async.instrumentation.Transformer;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A plugin that allows easily integrating EA Async instrumentation into the Gradle build process.
 */
public class AsyncPlugin implements Plugin<Project>
{

    /**
     * Applies the plugin to the project. Prepares Java compilation tasks to be followed up on by instrumentation.
     * @param project the Gradle project to which the plugin was applied
     */
    @Override
    public void apply(final Project project)
    {
        project.getTasks().withType(JavaCompile.class, task -> {
            // This will be called for every JavaCompile task, whether already existing or added dynamically later
            task.doLast("EA Async instrumentation", t -> instrumentCompileResults(task));
        });
    }

    /**
     * Rewrites compiled classes output by a Java compilation task.
     * Called for each compilation task after it finishes.
     * @param task the Java compilation task whose output to instrument
     */
    private void instrumentCompileResults(final JavaCompile task) {
        final Transformer asyncTransformer = new Transformer();
        asyncTransformer.setErrorListener(err -> {
            throw new RuntimeException("Failed to instrument the output of " + task.toString() + ": " + err);
        });
        final ClassLoader classLoader = getClass().getClassLoader();
        instrumentFile(task.getDestinationDir(), task, asyncTransformer, classLoader);
    }

    /**
     * Recursively instruments the specified file system entry and its descendants.
     * @param fsEntry a File in the output directory of a task
     * @param task the JavaCompile task whose output is being instrumented
     * @param asyncTransformer the transformer used to instrument the classes
     * @param classLoader the classloader to supply to the transformer
     */
    private void instrumentFile(final File fsEntry, final JavaCompile task,
                                final Transformer asyncTransformer, final ClassLoader classLoader) {
        if (fsEntry.isDirectory()) {
            for (File subentry : fsEntry.listFiles()) {
                instrumentFile(subentry, task, asyncTransformer, classLoader);
            }
        } else if (fsEntry.isFile() && fsEntry.getName().endsWith(".class")) {
            byte[] instrumentedClass;
            try (FileInputStream fis = new FileInputStream(fsEntry)) {
                instrumentedClass = asyncTransformer.instrument(classLoader, fis);
            } catch (IOException e) {
                throw new RuntimeException("Failed to instrument '" + fsEntry.getPath() + "' from " + task.toString(), e);
            }
            try {
                Files.write(Paths.get(fsEntry.getAbsolutePath()), instrumentedClass);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write '" + fsEntry.getPath() + "'", e);
            }
        }
    }

}
