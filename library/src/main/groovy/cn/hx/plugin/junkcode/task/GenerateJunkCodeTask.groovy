package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.utils.JunkUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

abstract class GenerateJunkCodeTask extends DefaultTask {

    @Nested
    abstract JunkCodeConfig config

    @Input
    abstract String namespace

    @OutputDirectory
    abstract DirectoryProperty getJavaOutputFolder()

    @OutputDirectory
    abstract DirectoryProperty getResOutputFolder()

    @OutputFile
    abstract RegularFileProperty getManifestOutputFile()

    @Internal
    List<String> activityList = new ArrayList<>()

    @TaskAction
    void taskAction() {
        def javaDir = getJavaOutputFolder().get().asFile
        def resDir = getResOutputFolder().get().asFile
        javaDir.deleteDir()
        resDir.deleteDir()
        for (int i = 0; i < config.packageCount; i++) {
            String packageName
            if (config.packageBase.isEmpty()) {
                packageName = JunkUtil.generateName(i)
            } else {
                packageName = config.packageBase + "." + JunkUtil.generateName(i)
            }
            def list = JunkUtil.generateActivity(javaDir, resDir, namespace, packageName, config)
            activityList.addAll(list)
            JunkUtil.generateJava(javaDir, packageName, config)
        }
        JunkUtil.generateManifest(getManifestOutputFile().get().asFile, activityList)
        JunkUtil.generateDrawableFiles(resDir, config)
        JunkUtil.generateStringsFile(resDir, config)
        JunkUtil.generateKeep(resDir, config)
    }
}