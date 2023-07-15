package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.template.ResTemplate
import cn.hx.plugin.junkcode.utils.JunkUtil
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

import javax.lang.model.element.Modifier
import java.nio.file.Files
import java.nio.file.Path

abstract class GenerateJunkCodeTask extends DefaultTask {

    @Nested
    abstract JunkCodeConfig config

    @Input
    String namespace

    @OutputDirectory
    abstract DirectoryProperty getJavaOutputFolder()

    @OutputDirectory
    abstract DirectoryProperty getResOutputFolder()

    @OutputFile
    abstract RegularFileProperty getManifestOutputFile()

    @Internal
    List<String> activityList = new ArrayList<>()

    @TaskAction
    void taskAction() {
        getJavaOutputFolder().get().asFile.deleteDir()
        getResOutputFolder().get().asFile.deleteDir()
        for (int i = 0; i < config.packageCount; i++) {
            String packageName
            if (config.packageBase.isEmpty()) {
                packageName = JunkUtil.generateName(i)
            } else {
                packageName = config.packageBase + "." + JunkUtil.generateName(i)
            }
            generateActivity(packageName)
            generateOtherClass(packageName)
        }
        generateManifest()
        generateDrawable()
        generateStringsFile()
        generateKeep()
    }

    private void generateActivity(String packageName) {
        for (int i = 0; i < config.activityCountPerPackage; i++) {
            def activityPreName = JunkUtil.generateName(i)
            def className = activityPreName.capitalize() + "Activity"
            def layoutName = "${config.resPrefix.toLowerCase()}${packageName.replace(".", "_")}_activity_${activityPreName}"
            generateLayout(layoutName)
            if (!config.excludeActivityJavaFile) {
                def typeBuilder = TypeSpec.classBuilder(className)
                typeBuilder.superclass(ClassName.get("android.app", "Activity"))
                typeBuilder.addModifiers(Modifier.PUBLIC)
                //onCreate方法
                def bundleClassName = ClassName.get("android.os", "Bundle")
                typeBuilder.addMethod(MethodSpec.methodBuilder("onCreate")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(bundleClassName, "savedInstanceState")
                        .addStatement("super.onCreate(savedInstanceState)")
                        .addStatement("setContentView(\$T.layout.${layoutName})", ClassName.get(namespace, "R"))
                        .build())
                if (config.typeGenerator) {
                    config.typeGenerator.execute(typeBuilder)
                } else {
                    //其它方法
                    for (int j = 0; j < config.methodCountPerClass; j++) {
                        def methodName = JunkUtil.generateName(j)
                        def methodBuilder = MethodSpec.methodBuilder(methodName)
                        if (config.methodGenerator) {
                            config.methodGenerator.execute(methodBuilder)
                        } else {
                            JunkUtil.generateMethods(methodBuilder)
                        }
                        typeBuilder.addMethod(methodBuilder.build())
                    }
                }
                def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
                writeJavaFile(javaFile)
                activityList.add(packageName + "." + className)
            }
        }
    }


    /**
     * 生成Manifest
     */
    private void generateManifest() {
        def manifestFile = getManifestOutputFile().get().asFile
        StringBuilder sb = new StringBuilder()
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
        sb.append("    <application>\n")
        for (i in 0..<activityList.size()) {
            sb.append("        <activity android:name=\"${activityList.get(i)}\"/>\n")
        }
        sb.append("    </application>\n")
        sb.append("</manifest>")
        JunkUtil.writeStringToFile(manifestFile, sb.toString())
    }

    private void generateOtherClass(String packageName) {
        for (int j = 0; j < config.otherCountPerPackage; j++) {
            def className = JunkUtil.generateName(j).capitalize()
            def typeBuilder = TypeSpec.classBuilder(className)
            if (config.typeGenerator) {
                config.typeGenerator.execute(typeBuilder)
            } else {
                typeBuilder.addModifiers(Modifier.PUBLIC)
                for (int k = 0; k < config.methodCountPerClass; k++) {
                    def methodName = JunkUtil.generateName(k)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
                    if (config.methodGenerator) {
                        config.methodGenerator.execute(methodBuilder)
                    } else {
                        JunkUtil.generateMethods(methodBuilder)
                    }
                    typeBuilder.addMethod(methodBuilder.build())
                }
            }
            def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
            writeJavaFile(javaFile)
        }
    }

    /**
     * 生成layout
     * @param layoutName
     */
    private void generateLayout(String layoutName) {
        def layoutFile = new File(getResOutputFolder().get().asFile, "layout/${layoutName}.xml")
        if (config.layoutGenerator) {
            def builder = new StringBuilder()
            config.layoutGenerator.execute(builder)
            JunkUtil.writeStringToFile(layoutFile, builder.toString())
        } else {
            def layoutStr = String.format(ResTemplate.LAYOUT_TEMPLATE, JunkUtil.generateId())
            JunkUtil.writeStringToFile(layoutFile, layoutStr)
        }
    }


    /**
     * 生成drawable
     * @param drawableName
     */
    void generateDrawable() {
        for (int i = 0; i < config.drawableCount; i++) {
            def drawableName = "${config.resPrefix.toLowerCase()}${JunkUtil.generateName(i)}"
            def drawableFile = new File(getResOutputFolder().get().asFile, "drawable/${drawableName}.xml")
            if (config.drawableGenerator) {
                def builder = new StringBuilder()
                config.drawableGenerator.execute(builder)
                JunkUtil.writeStringToFile(drawableFile, builder.toString())
            } else {
                def drawableStr = String.format(ResTemplate.DRAWABLE, JunkUtil.generateColor())
                JunkUtil.writeStringToFile(drawableFile, drawableStr)
            }
        }
    }

    /**
     * 生成strings.xml
     */
    void generateStringsFile() {
        List<String> stringList = new ArrayList<>()
        for (int i = 0; i < config.stringCount; i++) {
            stringList.add("${config.resPrefix.toLowerCase()}${JunkUtil.generateName(i)}")
        }
        def stringFile = new File(getResOutputFolder().get().asFile, "values/strings.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<resources>\n")
        for (i in 0..<stringList.size()) {
            sb.append("<string name=\"${stringList.get(i)}\">${stringList.get(i)}</string>\n")
        }
        sb.append("</resources>\n")
        JunkUtil.writeStringToFile(stringFile, sb.toString())
    }

    private void generateKeep() {
        def keepFile = new File(getResOutputFolder().get().asFile, "raw/android_junk_code_keep.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<resources xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                "    tools:keep=\"@layout/${config.resPrefix}*, @drawable/${config.resPrefix}*\" />\n")
        JunkUtil.writeStringToFile(keepFile, sb.toString())
    }

    private void writeJavaFile(JavaFile javaFile) {
        def outputDirectory = new File(getJavaOutputFolder().get().asFile, "java").toPath()
        if (!javaFile.packageName.isEmpty()) {
            for (String packageComponent : javaFile.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }
        Path outputPath = outputDirectory.resolve(javaFile.typeSpec.name + ".java");
        JunkUtil.writeStringToFile(outputPath.toFile(), javaFile.toString())
    }
}