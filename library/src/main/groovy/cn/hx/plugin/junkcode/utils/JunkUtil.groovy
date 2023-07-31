package cn.hx.plugin.junkcode.utils

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.template.ResTemplate
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec

import javax.lang.model.element.Modifier
import java.nio.file.Files
import java.nio.file.Path

class JunkUtil {

    static random = new Random()

    static abc = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    static color = "0123456789abcdef".toCharArray()

    /**
     * 生成名称
     * @param index
     * @return
     */
    static String generateName(int index) {
        def sb = new StringBuilder()
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
     * 生成颜色代码
     * @return
     */
    static String generateColor() {
        def sb = new StringBuilder()
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
        def sb = new StringBuilder()
        for (i in 0..5) {
            sb.append(abc[random.nextInt(abc.size())])
        }
        return sb.toString()
    }

    /**
     * 生成Activity
     * @param javaDir
     * @param packageName
     * @param config
     */
    static List<String> generateActivity(File javaDir, File resDir, String namespace, String packageName, JunkCodeConfig config) {
        def activityList = new ArrayList()
        for (int i = 0; i < config.activityCountPerPackage; i++) {
            def activityPreName = generateName(i)
            def className = activityPreName.capitalize() + "Activity"
            def layoutName = "${config.resPrefix.toLowerCase()}${packageName.replace(".", "_")}_activity_${activityPreName}"
            generateLayout(resDir, layoutName, config)
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
                        def methodName = generateName(j)
                        def methodBuilder = MethodSpec.methodBuilder(methodName)
                        if (config.methodGenerator) {
                            config.methodGenerator.execute(methodBuilder)
                        } else {
                            generateMethods(methodBuilder)
                        }
                        typeBuilder.addMethod(methodBuilder.build())
                    }
                }
                def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
                writeJavaToFile(javaDir, javaFile)
                activityList.add(packageName + "." + className)
            }
        }
        return activityList
    }

    /**
     * 生成java文件
     * @param javaDir
     * @param packageName
     * @param config
     */
    static void generateJava(File javaDir, String packageName, JunkCodeConfig config) {
        for (int j = 0; j < config.otherCountPerPackage; j++) {
            def className = generateName(j).capitalize()
            def typeBuilder = TypeSpec.classBuilder(className)
            if (config.typeGenerator) {
                config.typeGenerator.execute(typeBuilder)
            } else {
                typeBuilder.addModifiers(Modifier.PUBLIC)
                for (int k = 0; k < config.methodCountPerClass; k++) {
                    def methodName = generateName(k)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
                    if (config.methodGenerator) {
                        config.methodGenerator.execute(methodBuilder)
                    } else {
                        generateMethods(methodBuilder)
                    }
                    typeBuilder.addMethod(methodBuilder.build())
                }
            }
            def javaFile = JavaFile.builder(packageName, typeBuilder.build()).build()
            writeJavaToFile(javaDir, javaFile)
        }
    }

    /**
     * 生成layout文件
     * @param resDir
     * @param layoutName
     * @param config
     */
    static void generateLayout(File resDir, String layoutName, JunkCodeConfig config) {
        def layoutFile = new File(resDir, "layout/${layoutName}.xml")
        if (config.layoutGenerator) {
            def builder = new StringBuilder()
            config.layoutGenerator.execute(builder)
            writeStringToFile(layoutFile, builder.toString())
        } else {
            def layoutStr = String.format(ResTemplate.LAYOUT_TEMPLATE, generateId())
            writeStringToFile(layoutFile, layoutStr)
        }
    }

    /**
     * 生成drawable
     * @param resDir
     * @param config
     */
    static void generateDrawableFiles(File resDir, JunkCodeConfig config) {
        for (int i = 0; i < config.drawableCount; i++) {
            def drawableName = "${config.resPrefix.toLowerCase()}${generateName(i)}"
            def drawableFile = new File(resDir, "drawable/${drawableName}.xml")
            if (config.drawableGenerator) {
                def builder = new StringBuilder()
                config.drawableGenerator.execute(builder)
                writeStringToFile(drawableFile, builder.toString())
            } else {
                def drawableStr = String.format(ResTemplate.DRAWABLE, generateColor())
                writeStringToFile(drawableFile, drawableStr)
            }
        }
    }

    /**
     * 生成strings.xml
     * @param resDir
     * @param config
     */
    static void generateStringsFile(File resDir, JunkCodeConfig config) {
        def stringFile = new File(resDir, "values/strings.xml")
        StringBuilder sb = new StringBuilder()
        StringBuilder valueSb = new StringBuilder()
        sb.append("<resources>\n")
        for (int i = 0; i < config.stringCount; i++) {
            def key = "${config.resPrefix.toLowerCase()}${generateName(i)}"
            def value
            if (config.stringGenerator) {
                valueSb.setLength(0)
                config.stringGenerator.execute(valueSb)
                value = valueSb.toString()
            } else {
                value = generateName(i)
            }
            sb.append("<string name=\"${key}\">${value}</string>\n")
        }
        sb.append("</resources>\n")
        writeStringToFile(stringFile, sb.toString())
    }

    /**
     * 生成keep.xml
     * @param resDir
     * @param config
     */
    static void generateKeep(File resDir, JunkCodeConfig config) {
        def keepFile = new File(resDir, "raw/android_junk_code_keep.xml")
        StringBuilder sb = new StringBuilder()
        sb.append("<resources xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                "    tools:keep=\"@layout/${config.resPrefix}*, @drawable/${config.resPrefix}*\" />\n")
        writeStringToFile(keepFile, sb.toString())
    }

    /**
     * 生成AndroidManifest.xml
     * @param manifestFile
     * @param activityList
     */
    static void generateManifest(File manifestFile, List<String> activityList) {
        StringBuilder sb = new StringBuilder()
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n")
        sb.append("    <application>\n")
        for (i in 0..<activityList.size()) {
            sb.append("        <activity android:name=\"${activityList.get(i)}\"/>\n")
        }
        sb.append("    </application>\n")
        sb.append("</manifest>")
        writeStringToFile(manifestFile, sb.toString())
    }

    /**
     * java写入文件
     * @param javaDir
     * @param javaFile
     */
    static void writeJavaToFile(File javaDir, JavaFile javaFile) {
        def outputDirectory = javaDir.toPath()
        if (!javaFile.packageName.isEmpty()) {
            for (String packageComponent : javaFile.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }
        Path outputPath = outputDirectory.resolve(javaFile.typeSpec.name + ".java");
        writeStringToFile(outputPath.toFile(), javaFile.toString())
    }

    /**
     * 字符串写入文件
     * @param file
     * @param data
     */
    static void writeStringToFile(File file, String data) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs()
        }
        FileWriter writer
        try {
            writer = new FileWriter(file)
            writer.write(data)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                writer.close()
            }
        }
    }
}