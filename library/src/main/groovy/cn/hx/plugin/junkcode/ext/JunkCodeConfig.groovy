package cn.hx.plugin.junkcode.ext

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Action
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
    Action<TypeSpec.Builder> typeGenerator = null

    @Internal
    Action<MethodSpec.Builder> methodGenerator = null

    @Internal
    Action<StringBuilder> layoutGenerator = null

    @Internal
    Action<StringBuilder> drawableGenerator = null

    @Internal
    String name = ""

    JunkCodeConfig(String name) {
        this.name = name
    }
}