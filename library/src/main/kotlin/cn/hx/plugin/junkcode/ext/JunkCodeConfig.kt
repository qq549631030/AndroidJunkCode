package cn.hx.plugin.junkcode.ext

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import groovy.lang.Tuple2
import groovy.lang.Tuple3
import groovy.lang.Tuple4
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.io.File

class JunkCodeConfig(@Internal val name: String) {
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
    var typeGenerator: Action<TypeSpec.Builder>? = null

    @Internal
    var methodGenerator: Action<MethodSpec.Builder>? = null

    @Internal
    var packageCreator: Action<Tuple2<Int, StringBuilder>>? = null

    @Internal
    var activityCreator: Action<Tuple4<Int, StringBuilder, StringBuilder, StringBuilder>>? = null

    @Internal
    var classNameCreator: Action<Tuple2<Int, StringBuilder>>? = null

    @Internal
    var methodNameCreator: Action<Tuple2<Int, StringBuilder>>? = null

    @Internal
    var drawableCreator: Action<Tuple3<Int, StringBuilder, StringBuilder>>? = null

    @Internal
    var stringCreator: Action<Tuple3<Int, StringBuilder, StringBuilder>>? = null

    @Internal
    var keepCreator: Action<Tuple2<StringBuilder, StringBuilder>>? = null

    @Internal
    var proguardCreator: Action<Tuple2<List<String>, StringBuilder>>? = null

    @Internal
    var javaGenerator: Action<File>? = null

    @Internal
    var resGenerator: Action<File>? = null

    @Internal
    var manifestGenerator: Action<File>? = null
}