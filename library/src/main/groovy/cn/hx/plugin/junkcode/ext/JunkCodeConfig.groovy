package cn.hx.plugin.junkcode.ext

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

class JunkCodeConfig {
    @Input
    String packageBase = ""
    @Input
    int packageCount = 0
    @Input
    int activityCountPerPackage = 0
    @Input
    boolean excludeActivityJavaFile = false
    @Input
    int otherCountPerPackage = 0
    @Input
    int methodCountPerClass = 0
    @Input
    String resPrefix = "junk_"
    @Input
    int drawableCount = 0
    @Input
    int stringCount = 0

    @Internal
    String name = ""

    JunkCodeConfig(String name) {
        this.name = name
    }
}