package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.utils.JunkUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class GenerateJunkCodeTask extends DefaultTask {

    @Nested
    abstract JunkCodeConfig config

    @Input
    abstract Property<String> getNamespace()

    @OutputDirectory
    abstract DirectoryProperty getJavaOutputFolder()

    @OutputDirectory
    abstract DirectoryProperty getResOutputFolder()

    @OutputFile
    abstract RegularFileProperty getManifestOutputFile()

    @OutputFile
    abstract RegularFileProperty getProguardOutputFile()


    private List<String> packageList = new ArrayList<>()
    private List<String> activityList = new ArrayList<>()

    @TaskAction
    void taskAction() {
        def javaDir = getJavaOutputFolder().get().asFile
        def resDir = getResOutputFolder().get().asFile
        javaDir.deleteDir()
        resDir.deleteDir()
        if (config.javaGenerator) {
            config.javaGenerator.execute(javaDir)
        } else {
            for (int i = 0; i < config.packageCount; i++) {
                String packageName
                if (config.packageCreator) {
                    def packageNameBuilder = new StringBuffer()
                    config.packageCreator.execute(new Tuple2(i, packageNameBuilder))
                    packageName = packageNameBuilder.toString()
                } else {
                    if (config.packageBase.isEmpty()) {
                        packageName = JunkUtil.generateName(i)
                    } else {
                        packageName = config.packageBase + "." + JunkUtil.generateName(i)
                    }
                }
                def list = JunkUtil.generateActivity(javaDir, resDir, getNamespace().get(), packageName, config)
                activityList.addAll(list)
                JunkUtil.generateJava(javaDir, packageName, config)
                packageList.add(packageName)
            }
            //生成混淆文件
            JunkUtil.generateProguard(getProguardOutputFile().get().asFile, packageList)
        }
        if (config.resGenerator) {
            config.resGenerator.execute(resOutDir)
        } else {
            JunkUtil.generateDrawableFiles(resDir, config)
            JunkUtil.generateStringsFile(resDir, config)
            JunkUtil.generateKeep(resDir, config)
        }
        if (config.manifestGenerator) {
            config.manifestGenerator.execute(getManifestOutputFile().get().asFile)
        } else {
            JunkUtil.generateManifest(getManifestOutputFile().get().asFile, activityList)
        }
    }
}