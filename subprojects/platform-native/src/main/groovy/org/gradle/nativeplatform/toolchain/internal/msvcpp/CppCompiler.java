/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.nativeplatform.toolchain.internal.msvcpp;

import org.gradle.api.Transformer;
import org.gradle.nativeplatform.toolchain.internal.*;
import org.gradle.nativeplatform.toolchain.internal.compilespec.CppCompileSpec;

import java.io.File;
import java.util.List;

class CppCompiler extends NativeCompiler<CppCompileSpec> {

    CppCompiler(CommandLineTool commandLineTool, CommandLineToolInvocation invocation, Transformer<CppCompileSpec, CppCompileSpec> specTransformer, String objectFileSuffix) {
        super(commandLineTool, invocation, new CppCompilerArgsTransformer(), specTransformer, objectFileSuffix, true);
    }

    protected OptionsFileArgsWriter optionsFileTransformer(File tempDir) {
        return new VisualCppOptionsFileArgWriter(tempDir);
    }

    private static class CppCompilerArgsTransformer extends VisualCppCompilerArgsTransformer<CppCompileSpec> {
        protected String getLanguageOption() {
            return "/TP";
        }
    }

    @Override
    protected void addOutputArgs(List<String> args, File outputFile) {
        // MSVC doesn't allow a space between Fo and the file name
        args.add("/Fo" + outputFile.getAbsolutePath());
    }
}
