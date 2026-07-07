package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.GenerateJunkCodeTask
import cn.hx.plugin.junkcode.task.ManifestMergeTask
import cn.hx.plugin.junkcode.utils.capitalizeCompat
import com.android.build.api.AndroidPluginVersion
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@Suppress("UnstableApiUsage")
class AndroidJunkCodePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val androidJunkCodeExt = extensions.create(
                "androidJunkCode",
                AndroidJunkCodeExt::class.java,
                objects.domainObjectContainer(JunkCodeConfig::class.java)
            )
            val androidComponents =
                extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val variantName = variant.name
                val junkCodeConfig =
                    androidJunkCodeExt.variantConfig.findByName(variantName)
                        ?: variant.getExtension(JunkCodeConfig::class.java)
                        ?: return@onVariants
                if (androidJunkCodeExt.debug) {
                    println("AndroidJunkCode: generate code for variant $variantName")
                }
                //生成垃圾代码目录
                val junkCodeOutDir =
                    layout.buildDirectory.dir("generated/source/junk/${variantName}")
                val generateJunkCodeTaskProvider =
                    tasks.register(
                        "generate${variantName.capitalizeCompat()}JunkCode",
                        GenerateJunkCodeTask::class.java
                    ) { task ->
                        task.config.set(junkCodeConfig)
                        task.namespace.set(variant.namespace)
                        task.javaOutputDir.set(junkCodeOutDir.map { it.dir("java") })
                        task.resOutputDir.set(junkCodeOutDir.map { it.dir("res") })
                        task.manifestOutputFile.set(junkCodeOutDir.map { it.file("AndroidManifest.xml") })
                        task.proguardOutputFile.set(junkCodeOutDir.map { it.file("proguard-rules.pro") })
                    }
                //java文件
                variant.sources.java?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    it.javaOutputDir
                }
                //资源文件
                variant.sources.res?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    it.resOutputDir
                }
                //AndroidManifest.xml
                val pluginVersion = androidComponents.pluginVersion
                val useSourcesManifestsApi = pluginVersion >= AndroidPluginVersion(8, 6, 0)
                // AGP 8.6.0以后用新API
                if (useSourcesManifestsApi &&
                    variant.registerGeneratedManifestSourceIfAvailable(generateJunkCodeTaskProvider)
                ) {
                    if (androidJunkCodeExt.debug) {
                        println("AndroidJunkCode: use variant.sources.manifests.addGeneratedManifestFile for $variantName")
                    }
                } else {
                    if (androidJunkCodeExt.debug) {
                        println("AndroidJunkCode: use ManifestMergeTask.genManifestFile for $variantName")
                    }
                    //  (AGP < 8.6.0 fallback)
                    val manifestUpdater =
                        tasks.register(
                            "merge" + variantName.capitalizeCompat() + "JunkCodeManifest",
                            ManifestMergeTask::class.java
                        ) { task ->
                            task.genManifestFile.set(generateJunkCodeTaskProvider.flatMap {
                                it.manifestOutputFile
                            })
                        }
                    variant.artifacts.use(manifestUpdater)
                        .wiredWithFiles(
                            { it.mergedManifest },
                            { it.updatedManifest })
                        .toTransform(SingleArtifact.MERGED_MANIFEST)
                }
                //混淆文件
                variant.proguardFiles.add(generateJunkCodeTaskProvider.flatMap {
                    it.proguardOutputFile
                })
            }
        }
    }

    private fun Variant.registerGeneratedManifestSourceIfAvailable(
        generateTask: TaskProvider<GenerateJunkCodeTask>
    ): Boolean {
        return try {
            val manifests = sources.javaClass.getMethod("getManifests").invoke(sources)
            val addGenerated = manifests.javaClass.methods.firstOrNull {
                it.name == "addGeneratedManifestFile" && it.parameterCount == 2
            } ?: return false
            addGenerated.invoke(
                manifests,
                generateTask,
                { task: GenerateJunkCodeTask ->
                    task.manifestOutputFile
                }
            )
            true
        } catch (_: Throwable) {
            false
        }
    }
}
