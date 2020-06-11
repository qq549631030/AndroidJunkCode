package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
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
        android.applicationVariants.all { variant ->
            def variantName = variant.name
            if (variantName in generateJunkCodeExt.variants) {
                def dir = new File(project.buildDir, "generated/source/junk/$variantName")
                String packageName = findPackageName(variant)
                def generateJunkCodeTask = project.task("generateJunkCode${variantName.capitalize()}", type: AndroidJunkCodeTask) {
                    manifestPackageName = packageName
                    packages = generateJunkCodeExt.packages
                    activityCountPerPackage = generateJunkCodeExt.activityCountPerPackage
                    otherCountPerPackage = generateJunkCodeExt.otherCountPerPackage
                    methodCountPerClass = generateJunkCodeExt.methodCountPerClass
                    resPrefix = generateJunkCodeExt.resPrefix
                    drawableCount = generateJunkCodeExt.drawableCount
                    stringCount = generateJunkCodeExt.stringCount
                    outDir = dir
                }
                //将自动生成的AndroidManifest.xml加入到一个未被占用的manifest位置(如果都占用了就不合并了，通常较少出现全被占用情况)
                for (int i = 0; i < variant.sourceSets.size(); i++) {
                    def sourceSet = variant.sourceSets[i]
                    if (!sourceSet.manifestFile.exists()) {
                        android.sourceSets."${sourceSet.name}".manifest.srcFile(new File(dir, "AndroidManifest.xml").absolutePath)
                        break
                    }
                }
                android.sourceSets."main".res.srcDir(new File(dir, "res"))
                variant.registerJavaGeneratingTask(generateJunkCodeTask, dir)
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