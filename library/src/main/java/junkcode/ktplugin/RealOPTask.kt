package junkcode.ktplugin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.IOException
import java.util.Date
import java.util.Random

object RealOPTask {

    val abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    val random = Random()

    fun generateKTActivity(
        namespace: String, config: JunkCodeConfig,
        packageName: String, activityPreName: String,
        layoutName: String
    ): FileSpec {
        val className = activityPreName.capitalize() + "Activity"
        val typeBuilder =
            TypeSpec.classBuilder(className).superclass(ClassName("android.app", "Activity"))
                .addFunction(
                    FunSpec.builder("onCreate")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter(
                            "savedInstanceState",
                            ClassName("android.os", "Bundle").copy(nullable = true)
                        )
                        .addStatement("super.onCreate(savedInstanceState)")
                        .addStatement(
                            "setContentView(%T.layout.${layoutName})",
                            ClassName(namespace, "R")
                        )
                        .build()
                ).addFunction(
                    FunSpec.builder("onDestroy").addModifiers(KModifier.OVERRIDE)
                        .addStatement("super.onDestroy()").build()
                ).addModifiers(KModifier.PUBLIC)

        for (i in 0..random.nextInt(config.methodCountPerClass)) {
            val methodName = generateName(i)
            val methodBuilder = FunSpec.builder(methodName)
            generateMethods(methodBuilder)
            typeBuilder.addFunction(methodBuilder.build())
        }
        val kotlinFile =
            FileSpec.builder(packageName, className).addType(typeBuilder.build()).build()

        return kotlinFile
    }


    private fun generateMethods(builder: FunSpec.Builder) {
        when (random.nextInt(4)) {
            0 -> {
                builder.addStatement("val now = %T.currentTimeMillis()", System::class.java)
                    .beginControlFlow("if (%T.currentTimeMillis() < now)", System::class.java)
                    .addStatement(
                        "%T.out.println(%S)", System::class.java, "Time travelling, woo hoo!"
                    ).nextControlFlow("else if (%T.currentTimeMillis() == now)", System::class.java)
                    .addStatement("%T.out.println(%S)", System::class.java, "Time stood still!")
                    .nextControlFlow("else").addStatement(
                        "%T.out.println(%S)", System::class.java, "Ok, time still moving forward"
                    ).endControlFlow()
            }

            1 -> {
                builder.addCode("" + "var sum = 0\n" + " for (i in 0..9) {\n" + "  sum += i\n" + "}\n")
            }

            2 -> {
                builder.beginControlFlow("try")
                    .addStatement("throw %T(%S)", IOException::class.asTypeName(), "Failed")
                    .nextControlFlow("catch (e :%T)", IOException::class.asTypeName())
                    .addStatement("throw %T(e)", RuntimeException::class.asTypeName())
                    .endControlFlow()
            }

            3 -> {
                builder.returns(Date::class).addStatement("return %T()", Date::class.java)
            }

            else -> {
            }
        }
    }

    private fun generateName(index: Int): String {
        val sb = StringBuilder()
        for (i in 0..random.nextInt(4)) {
            sb.append(abc[random.nextInt(abc.size)])
        }
        var temp: Int = index
        while (temp >= 0) {
            sb.append(abc[(temp % abc.size)])
            temp /= abc.size
            if (temp == 0) {
                temp = -1
            }
        }
        sb.append(index.toString())
        return sb.toString()
    }

}