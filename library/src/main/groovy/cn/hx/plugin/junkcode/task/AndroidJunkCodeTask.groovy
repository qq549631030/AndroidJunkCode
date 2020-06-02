package cn.hx.plugin.junkcode.task

import cn.hx.plugin.junkcode.template.ActivityNodeTemplate
import cn.hx.plugin.junkcode.template.ManifestTemplate
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import groovy.text.GStringTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction

import javax.lang.model.element.Modifier

class AndroidJunkCodeTask extends DefaultTask {

    static def random = new Random()

    static abc = "abcdefghijklmnopqrstuvwxyz".toCharArray()

    @Input
    String manifestPackage = "com.example"
    @Input
    String[] packages = []
    @Input
    int fileCountPerPackage = 0
    @Input
    int methodCountPerClass = 0

    @OutputDirectories
    File outDir

    @TaskAction
    void execute() {
        packages.each { packageName ->
            for (int i = 0; i < fileCountPerPackage; i++) {
                def className = generateName(i)
                def typeBuilder = TypeSpec.classBuilder(className)
                typeBuilder.superclass(ClassName.get("android.app", "Activity"))
                for (int j = 0; j < methodCountPerClass; j++) {
                    def methodName = generateName(j)
                    def methodBuilder = MethodSpec.methodBuilder(methodName)
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
                    typeBuilder.addMethod(methodBuilder.build())
                }
                def fileBuilder = JavaFile.builder(packageName, typeBuilder.build())
                fileBuilder.build().writeTo(outDir)
//                addToManifestByFileIo(className, packageName)
            }
        }
    }

    static String generateName(int index) {
        def sb = new StringBuffer()
        int temp = index
        while (temp >= 0) {
            sb.append(abc[temp % abc.size()])
            temp = temp / abc.size()
            if (temp == 0) {
                temp = -1
            }
        }
        return sb.toString()
    }

    /**
     * 通过文件读写流的方式将新创建的Activity加入清单文件
     *
     * @param activityName
     * @param packageName
     */
    void addToManifestByFileIo(String activityName, String packageName) {
        def manifestFile = new File(outDir, "AndroidManifest.xml")
        if (!manifestFile.exists()) {
            def binding = [
                    packageName: manifestPackage,
            ]
            def template = makeTemplate(new ManifestTemplate().template, binding)
            FileWriter writer
            try {
                writer = new FileWriter(manifestFile)
                writer.write(template.toString())
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                if (writer != null) {
                    writer.close()
                }
            }
        }
        FileReader reader
        FileWriter writer
        try {
            reader = new FileReader(manifestFile)
            StringBuilder sb = new StringBuilder()
            // 每一行的内容
            String line = ""
            while ((line = reader.readLine()) != null) {
                // 找到application节点的末尾
                if (line.contains("</application>")) {
                    // 在application节点最后插入新创建的activity节点
                    def binding = [
                            packageName : packageName,
                            activityName: activityName,
                    ]
                    def template = makeTemplate(new ActivityNodeTemplate().template, binding)
                    sb.append(template.toString() + "\n")
                }
                sb.append(line + "\n")
            }
            String content = sb.toString()
            writer = new FileWriter(manifestFile)
            writer.write(content)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                reader.close()
            }
            if (writer != null) {
                writer.close()
            }
        }
    }


    /**
     * 加载模板
     *
     * @param template
     * @param binding
     * @return
     */
    static def makeTemplate(def template, def binding) {
        def engine = new GStringTemplateEngine()
        return engine.createTemplate(template).make(binding)
    }
}