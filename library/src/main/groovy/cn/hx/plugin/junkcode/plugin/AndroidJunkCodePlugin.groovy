package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.AndroidJunkCodeTask
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidJunkCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.findByType(AppExtension)
        if (!android) {
            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
        }
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt)
        generateJunkCodeExt.variantConfig = project.container(JunkCodeConfig.class, new JunkCodeConfigFactory())

        android.applicationVariants.all { variant ->
            def variantName = variant.name
            def junkCodeConfig = generateJunkCodeExt.variantConfig.findByName(variantName)
            if (junkCodeConfig) {
                createGenerateJunkCodeTask(project, android, variant, junkCodeConfig)
            }
        }
    }

    private def createGenerateJunkCodeTask = { project, android, variant, junkCodeConfig ->
        def variantName = variant.name
        def generateJunkCodeTaskName = "generate${variantName.capitalize()}JunkCode"
        def dir = new File(project.buildDir, "generated/source/junk/$variantName")
        def resDir = new File(dir, "res")
        def javaDir = new File(dir, "java")
        def manifestFile = new File(dir, "AndroidManifest.xml")
        def packageName = android.namespace//AGP 7.3+
        if (!packageName) {//AGP under 7.3
            //从AndroidManifest.xml找到package name
            for (int i = 0; i < variant.sourceSets.size(); i++) {
                def sourceSet = variant.sourceSets[i]
                if (sourceSet.manifestFile.exists()) {
                    def parser = new XmlParser()
                    def node = parser.parse(sourceSet.manifestFile)
                    packageName = node.attribute("package")
                    if (packageName) {
                        break
                    }
                }
            }
        }
        if (!packageName) {
            packageName = ""
        }
        def generateJunkCodeTask = project.task(generateJunkCodeTaskName, type: AndroidJunkCodeTask) {
            config = junkCodeConfig
            manifestPackageName = packageName
            outDir = dir
        }
        //将自动生成的AndroidManifest.xml加入到一个未被占用的manifest位置(如果都占用了就不合并了，通常较少出现全被占用情况)
        for (int i = variant.sourceSets.size() - 1; i >= 0; i--) {
            def sourceSet = variant.sourceSets[i]
            if (!sourceSet.manifestFile.exists()) {
                sourceSet.manifest.srcFile(manifestFile)
                break
            }
        }
        if (variant.respondsTo("registerGeneratedResFolders")) {
            variant.registerGeneratedResFolders(project
                    .files(resDir)
                    .builtBy(generateJunkCodeTask))
            if (variant.hasProperty("mergeResourcesProvider")) {
                variant.mergeResourcesProvider.configure { dependsOn(generateJunkCodeTask) }
            } else {
                //noinspection GrDeprecatedAPIUsage
                variant.mergeResources.dependsOn(generateJunkCodeTask)
            }
        } else {
            //noinspection GrDeprecatedAPIUsage
            variant.registerResGeneratingTask(generateJunkCodeTask, resDir)
        }
        variant.registerJavaGeneratingTask(generateJunkCodeTask, javaDir)
    }
}