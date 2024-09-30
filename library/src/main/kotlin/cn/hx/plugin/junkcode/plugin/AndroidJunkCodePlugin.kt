package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.GenerateJunkCodeTask
import cn.hx.plugin.junkcode.task.ManifestMergeTask
import cn.hx.plugin.junkcode.utils.capitalizeCompat
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class AndroidJunkCodePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {
            val androidJunkCodeExt = target.extensions.create("androidJunkCode", AndroidJunkCodeExt::class.java, target.container(JunkCodeConfig::class.java))
            val androidComponents = target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val variantName = variant.name
                val junkCodeConfig = androidJunkCodeExt.variantConfig.findByName(variantName) ?: return@onVariants
                if (androidJunkCodeExt.debug) {
                    println("AndroidJunkCode: generate code for variant $variantName")
                }
                //生成垃圾代码目录
                val junkCodeOutDir = target.layout.buildDirectory.dir("generated/source/junk/${variantName}")
                val generateJunkCodeTaskProvider = target.tasks.register<GenerateJunkCodeTask>("generate${variantName.capitalizeCompat()}JunkCode") {
                    config.set(junkCodeConfig)
                    namespace.set(variant.namespace)
                    outputFolder.set(junkCodeOutDir)
                }
                //java文件
                variant.sources.java?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    target.objects.directoryProperty().value(it.outputFolder.dir("java"))
                }
                //资源文件
                variant.sources.res?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    target.objects.directoryProperty().value(it.outputFolder.dir("res"))
                }
                //AndroidManifest.xml
                val manifestUpdater = target.tasks.register<ManifestMergeTask>("merge" + variantName.capitalizeCompat() + "JunkCodeManifest") {
                    genManifestFile.set(generateJunkCodeTaskProvider.flatMap { it.outputFolder.file("AndroidManifest.xml") })
                }
                variant.artifacts.use(manifestUpdater)
                    .wiredWithFiles({ it.mergedManifest },
                        { it.updatedManifest })
                    .toTransform(SingleArtifact.MERGED_MANIFEST)
                //混淆文件
                variant.proguardFiles.add(generateJunkCodeTaskProvider.flatMap {
                    it.outputFolder.file("proguard-rules.pro")
                })
            }
        }
    }
}