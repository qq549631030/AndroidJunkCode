package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.template.ResTemplate
import com.squareup.kotlinpoet.FileSpec
import junkcode.ktplugin.JunkCodeConfig
import junkcode.ktplugin.OPTaskJavaDelegate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

class AndroidJunkCodeTask extends DefaultTask {

    static def random = new Random()
    static def random2 = new Random()
    static def random3 = new Random()

    static abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    static color = "0123456789abcdef".toCharArray()

    @Nested
    JunkCodeConfig config

    @Input
    String namespace

    @OutputDirectory
    File outDir

    private static List<String> activityList = new ArrayList<>()
    private List<String> stringList = new ArrayList<>()

    @TaskAction
    void generateJunkCode() {
        if (outDir.exists()) {
            outDir.deleteDir()
        }
        activityList.clear()
        stringList.clear()
        //通过成类
        generateClasses()
        //生成资源
        generateRes()
    }

    /**
     * 生成 java 或者 kotlin 代码和 AndroidManifest.xml */
    void generateClasses() {
        for (int i = 0; i < random2.nextInt(config.packageCount) + config.packageCount / 2; i++) {
            config.packageBase.split(',').each { packageBaseName ->
                String packageName
                if (packageBaseName.isEmpty()) {
                    packageName = generateName(i).trim()
                } else {
                    packageName = packageBaseName + "." + generateName(i).trim()
                }
                //生成Activity
                def activityRandom = new Random()
                for (int j = 0; j < activityRandom.nextInt(config.activityCountPerPackage) + config.activityCountPerPackage / 2; j++) {
                    def activityPreName = generateName(j)
                    def layoutName = "${config.resPrefix.toLowerCase()}${packageName.replace(".", "_")}_activity_${activityPreName}".toLowerCase()
                    generateLayout(layoutName)
                    if (j / 2 == 0) {
                        generateActivity(layoutName, packageName, activityPreName)
                    } else {
                        def kotlinFile = OPTaskJavaDelegate.generateKTActivity(namespace, config, packageName, activityPreName, layoutName)
                        writeKotlinFile(kotlinFile)
                        def name = packageName + "." + activityPreName.capitalize() + "Activity"
                        activityList.add(name)
                    }
                }
                //生成其它类
                def classRandom = new Random()
                for (int j = 0; j < classRandom.nextInt(config.otherCountPerPackage) + config.otherCountPerPackage / 2; j++) {
                    def className = generateName(j).capitalize()
                    def typeBuilder = TypeSpec.classBuilder(className)
                    typeBuilder.addModifiers(Modifier.PUBLIC)
                    def methodRandom = new Random()
                    for (int k = 0; k < methodRandom.nextInt(config.methodCountPerClass) + config.methodCountPerClass / 2; k++) {
                        def methodName = generateName(k)
                        def methodBuilder = MethodSpec.methodBuilder(methodName)
                        generateMethods(methodBuilder)
                        typeBuilder.addMethod(methodBuilder.build())
                    }
                    def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
                    writeJavaFile(javaFile)
                }
            }

        }
        //所有Activity生成完了
        generateManifest()
    }

    /**
     * 生成随机方法
     * @param methodBuilder
     */
    static void generateMethods(MethodSpec.Builder methodBuilder) {
        def ranCase = 0
        if (random2.nextInt(2) == 0) {
            ranCase = random.nextInt(5)
        } else {
            ranCase = random3.nextInt(5)
        }
        switch (ranCase) {
            case 0:
                methodBuilder.addStatement("long now = \$T.currentTimeMillis()", System.class)
                        .beginControlFlow("if (\$T.currentTimeMillis() < now)", System.class)
                        .addStatement("\$T.out.println(\$S)", System.class, "Time travelling, woo hoo!")
                        .nextControlFlow("else if (\$T.currentTimeMillis() == now)", System.class)
                        .addStatement("\$T.out.println(\$S)", System.class, "Time stood still!")
                        .nextControlFlow("else")
                        .addStatement("\$T.out.println(\$S)", System.class, "Ok, time still moving forward")
                        .endControlFlow()
                break
            case 1:
                methodBuilder.addCode("" + "int total = 0;\n" + "for (int i = 0; i < 10; i++) {\n" + "  total += i;\n" + "}\n")
                break
            case 2:
                methodBuilder.beginControlFlow("try")
                        .addStatement("throw new Exception(\$S)", "Failed")
                        .nextControlFlow("catch (\$T e)", Exception.class)
                        .addStatement("throw new \$T(e)", RuntimeException.class)
                        .endControlFlow()
                break
            case 3:
                methodBuilder.returns(Date.class)
                        .addStatement("return new \$T()", Date.class)
                break
            case 4:
                methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("\$T.out.println(\$S)", System.class, "Hello")
                break
            default:
                methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("\$T.out.println(\$S)", System.class, "Hello")
        }
    }

    /**
     * 生成 Activity
     * @param packageName
     * @param activityPreName
     */
    void generateActivity(String layoutName, String packageName, String activityPreName) {
        def className = activityPreName.capitalize() + "Activity"
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
            //其它方法
            if (random2.nextInt(2) == 0) {
                for (int j = 0; j < random3.nextInt(config.methodCountPerClass); j++) {
                    def methodName = generateName(j)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
                    generateMethods(methodBuilder)
                    typeBuilder.addMethod(methodBuilder.build())
                }
            } else {
                for (int j = 0; j < random.nextInt(config.methodCountPerClass); j++) {
                    def methodName = generateName(j)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
                    generateMethods(methodBuilder)
                    typeBuilder.addMethod(methodBuilder.build())
                }
            }

            def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
            writeJavaFile(javaFile)
        }
        activityList.add(packageName + "." + className)
    }

    /**
     * 生成资源文件*/
    void generateRes() {
        //生成drawable
        for (int i = 0; i < random2.nextInt(config.drawableCount) + config.drawableCount / 2; i++) {
            def drawableName = "${config.resPrefix.toLowerCase()}${generateName(i).toLowerCase()}"
            generateDrawable(drawableName)
        }
        //生成string
        for (int i = 0; i < random3.nextInt(config.stringCount) + config.stringCount / 2; i++) {
            stringList.add("${config.resPrefix.toLowerCase()}${generateName(i)}")
        }
        generateStringsFile()
        generateKeep()
    }

    /**
     * 生成 drawable
     * @param drawable
     */
    void generateDrawable(String drawableName) {
        def drawableFile = new File(outDir, "res/drawable/${drawableName}.xml")
        def drawableStr = String.format(ResTemplate.DRAWABLE, generateColor(), generateColor(), generateColor(), generateColor())
        writeStringToFile(drawableFile, drawableStr)
    }


    /**
     * 生成 layout
     * @param layoutName
     */
    void generateLayout(String layoutName) {
        def layoutFile = new File(outDir, "res/layout/${layoutName}.xml")
        if (random3.nextInt(2) == 0) {
            def layoutStr = String.format(ResTemplate.LAYOUT_TEMPLATE, generateId())
            writeStringToFile(layoutFile, layoutStr)
        } else {
            def layoutStr = String.format(ResTemplate.LAYOUT_TEMPLATE2, generateId())
            writeStringToFile(layoutFile, layoutStr)
        }
    }


    /**
     * 生成 Manifest 和没有引用的 service*/
    void generateManifest() {
        def manifestFile = new File(outDir, "AndroidManifest.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
        sb.append("    <application>\n")
        for (i in 0..<activityList.size()) {
            sb.append("<activity android:name=\"${activityList.get(i)}\" " + "android:exported=\"true\"  " + "android:screenOrientation=\"portrait\">\n")
            sb.append(" <meta-data\n" + "                android:name=\"android.app.lib_name\"\n" + "                android:value=\"\" />")
            sb.append("</activity>")
        }
        config.packageBase.split(',').each { packageBaseName ->
            for (i in 0..random.nextInt(7)) {
                sb.append("<service android:name=\"${packageBaseName}.${generateName(i)}\">")
                sb.append("</service>")
            }
        }
        sb.append("    </application>\n")
        sb.append("</manifest>")
        writeStringToFile(manifestFile, sb.toString())
    }

    /**
     * 生成 strings.xml */
    void generateStringsFile() {
        def stringFile = new File(outDir, "res/values/strings.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<resources>\n")
        for (i in 0..<stringList.size()) {
            sb.append("<string name=\"${stringList.get(i)}\">${stringList.get(i)}</string>\n")
        }
        sb.append("</resources>\n")
        writeStringToFile(stringFile, sb.toString())
    }

    void generateKeep() {
        def keepFile = new File(outDir, "res/raw/keep.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<resources xmlns:tools=\"http://schemas.android.com/tools\"\n" + "    tools:keep=\"@layout/${config.resPrefix}*, @drawable/${config.resPrefix}*\" />\n")
        writeStringToFile(keepFile, sb.toString())
    }

    void writeJavaFile(JavaFile javaFile) {
        def outputDirectory = new File(outDir, "java").toPath()
        if (!javaFile.packageName.isEmpty()) {
            for (String packageComponent : javaFile.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }
        Path outputPath = outputDirectory.resolve(javaFile.typeSpec.name + ".java");
        writeStringToFile(outputPath.toFile(), javaFile.toString())
    }

    void writeKotlinFile(FileSpec kotlinFile) {
        def outputDirectory = new File(outDir, "java").toPath()
        if (!kotlinFile.packageName.isEmpty()) {
            for (String packageComponent : kotlinFile.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }
        Path outputPath = outputDirectory.resolve(kotlinFile.name + ".kt");
        writeStringToFile(outputPath.toFile(), kotlinFile.toString())
    }

    private static void writeStringToFile(File file, String data) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs()
        }
        FileWriter writer
        try {
            writer = new FileWriter(file, Charset.forName("UTF-8"))
            writer.write(data)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }

    /**
     * 生成名称
     * @param index
     * @return
     */
    static String generateName(int index) {
        def sb = new StringBuilder()
        for (i in 0..random2.nextInt(10)) {
            sb.append(abc[random.nextInt(abc.size())])
        }
        int temp = index
        while (temp >= 0) {
            sb.append(abc[temp % abc.size()])
            temp = temp / abc.size()
            if (temp == 0) {
                temp = -1
            }
        }
        sb.append(index.toString())
        return sb.toString()
    }

    /**
     * 生成颜色代码
     * @return
     */
    static String generateColor() {
        def sb = new StringBuilder()
        sb.append("#")
        for (i in 0..5) {
            sb.append(color[random3.nextInt(color.size())])
        }
        return sb.toString()
    }
    /**
     * 生成id代码
     * @return
     */
    static String generateId() {
        def sb = new StringBuilder()
        for (i in 0..5) {
            sb.append(abc[random2.nextInt(abc.size())])
        }
        return sb.toString()
    }
}