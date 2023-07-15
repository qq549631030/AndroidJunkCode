package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.ManifestMergeTask
import cn.hx.plugin.junkcode.task.GenerateJunkCodeTask
import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class NewVariantApiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.findByName("android")
        def androidComponents = project.extensions.findByName("androidComponents")
        def generateJunkCodeExt = project.extensions.create("androidJunkCode", AndroidJunkCodeExt, project.container(JunkCodeConfig))
        androidComponents.onVariants(androidComponents.selector().all(), { variant ->
            def variantName = variant.name
            def junkCodeConfig = generateJunkCodeExt.variantConfig.findByName(variantName)
            if (generateJunkCodeExt.debug) {
                println("AndroidJunkCode: generate code for variant $variantName? ${junkCodeConfig != null}")
            }
            if (junkCodeConfig) {
                def junkCodeOutDir = new File(project.buildDir, "generated/source/junk/${variantName}")
                def generateJunkCodeTaskProvider = project.tasks.register("generate${variantName.capitalize()}JunkCode", GenerateJunkCodeTask) {
                    config = junkCodeConfig
                    namespace = android.namespace
                    javaOutputFolder.set(new File(junkCodeOutDir, "java"))
                    resOutputFolder.set(new File(junkCodeOutDir, "res"))
                    manifestOutputFile.set(new File(junkCodeOutDir, "AndroidManifest.xml"))
                }
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
                TaskProvider manifestUpdater = project.tasks.register('merge' + variantName.capitalize() + 'JunkCodeManifest', ManifestMergeTask) {
                    it.genManifestFile.set(generateJunkCodeTaskProvider.flatMap {
                        it.manifestOutputFile
                    })
                }
                variant.artifacts.use(manifestUpdater)
                        .wiredWithFiles({ it.mergedManifest },
                                { it.updatedManifest })
                        .toTransform(SingleArtifact.MERGED_MANIFEST.INSTANCE)
            }
        })
    }
}