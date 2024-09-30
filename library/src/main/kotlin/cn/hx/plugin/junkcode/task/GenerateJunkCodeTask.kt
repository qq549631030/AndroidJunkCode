package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.utils.JunkUtil
import groovy.lang.Tuple2
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateJunkCodeTask : DefaultTask() {

    @get:Nested
    abstract val config: Property<JunkCodeConfig>

    @get:Input
    abstract val namespace: Property<String>

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    private val packageList = mutableListOf<String>()

    private val activityList = mutableListOf<String>()

    @TaskAction
    fun taskAction() {
        outputFolder.get().asFile.delete()
        packageList.clear()
        activityList.clear()
        val junkCodeConfig = config.get()
        val javaDir = outputFolder.dir("java").get().asFile
        val resDir = outputFolder.dir("res").get().asFile
        val manifestOutputFile = outputFolder.file("AndroidManifest.xml").get().asFile
        val proguardOutputFile = outputFolder.file("proguard-rules.pro").get().asFile
        junkCodeConfig.javaGenerator?.execute(javaDir) ?: run {
            for (i in 0 until junkCodeConfig.packageCount) {
                val packageName = junkCodeConfig.packageCreator?.let {
                    StringBuilder().apply { it.execute(Tuple2(i, this)) }.toString()
                } ?: run {
                    junkCodeConfig.packageBase.takeIf { it.isNotEmpty() }?.let { it + "." + JunkUtil.generateName(i) } ?: JunkUtil.generateName(i)
                }
                val list = JunkUtil.generateActivity(javaDir, resDir, namespace.get(), packageName, junkCodeConfig)
                activityList.addAll(list)
                JunkUtil.generateJava(javaDir, packageName, junkCodeConfig)
                packageList.add(packageName)
            }
            //生成混淆文件
            JunkUtil.generateProguard(proguardOutputFile, packageList, junkCodeConfig)
        }
        junkCodeConfig.resGenerator?.execute(resDir) ?: run {
            JunkUtil.generateDrawableFiles(resDir, junkCodeConfig)
            JunkUtil.generateStringsFile(resDir, junkCodeConfig)
            JunkUtil.generateKeep(resDir, junkCodeConfig)
        }
        junkCodeConfig.manifestGenerator?.execute(manifestOutputFile) ?: run {
            JunkUtil.generateManifest(manifestOutputFile, activityList)
        }
    }
}