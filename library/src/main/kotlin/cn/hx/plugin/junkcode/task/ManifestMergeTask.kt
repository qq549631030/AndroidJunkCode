package cn.hx.plugin.junkcode.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ManifestMergeTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val genManifestFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val updatedManifest: RegularFileProperty

    @TaskAction
    fun taskAction() {
        var genManifest = genManifestFile.get().asFile.readText()
        genManifest = genManifest.substring(genManifest.indexOf("<application>") + "<application>".length, genManifest.indexOf("</application>"))
        val mergedManifest = mergedManifest.get().asFile.readText()
        val finalManifest = mergedManifest.replace("</application>", "$genManifest\n</application>")
        updatedManifest.get().asFile.writeText(finalManifest)
    }
}