/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.build.docs

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Verifies that the text of the [projectPathProvider] can be found in the [tipOfTreeBuildFile] to
 * enforce that projects enable docs generation.
 */
@CacheableTask
abstract class CheckTipOfTreeDocsTask : DefaultTask() {
    @get:[InputFile PathSensitive(PathSensitivity.NONE)]
    abstract val tipOfTreeBuildFile: RegularFileProperty

    @get:Input
    abstract val projectPathProvider: Property<String>

    @TaskAction
    fun exec() {
        // Make sure not to allow a partial project path match, e.g. ":activity:activity" shouldn't
        // match ":activity:activity-ktx", both need to be listed separately.
        val projectPath = projectPathProvider.get()
        val fullExpectedText = "project(\"$projectPath\")"
        if (!tipOfTreeBuildFile.asFile.get().readText().contains(fullExpectedText)) {
            val message = "Project $projectPath not found in docs-tip-of-tree/build.gradle\n\n" +
                "Use the project creation script (development/project-creator/create_project.py) " +
                "when setting up a project to make sure all required steps are complete.\n\n" +
                "The project should be added to docs-tip-of-tree/build.gradle as " +
                "\'docs(project(\"$projectPath\"))\' (use 'kmpDocs' instead of 'docs' for KMP " +
                "projects).\n\n" +
                "If this project should not have published refdocs, first check that the library " +
                "type listed in its build.gradle file is accurate. If it is, opt out of refdoc " +
                "generation using \'doNotDocumentReason = \"some reason\"\' in the 'androidx' " +
                "configuration section (this is not common)."
            throw GradleException(message)
        }
    }
}