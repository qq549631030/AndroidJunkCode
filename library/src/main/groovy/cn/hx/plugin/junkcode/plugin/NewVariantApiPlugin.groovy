package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.GenerateJunkCodeTask
import cn.hx.plugin.junkcode.task.ManifestMergeTask
import cn.hx.plugin.junkcode.utils.JunkUtil
import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * AGP 7.0.0+
 */
class NewVariantApiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def androidComponents = project.extensions.findByName("androidComponents")
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt, project.container(JunkCodeConfig))
        androidComponents.onVariants(androidComponents.selector().all(), { variant ->
            def variantName = variant.name
            def junkCodeConfig = generateJunkCodeExt.variantConfig.findByName(variantName)
            if (generateJunkCodeExt.debug) {
                println("AndroidJunkCode: generate code for variant $variantName? ${junkCodeConfig != null}")
            }
            if (junkCodeConfig) {
                def junkCodeOutDir = project.layout.buildDirectory.dir("generated/source/junk/${variantName}")
                def generateJunkCodeTaskProvider = project.tasks.register("generate${variantName.capitalize()}JunkCode", GenerateJunkCodeTask) {
                    config = junkCodeConfig
                    namespace = variant.namespace
                    javaOutputFolder.set(junkCodeOutDir.map { it.dir("java") })
                    resOutputFolder.set(junkCodeOutDir.map { it.dir("res") })
                    manifestOutputFile.set(junkCodeOutDir.map { it.file("AndroidManifest.xml") })
                    proguardOutputFile.set(junkCodeOutDir.map { it.file("proguard-rules.pro") })
                }
                if (JunkUtil.isAGP7_4_0(project)) {
                    if (variant.sources.java) {
                        variant.sources.java.addGeneratedSourceDirectory(generateJunkCodeTaskProvider, {
                            it.javaOutputFolder
                        })
                    }
                    if (variant.sources.res) {
                        variant.sources.res.addGeneratedSourceDirectory(generateJunkCodeTaskProvider, {
                            it.resOutputFolder
                        })
                    }
                }
                TaskProvider manifestUpdater = project.tasks.register('merge' + variantName.capitalize() + 'JunkCodeManifest', ManifestMergeTask) {
                    it.genManifestFile.set(generateJunkCodeTaskProvider.flatMap {
                        it.manifestOutputFile
                    })
                }
                variant.artifacts.use(manifestUpdater)
                        .wiredWithFiles({ it.mergedManifest },
                                { it.updatedManifest })
                        .toTransform(SingleArtifact.MERGED_MANIFEST.INSTANCE)
                variant.proguardFiles.add(generateJunkCodeTaskProvider.flatMap {
                    it.proguardOutputFile
                })
            }
        })
        if (!JunkUtil.isAGP7_4_0(project)) {
            def android = project.extensions.findByName("android")
            android.applicationVariants.all { variant ->
                def variantName = variant.name
                def junkCodeConfig = generateJunkCodeExt.variantConfig.findByName(variantName)
                if (junkCodeConfig) {
                    def generateJunkCodeTaskProvider = project.tasks.named("generate${variantName.capitalize()}JunkCode", GenerateJunkCodeTask)
                    variant.registerJavaGeneratingTask(generateJunkCodeTaskProvider, generateJunkCodeTaskProvider.get().javaOutputFolder.get().asFile)

                    variant.registerGeneratedResFolders(project.files(generateJunkCodeTaskProvider.map {
                        it.resOutputFolder.asFile
                    }).builtBy(generateJunkCodeTaskProvider))
                    variant.mergeResourcesProvider.configure { dependsOn(generateJunkCodeTaskProvider.get()) }
                }
            }
        }
    }
}