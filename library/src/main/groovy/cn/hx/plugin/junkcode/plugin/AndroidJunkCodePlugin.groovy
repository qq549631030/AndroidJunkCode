package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.utils.JunkUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidJunkCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.findByName("android")
        if (!android || !android.hasProperty("applicationVariants")) {
            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
        }
        //AGP 7.0.0+
        if (JunkUtil.isAGP7_0_0(project)) {
            new NewVariantApiPlugin().apply(project)
        } else {
            new OldVariantApiPlugin().apply(project)
        }
    }
}