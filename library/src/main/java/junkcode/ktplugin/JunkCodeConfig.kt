package junkcode.ktplugin

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

class JunkCodeConfig {
    @Input
    var packageBase: String = ""

    @Input
    var packageCount: Int = 0

    @Input
    var activityCountPerPackage: Int = 0

    @Input
    var excludeActivityJavaFile: Boolean = false

    @Input
    var otherCountPerPackage: Int = 0

    @Input
    var methodCountPerClass: Int = 0

    @Input
    var resPrefix: String = "junk_"

    @Input
    var drawableCount: Int = 0

    @Input
    var stringCount: Int = 0

    @Internal
    var name: String = ""

    constructor(name: String) {
        this.name = name
    }

}