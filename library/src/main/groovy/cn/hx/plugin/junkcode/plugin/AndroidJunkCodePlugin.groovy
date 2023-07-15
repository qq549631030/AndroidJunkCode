package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.AndroidJunkCodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidJunkCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.findByName("android")
        if (!android || !android.hasProperty("applicationVariants")) {
            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
        }
        def androidComponents = project.extensions.findByName("androidComponents")
        //AGP 7.4.0+
        if (androidComponents && androidComponents.hasProperty("pluginVersion")
                && (androidComponents.pluginVersion.major > 7 || androidComponents.pluginVersion.minor >= 4)) {
            new NewVariantApiPlugin().apply(project)
            return
        }
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
                def generateJunkCodeTaskName = "generate${variantName.capitalize()}JunkCode"
                def generateJunkCodeTask = project.tasks.create(generateJunkCodeTaskName, AndroidJunkCodeTask) {
                    config = junkCodeConfig
                    namespace = junkCodeNamespace
                    outDir = junkCodeOutDir
                }
                def manifestFile = new File(junkCodeOutDir, "AndroidManifest.xml")
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
                def javaDir = new File(junkCodeOutDir, "java")
                variant.registerJavaGeneratingTask(generateJunkCodeTask, javaDir)
                def resDir = new File(junkCodeOutDir, "res")
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
            }
        }
    }
}