package cn.hx.plugin.junkcode.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class ManifestMergeTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getGenManifestFile()

    @InputFile
    abstract RegularFileProperty getMergedManifest()

    @OutputFile
    abstract RegularFileProperty getUpdatedManifest()

    @TaskAction
    void taskAction() {
        String genManifest = new String(getGenManifestFile().get().asFile.readBytes())
        genManifest = genManifest.substring(genManifest.indexOf("<application>") + "<application>".length(), genManifest.indexOf("</application>"))
        String manifest = new String(getMergedManifest().get().asFile.readBytes())
        manifest = manifest.replace("</application>", "$genManifest\n</application>")
        getUpdatedManifest().get().asFile.write(manifest)
    }
}