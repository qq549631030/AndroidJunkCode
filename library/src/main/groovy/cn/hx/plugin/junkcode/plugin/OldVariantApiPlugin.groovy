package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.AndroidJunkCodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * before AGP 7.0.0
 */
class OldVariantApiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.findByName("android")
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt, project.container(JunkCodeConfig))
        android.applicationVariants.all { variant ->
            def variantName = variant.name
            def junkCodeConfig = generateJunkCodeExt.variantConfig.findByName(variantName)
            if (generateJunkCodeExt.debug) {
                println("AndroidJunkCode: generate code for variant $variantName? ${junkCodeConfig != null}")
            }
            if (junkCodeConfig) {
                def junkCodeNamespace = ""
                if (android.hasProperty("namespace") && android.namespace) {//AGP 4.2.0+
                    junkCodeNamespace = android.namespace
                } else {
                    //从AndroidManifest.xml找到package name
                    def parser = new XmlParser()
                    for (int i = 0; i < sourceSets.size(); i++) {
                        def sourceSet = sourceSets[i]
                        if (sourceSet.manifestFile.exists()) {
                            def node = parser.parse(sourceSet.manifestFile)
                            if (node.attribute("package")) {
                                junkCodeNamespace = node.attribute("package")
                                break
                            }
                        }
                    }
                }
                def junkCodeOutDir = new File(project.buildDir, "generated/source/junk/$variantName")
                def javaDir = new File(junkCodeOutDir, "java")
                def resDir = new File(junkCodeOutDir, "res")
                def manifestFile = new File(junkCodeOutDir, "AndroidManifest.xml")
                def proguardFile = new File(junkCodeOutDir, "proguard-rules.pro")
                def generateJunkCodeTask = project.tasks.create("generate${variantName.capitalize()}JunkCode", AndroidJunkCodeTask) {
                    config = junkCodeConfig
                    namespace = junkCodeNamespace
                    javaOutDir = javaDir
                    resOutDir = resDir
                    manifestOutFile = manifestFile
                    proguardOutFile = proguardFile
                }
                //将自动生成的AndroidManifest.xml加入到一个未被占用的manifest位置(如果都占用了就不合并了，通常较少出现全被占用情况)
                for (int i = variant.sourceSets.size() - 1; i >= 0; i--) {
                    def sourceSet = variant.sourceSets[i]
                    if (!sourceSet.manifestFile.exists()) {
                        sourceSet.manifest.srcFile(manifestFile)
                        //AGP4.1.0+
                        def processManifestTaskName = "process${variantName.capitalize()}MainManifest"
                        def processManifestTask = project.tasks.findByName(processManifestTaskName)
                        if (processManifestTask == null) {
                            //before AGP4.1.0
                            processManifestTaskName = "process${variantName.capitalize()}Manifest"
                            processManifestTask = project.tasks.findByName(processManifestTaskName)
                        }
                        if (processManifestTask) {
                            project.tasks.named(processManifestTaskName).configure {
                                it.dependsOn(generateJunkCodeTask)
                            }
                        }
                        break
                    }
                }
                variant.registerJavaGeneratingTask(generateJunkCodeTask, javaDir)
                if (variant.respondsTo("registerGeneratedResFolders")) {//AGP 3.0.0+
                    variant.registerGeneratedResFolders(project
                            .files(resDir)
                            .builtBy(generateJunkCodeTask))
                    if (variant.hasProperty("mergeResourcesProvider")) {//AGP 3.3.0+
                        variant.mergeResourcesProvider.configure { dependsOn(generateJunkCodeTask) }
                    } else {
                        variant.mergeResources.dependsOn(generateJunkCodeTask)
                    }
                } else {
                    variant.registerResGeneratingTask(generateJunkCodeTask, resDir)//AGP 1.1.0+
                }
                variant.getBuildType().buildType.proguardFile(proguardFile)
            }
        }
    }
}