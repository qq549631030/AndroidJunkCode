package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.utils.JunkUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

/**
 * before AGP 7.0.0
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

    @OutputFile
    abstract File proguardOutFile

    private List<String> packageList = new ArrayList<>()
    private List<String> activityList = new ArrayList<>()

    @TaskAction
    void generateJunkCode() {
        javaOutDir.deleteDir()
        resOutDir.deleteDir()
        if (config.javaGenerator) {//自定义生成java逻辑
            config.javaGenerator.execute(javaOutDir)
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
                def list = JunkUtil.generateActivity(javaOutDir, resOutDir, namespace, packageName, config)
                activityList.addAll(list)
                JunkUtil.generateJava(javaOutDir, packageName, config)
                packageList.add(packageName)
            }
            //生成混淆文件
            JunkUtil.generateProguard(proguardOutFile, packageList)
        }
        if (config.resGenerator) {//自定义生成res逻辑
            config.resGenerator.execute(resOutDir)
        } else {
            JunkUtil.generateDrawableFiles(resOutDir, config)
            JunkUtil.generateStringsFile(resOutDir, config)
            JunkUtil.generateKeep(resOutDir, config)
        }
        if (config.manifestGenerator) {//自定义生成manifest逻辑
            config.manifestGenerator.execute(manifestOutFile)
        } else {
            JunkUtil.generateManifest(manifestOutFile, activityList)
        }
    }
}