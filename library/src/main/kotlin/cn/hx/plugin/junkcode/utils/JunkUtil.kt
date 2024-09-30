package cn.hx.plugin.junkcode.utils

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.template.ResTemplate
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import groovy.lang.Tuple2
import groovy.lang.Tuple3
import groovy.lang.Tuple4
import java.io.File
import java.nio.file.Files
import java.util.Date
import java.util.Locale
import javax.lang.model.element.Modifier
import kotlin.random.Random

object JunkUtil {
    val random = Random.Default
    val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    val colorLetters = "0123456789abcdef".toCharArray()

    /**
     * 根据下标生成随机小写字母名称
     */
    fun generateName(index: Int): String {
        val sb = StringBuilder()
        for (i in 0..4) {
            sb.append(lowercaseLetters[random.nextInt(lowercaseLetters.size)])
        }
        var temp = index
        while (temp >= 0) {
            sb.append(lowercaseLetters[temp % lowercaseLetters.size])
            temp /= lowercaseLetters.size
            if (temp == 0) {
                temp = -1
            }
        }
        return sb.toString()
    }

    /**
     * 生成随机方法
     */
    private fun generateMethods(methodBuilder: MethodSpec.Builder) {
        when (random.nextInt(5)) {
            0 -> {
                methodBuilder.addStatement("long now = \$T.currentTimeMillis()", System::class.java)
                    .beginControlFlow("if (\$T.currentTimeMillis() < now)", System::class.java)
                    .addStatement(
                        "\$T.out.println(\$S)", System::class.java, "Time travelling, woo hoo!"
                    )
                    .nextControlFlow("else if (\$T.currentTimeMillis() == now)", System::class.java)
                    .addStatement("\$T.out.println(\$S)", System::class.java, "Time stood still!")
                    .nextControlFlow("else").addStatement(
                        "\$T.out.println(\$S)", System::class.java, "Ok, time still moving forward"
                    ).endControlFlow()
            }

            1 -> {
                methodBuilder.addCode("" + "int total = 0;\n" + "for (int i = 0; i < 10; i++) {\n" + "  total += i;\n" + "}\n")
            }

            2 -> {
                methodBuilder.beginControlFlow("try")
                    .addStatement("throw new Exception(\$S)", "Failed")
                    .nextControlFlow("catch (\$T e)", Exception::class.java)
                    .addStatement("throw new \$T(e)", RuntimeException::class.java).endControlFlow()
            }

            3 -> {
                methodBuilder.returns(Date::class.java)
                    .addStatement("return new \$T()", Date::class.java)
            }

            else -> {
                methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String::class.java, "args")
                    .addStatement("\$T.out.println(\$S)", System::class.java, "Hello")
            }
        }
    }

    /**
     * 生成随机颜色
     */
    private fun generateColor(): String {
        val sb = StringBuilder()
        sb.append("#")
        for (i in 0..5) {
            sb.append(colorLetters[random.nextInt(colorLetters.size)])
        }
        return sb.toString()
    }

    /**
     * 生成随机颜色
     */
    private fun generateId(): String {
        val sb = StringBuilder()
        for (i in 0..4) {
            sb.append(lowercaseLetters[random.nextInt(lowercaseLetters.size)])
        }
        return sb.toString()
    }

    /**
     * 生成Activity
     */
    fun generateActivity(javaDir: File, resDir: File, namespace: String, packageName: String, config: JunkCodeConfig): List<String> {
        val activityList = mutableListOf<String>()
        for (i in 0 until config.activityCountPerPackage) {
            var className = ""
            var layoutName = ""
            config.activityCreator?.let {
                val activityNameBuilder = StringBuilder()
                val layoutNameBuilder = StringBuilder()
                val layoutContentBuilder = StringBuilder()
                it.execute(Tuple4(i, activityNameBuilder, layoutNameBuilder, layoutContentBuilder))
                className = activityNameBuilder.toString()
                layoutName = layoutNameBuilder.toString()
                writeStringToFile(File(resDir, "layout/${layoutName}.xml"), layoutContentBuilder.toString())
            } ?: run {
                val activityPreName = generateName(i)
                className = activityPreName.capitalizeCompat() + "Activity"
                layoutName = "${config.resPrefix.lowercase(Locale.getDefault())}${packageName.replace(".", "_")}_activity_${activityPreName}"
                generateLayout(resDir, layoutName)
            }
            if (!config.excludeActivityJavaFile) {
                val typeBuilder = TypeSpec.classBuilder(className)
                typeBuilder.superclass(ClassName.get("android.app", "Activity"))
                typeBuilder.addModifiers(Modifier.PUBLIC)
                //onCreate方法
                val bundleClassName = ClassName.get("android.os", "Bundle")
                typeBuilder.addMethod(
                    MethodSpec.methodBuilder("onCreate")
                        .addAnnotation(
                            Override::class.java
                        )
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(bundleClassName, "savedInstanceState")
                        .addStatement("super.onCreate(savedInstanceState)")
                        .addStatement("setContentView(\$T.layout.${layoutName})", ClassName.get(namespace, "R"))
                        .build()
                )
                //其它方法
                config.typeGenerator?.execute(typeBuilder) ?: run {
                    for (j in 0 until config.methodCountPerClass) {
                        val methodName = config.methodNameCreator?.let {
                            StringBuilder().apply {
                                it.execute(Tuple2(j, this))
                            }.toString()
                        } ?: generateName(j)
                        val methodBuilder = MethodSpec.methodBuilder(methodName)
                        config.methodGenerator?.execute(methodBuilder) ?: run {
                            generateMethods(methodBuilder)
                        }
                        typeBuilder.addMethod(methodBuilder.build())
                    }
                }
                val javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
                writeJavaToFile(javaDir, javaFile)
                activityList.add("$packageName.$className")
            }
        }
        return activityList
    }

    /**
     * 生成java文件
     */
    fun generateJava(javaDir: File, packageName: String, config: JunkCodeConfig) {
        for (i in 0 until config.otherCountPerPackage) {
            val className = config.classNameCreator?.let {
                StringBuilder().apply {
                    it.execute(Tuple2(i, this))
                }.toString()
            } ?: generateName(i).capitalizeCompat()
            val typeBuilder = TypeSpec.classBuilder(className)
            config.typeGenerator?.execute(typeBuilder) ?: run {
                typeBuilder.addModifiers(Modifier.PUBLIC)
                for (j in 0 until config.methodCountPerClass) {
                    val methodName = config.methodNameCreator?.let {
                        StringBuilder().apply {
                            it.execute(Tuple2(j, this))
                        }.toString()
                    } ?: generateName(j)
                    val methodBuilder = MethodSpec.methodBuilder(methodName)
                    config.methodGenerator?.execute(methodBuilder) ?: generateMethods(methodBuilder)
                    typeBuilder.addMethod(methodBuilder.build())
                }
            }
            val javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
            writeJavaToFile(javaDir, javaFile)
        }
    }

    /**
     * 生成layout文件
     */
    fun generateLayout(resDir: File, layoutName: String) {
        val layoutFile = File(resDir, "layout/${layoutName}.xml")
        val layoutStr = String.format(ResTemplate.LAYOUT_TEMPLATE, generateId())
        writeStringToFile(layoutFile, layoutStr)
    }

    /**
     * 生成drawable
     */
    fun generateDrawableFiles(resDir: File, config: JunkCodeConfig) {
        config.drawableCreator?.let {
            val fileNameBuilder = StringBuilder()
            val contentBuilder = StringBuilder()
            for (i in 0 until config.drawableCount) {
                fileNameBuilder.setLength(0)
                contentBuilder.setLength(0)
                it.execute(Tuple3(i, fileNameBuilder, contentBuilder))
                val drawableName = fileNameBuilder.toString()
                val drawableFile = File(resDir, "drawable/${drawableName}.xml")
                val drawableStr = contentBuilder.toString()
                writeStringToFile(drawableFile, drawableStr)
            }
        } ?: run {
            for (i in 0 until config.drawableCount) {
                val drawableName = "${config.resPrefix.lowercase(Locale.getDefault())}${generateName(i)}"
                val drawableFile = File(resDir, "drawable/${drawableName}.xml")
                val drawableStr = String.format(ResTemplate.DRAWABLE, generateColor())
                writeStringToFile(drawableFile, drawableStr)
            }
        }
    }

    /**
     * 生成strings.xml
     */
    fun generateStringsFile(resDir: File, config: JunkCodeConfig) {
        val stringFile = File(resDir, "values/strings.xml")
        val contentBuilder = StringBuilder()
        val keyBuilder = StringBuilder()
        val valueBuilder = StringBuilder()
        contentBuilder.append("<resources>\n")
        for (i in 0 until config.stringCount) {
            config.stringCreator?.let {
                keyBuilder.setLength(0)
                valueBuilder.setLength(0)
                it.execute(Tuple3(i, keyBuilder, valueBuilder))
                val key = keyBuilder.toString()
                val value = valueBuilder.toString()
                contentBuilder.append("<string name=\"${key}\">${value}</string>\n")
            } ?: run {
                val key = "${config.resPrefix.lowercase(Locale.getDefault())}${generateName(i)}"
                val value = generateName(i)
                contentBuilder.append("<string name=\"${key}\">${value}</string>\n")
            }
        }
        contentBuilder.append("</resources>\n")
        writeStringToFile(stringFile, contentBuilder.toString())
    }

    /**
     * 生成keep.xml
     */
    fun generateKeep(resDir: File, config: JunkCodeConfig) {
        config.keepCreator?.let {
            val fileNameBuilder = StringBuilder()
            val contentBuilder = StringBuilder()
            it.execute(Tuple2(fileNameBuilder, contentBuilder))
            val keepName = fileNameBuilder.toString()
            val keepFile = File(resDir, "raw/${keepName}.xml")
            val keepContent = contentBuilder.toString()
            writeStringToFile(keepFile, keepContent)
        } ?: run {
            if (config.resPrefix.isEmpty()) {
                return
            }
            val keepName = "android_junk_code_keep"
            val keepFile = File(resDir, "raw/${keepName}.xml")
            val keepContent = "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                    "    tools:keep=\"@layout/${config.resPrefix}*, @drawable/${config.resPrefix}*\" />\n"
            writeStringToFile(keepFile, keepContent)
        }
    }

    /**
     * 生成AndroidManifest.xml
     */
    fun generateManifest(manifestFile: File, activityList: List<String>) {
        val sb = StringBuilder()
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
        sb.append("    <application>\n")
        for (i in activityList.indices) {
            sb.append("        <activity android:name=\"${activityList[i]}\"/>\n")
        }
        sb.append("    </application>\n")
        sb.append("</manifest>")
        writeStringToFile(manifestFile, sb.toString())
    }

    /**
     * 生成proguard-rules.pro
     */
    fun generateProguard(proguardFile: File, packageList: List<String>, config: JunkCodeConfig) {
        val sb = StringBuilder()
        config.proguardCreator?.execute(Tuple2(packageList, sb)) ?: run {
            for (i in packageList.indices) {
                sb.append("-keep class ${packageList[i]}.**{*;}\n")
            }
        }
        writeStringToFile(proguardFile, sb.toString())
    }

    /**
     * java写入文件
     */
    private fun writeJavaToFile(javaDir: File, javaFile: JavaFile) {
        var outputDirectory = javaDir.toPath()
        if (javaFile.packageName.isNotEmpty()) {
            for (packageComponent in javaFile.packageName.split(".")) {
                outputDirectory = outputDirectory.resolve(packageComponent)
            }
            Files.createDirectories(outputDirectory);
        }
        val outputPath = outputDirectory.resolve(javaFile.typeSpec.name + ".java")
        writeStringToFile(outputPath.toFile(), javaFile.toString())
    }


    /**
     * 字符串写入文件
     */
    private fun writeStringToFile(file: File, data: String) {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        try {
            file.writeText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}