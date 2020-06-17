package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.AndroidJunkCodeTask
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidJunkCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        if (!android) {
            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
        }
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt)
        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                def variantName = variant.name
                Closure<JunkCodeConfig> junkCodeConfig = generateJunkCodeExt.configMap[variantName]
                if (junkCodeConfig) {
                    def dir = new File(project.buildDir, "generated/source/junk/$variantName")
                    String packageName = findPackageName(variant)
                    def generateJunkCodeTask = project.task("generateJunkCode${variantName.capitalize()}", type: AndroidJunkCodeTask) {
                        junkCodeConfig.delegate = config
                        junkCodeConfig.resolveStrategy = DELEGATE_FIRST
                        junkCodeConfig.call()
                        manifestPackageName = packageName
                        outDir = dir
                    }
                    //将自动生成的AndroidManifest.xml加入到一个未被占用的manifest位置(如果都占用了就不合并了，通常较少出现全被占用情况)
                    for (int i = variant.sourceSets.size() - 1; i >= 0; i--) {
                        def sourceSet = variant.sourceSets[i]
                        if (!sourceSet.manifestFile.exists()) {
                            android.sourceSets."${sourceSet.name}".manifest.srcFile(new File(dir, "AndroidManifest.xml").absolutePath)
                            break
                        }
                    }
                    android.sourceSets."${variantName}".res.srcDir(new File(dir, "res"))
                    variant.registerJavaGeneratingTask(generateJunkCodeTask, new File(dir, "java"))
                    //在执行generateBuildConfig之前执行generateJunkCodeTask
                    variant.generateBuildConfigProvider.get().dependsOn(generateJunkCodeTask)
                }
            }
        }
    }


    /**
     * 从AndroidManifest.xml找到package name
     * @param variant
     * @return
     */
    static String findPackageName(ApplicationVariant variant) {
        String packageName = null
        for (int i = 0; i < variant.sourceSets.size(); i++) {
            def sourceSet = variant.sourceSets[i]
            if (sourceSet.manifestFile.exists()) {
                def parser = new XmlParser()
                Node node = parser.parse(sourceSet.manifestFile)
                packageName = node.attribute("package")
                if (packageName != null) {
                    break
                }
            }
        }
        return packageName
    }
}