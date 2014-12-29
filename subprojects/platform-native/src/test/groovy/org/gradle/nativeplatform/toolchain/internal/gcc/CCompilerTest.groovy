/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.nativeplatform.toolchain.internal.gcc

import org.gradle.nativeplatform.internal.CompilerOutputFileNamingScheme
import org.gradle.nativeplatform.toolchain.internal.compilespec.CCompileSpec
import org.gradle.nativeplatform.toolchain.internal.CommandLineTool
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolInvocation
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

class CCompilerTest extends Specification {
    @Rule final TestNameTestDirectoryProvider tmpDirProvider = new TestNameTestDirectoryProvider()

    def executable = new File("executable")
    def invocation = Mock(MutableCommandLineToolInvocation)
    CommandLineTool commandLineTool = Mock(CommandLineTool)
    String objectFileExtension = ".o";
    CCompiler compiler = new CCompiler(commandLineTool, invocation, objectFileExtension, false);

    def "compiles all source files in separate executions"() {
        given:
        def testDir = tmpDirProvider.testDirectory
        def objectFileDir = testDir.file("output/objects")
        def genericArgs = [ "-x", "c",
                        "-Dfoo=bar", "-Dempty",
                        "-firstArg", "-secondArg",
                        "-c",
                        "-I", testDir.file("include.h").absolutePath ]

        when:
        CCompileSpec compileSpec = Stub(CCompileSpec) {
            getMacros() >> [foo: "bar", empty: null]
            getObjectFileDir() >> objectFileDir
            getAllArgs() >> ["-firstArg", "-secondArg"]
            getIncludeRoots() >> [testDir.file("include.h")]
            getSourceFiles() >> [testDir.file("one.c"), testDir.file("two.c")]
        }

        and:
        compiler.execute(compileSpec)

        then:

        1 * invocation.copy() >> invocation
        1 * invocation.setArgs(genericArgs)
        1 * invocation.getArgs() >> genericArgs

        ["one.c", "two.c"].each{ sourceFileName ->

            TestFile sourceFile = testDir.file(sourceFileName)
            File outputFile = outputFile(objectFileDir, sourceFile)
            Runnable run = Mock(Runnable)

            1 * invocation.copy() >> invocation
            1 * invocation.setWorkDirectory(objectFileDir)
            1 * invocation.clearPostArgsActions()
            1 * invocation.setArgs(genericArgs + [
                    testDir.file(sourceFileName).absolutePath,
                    "-o", outputFile.absolutePath])

            1 * commandLineTool.toRunnableExecution(invocation) >> run
            1 * run.run()
        }
        0 * _
    }

    File outputFile(File outputRoot, TestFile inputFile) {
        return new CompilerOutputFileNamingScheme()
                .withOutputBaseFolder(outputRoot)
                .withObjectFileNameSuffix(objectFileExtension)
                .map(inputFile)
    }
}
