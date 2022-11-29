package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.template.ResTemplate
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.lang.model.element.Modifier

class AndroidJunkCodeTask extends DefaultTask {

    static def random = new Random()

    static abc = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    static color = "0123456789abcdef".toCharArray()

    @Nested
    JunkCodeConfig config

    @Input
    String namespace

    @OutputDirectory
    File outDir

    private List<String> activityList = new ArrayList<>()
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
     * 生成java代码和AndroidManifest.xml
     */
    void generateClasses() {
        def javaDir = new File(outDir, "java")
        for (int i = 0; i < config.packageCount; i++) {
            String packageName
            if (config.packageBase.isEmpty()) {
                packageName = generateName(i)
            } else {
                packageName = config.packageBase + "." + generateName(i)
            }
            //生成Activity
            for (int j = 0; j < config.activityCountPerPackage; j++) {
                def activityPreName = generateName(j)
                generateActivity(packageName, activityPreName)
            }
            //生成其它类
            for (int j = 0; j < config.otherCountPerPackage; j++) {
                def className = generateName(j).capitalize()
                def typeBuilder = TypeSpec.classBuilder(className)
                typeBuilder.addModifiers(Modifier.PUBLIC)
                for (int k = 0; k < config.methodCountPerClass; k++) {
                    def methodName = generateName(k)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
                    generateMethods(methodBuilder)
                    typeBuilder.addMethod(methodBuilder.build())
                }
                def fileBuilder = JavaFile.builder(packageName, typeBuilder.build())
                fileBuilder.build().writeTo(javaDir)
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
        switch (random.nextInt(5)) {
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
                methodBuilder.addCode(""
                        + "int total = 0;\n"
                        + "for (int i = 0; i < 10; i++) {\n"
                        + "  total += i;\n"
                        + "}\n")
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
     * 生成Activity
     * @param packageName
     * @param activityPreName
     */
    void generateActivity(String packageName, String activityPreName) {
        def javaDir = new File(outDir, "java")
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
            //其它方法
            for (int j = 0; j < config.methodCountPerClass; j++) {
                def methodName = generateName(j)
                def methodBuilder = MethodSpec.methodBuilder(methodName)
                generateMethods(methodBuilder)
                typeBuilder.addMethod(methodBuilder.build())
            }
            def fileBuilder = JavaFile.builder(packageName, typeBuilder.build())
            fileBuilder.build().writeTo(javaDir)
        }
        activityList.add(packageName + "." + className)
    }

    /**
     * 生成资源文件
     */
    void generateRes() {
        //生成drawable
        for (int i = 0; i < config.drawableCount; i++) {
            def drawableName = "${config.resPrefix.toLowerCase()}${generateName(i)}"
            generateDrawable(drawableName)
        }
        //生成string
        for (int i = 0; i < config.stringCount; i++) {
            stringList.add("${config.resPrefix.toLowerCase()}${generateName(i)}")
        }
        generateStringsFile()
    }

    /**
     * 生成layout
     * @param layoutName
     */
    void generateDrawable(String drawableName) {
        def drawableFile = new File(outDir, "res/drawable/${drawableName}.xml")
        if (!drawableFile.getParentFile().exists()) {
            drawableFile.getParentFile().mkdirs()
        }
        FileWriter writer
        try {
            writer = new FileWriter(drawableFile)
            def template = String.format(ResTemplate.DRAWABLE, generateColor())
            writer.write(template.toString())
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }


    /**
     * 生成layout
     * @param layoutName
     */
    void generateLayout(String layoutName) {
        def layoutFile = new File(outDir, "res/layout/${layoutName}.xml")
        if (!layoutFile.getParentFile().exists()) {
            layoutFile.getParentFile().mkdirs()
        }
        FileWriter writer
        try {
            writer = new FileWriter(layoutFile)
            def template = String.format(ResTemplate.LAYOUT_TEMPLATE, generateId())
            writer.write(template.toString())
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }


    /**
     * 生成Manifest
     */
    void generateManifest() {
        def manifestFile = new File(outDir, "AndroidManifest.xml")
        if (!manifestFile.getParentFile().exists()) {
            manifestFile.getParentFile().mkdirs()
        }
        StringBuilder sb = new StringBuilder()
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
        sb.append("    <application>\n")
        for (i in 0..<activityList.size()) {
            sb.append("        <activity android:name=\"${activityList.get(i)}\"/>\n")
        }
        sb.append("    </application>\n")
        sb.append("</manifest>")
        FileWriter writer
        try {
            writer = new FileWriter(manifestFile)
            writer.write(sb.toString())
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }

    /**
     * 生成strings.xml
     */
    void generateStringsFile() {
        def stringFile = new File(outDir, "res/values/strings.xml")
        if (!stringFile.getParentFile().exists()) {
            stringFile.getParentFile().mkdirs()
        }
        StringBuilder sb = new StringBuilder()
        sb.append("<resources>\n")
        for (i in 0..<stringList.size()) {
            sb.append("<string name=\"${stringList.get(i)}\">${stringList.get(i)}</string>\n")
        }
        sb.append("</resources>\n")
        FileWriter writer
        try {
            writer = new FileWriter(stringFile)
            writer.write(sb.toString())
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
        def sb = new StringBuffer()
        for (i in 0..4) {
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
        def sb = new StringBuffer()
        sb.append("#")
        for (i in 0..5) {
            sb.append(color[random.nextInt(color.size())])
        }
        return sb.toString()
    }
    /**
     * 生成id代码
     * @return
     */
    static String generateId() {
        def sb = new StringBuffer()
        for (i in 0..5) {
            sb.append(abc[random.nextInt(abc.size())])
        }
        return sb.toString()
    }
}