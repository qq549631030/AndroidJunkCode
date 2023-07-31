package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.utils.JunkUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

/**
 * before AGP 7.4.0
 */
abstract class AndroidJunkCodeTask extends DefaultTask {

    @Nested
    abstract JunkCodeConfig config

    @Input
    abstract String namespace

    @OutputDirectory
    abstract File javaOutDir

    @OutputDirectory
    abstract File resOutDir

    @OutputFile
    abstract File manifestOutFile

    private List<String> activityList = new ArrayList<>()

    @TaskAction
    void generateJunkCode() {
        javaOutDir.deleteDir()
        resOutDir.deleteDir()
        for (int i = 0; i < config.packageCount; i++) {
            String packageName
            if (config.packageBase.isEmpty()) {
                packageName = JunkUtil.generateName(i)
            } else {
                packageName = config.packageBase + "." + JunkUtil.generateName(i)
            }
            def list = JunkUtil.generateActivity(javaOutDir, resOutDir, namespace, packageName, config)
            activityList.addAll(list)
            JunkUtil.generateJava(javaOutDir, packageName, config)
        }
        JunkUtil.generateManifest(manifestOutFile, activityList)
        JunkUtil.generateDrawableFiles(resOutDir, config)
        JunkUtil.generateStringsFile(resOutDir, config)
        JunkUtil.generateKeep(resOutDir, config)
    }
}