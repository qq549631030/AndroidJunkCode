package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.task.AndroidJunkCodeTask
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidJunkCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        if (!android) {
            throw IllegalArgumentException("")
        }
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt)
        android.applicationVariants.all { variant ->
            def variantName = variant.name
            if (variantName in generateJunkCodeExt.variants) {
                def dir = new File(project.buildDir, "generated/source/junk/$variantName")
                def generateJunkCodeTask = project.task("generateJunkCode${variantName.capitalize()}", type: AndroidJunkCodeTask) {
                    packages = generateJunkCodeExt.packages
                    fileCountPerPackage = generateJunkCodeExt.fileCountPerPackage
                    methodCountPerClass = generateJunkCodeExt.methodCountPerClass
                    outDir = dir
                }
                variant.registerJavaGeneratingTask(generateJunkCodeTask, dir)
            }
        }
    }
}