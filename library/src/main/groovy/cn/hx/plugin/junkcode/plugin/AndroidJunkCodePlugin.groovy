package cn.hx.plugin.junkcode.plugin


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
        } else {
            new OldVariantApiPlugin().apply(project)
        }
    }
}