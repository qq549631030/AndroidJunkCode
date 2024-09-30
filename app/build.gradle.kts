import cn.hx.plugin.junkcode.ext.JunkCodeConfig

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junk.code)
    alias(libs.plugins.dexcount)
}

android {
    namespace = "cn.hx.plugin.junkcode.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.hx.plugin.junkcode.demo"
        minSdk = 19
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

if (project.properties["PLUGIN_ENABLE"].toString().toBoolean()) {
    androidJunkCode {
        debug = true
        /**
         * 生成逻辑由插件完成
         */
        val config = Action<JunkCodeConfig> {
            packageBase = "cn.hx.plugin.ui"
            packageCount = 30
            activityCountPerPackage = 30
            excludeActivityJavaFile = false
            otherCountPerPackage = 50
            methodCountPerClass = 20
            resPrefix = "junk_"
            drawableCount = 300
            stringCount = 300
        }

        /**
         * 生成逻辑自定义
         */
        val partCustomConfig = Action<JunkCodeConfig> {
            packageBase = "cn.hx.plugin.ui"
            packageCount = 3
            activityCountPerPackage = 30
            excludeActivityJavaFile = false
            otherCountPerPackage = 50
            methodCountPerClass = 20
            resPrefix = "junk_"
            drawableCount = 300
            stringCount = 300

            //自定义生成包名(设置此项后packageBase将无效)
            //注意，要把生成的包名加入混淆文件
            packageCreator = Action {
                //int:下标 [0,packageCount)
                val index = v1
                //StringBuilder: 生成包名格式xx.xx.xx
                val packageNameBuilder = v2
                packageNameBuilder.append("cn.hx.package" + index)
            }

            /**
             * 自定义生成Activity
             */
            activityCreator = Action {
                //int:下标 [0,activityCountPerPackage)
                val index = v1
                //StringBuilder: 生成Activity文件名
                val activityNameBuilder = v2
                //StringBuilder: 生成layout文件名
                val layoutNameBuilder = v3
                //StringBuilder: 生成layout内容
                val layoutContentBuilder = v4

                //例
                activityNameBuilder.append("Activity${index}")
                layoutNameBuilder.append("activity_${index}")
                layoutContentBuilder.append(
                    """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />
</LinearLayout>"""
                )
            }

            //自定义生成类名（Activity除外）
            classNameCreator = Action {
                //int:下标 [0,otherCountPerPackage)
                val index = v1
                //StringBuilder: 生成Java文件名
                val classNameBuilder = v2

                //例
                classNameBuilder.append("Class${index}")
            }

            //自定义生成方法名
            methodNameCreator = Action {
                //int:下标 [0,methodCountPerClass)
                val index = v1
                //StringBuilder: 生成的方法名
                val classNameBuilder = v2

                //例
                classNameBuilder.append("method${index}")
            }

            //自定义生成drawable（只支持xml）
            drawableCreator = Action {
                //int:下标 [0,drawableCount)
                val index = v1
                //StringBuilder: 生成drawable文件名
                val fileNameBuilder = v2
                //StringBuilder: 生成drawable文件内容
                val contentBuilder = v3

                //例
                fileNameBuilder.append("drawable${index}")
                contentBuilder.append(
                    """<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <corners android:radius="4dp" />
    <stroke
        android:width="1dp"
        android:color="#333333" />
</shape>
                """
                )
            }

            //自定义生成 string
            stringCreator = Action {
                //int:下标 [0,drawableCount)
                val index = v1
                //StringBuilder: 生成string名
                val keyBuilder = v2
                //StringBuilder: 生成string值
                val valueBuilder = v3

                //例
                keyBuilder.append("string${index}")
                valueBuilder.append("value${index}")
            }

            //自定义生成keep.xm
            keepCreator = Action {
                //StringBuilder:生成文件名
                val fileNameBuilder = v1
                //StringBuilder: 生成的文件内容
                val contentBuilder = v2

                //例
                fileNameBuilder.append("android_junk_code_keep")
                contentBuilder.append(
                    "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                            "    tools:keep=\"@layout/activity_*, @drawable/drawable*\" />\n"
                )
            }

            proguardCreator = Action {
                //List<String>:生成的包名列表
                val packageList = v1
                //StringBuilder: 生成的文件内容
                val contentBuilder = v2

                //例
                for (i in 0 until packageList.size) {
                    contentBuilder.append("-keep class ${packageList.get(i)}.**{*;}\n")
                }
            }
            //自定义类实现（类名已经实现随机，Activity类已经实现了onCreate，其它自己实现随机）
            //注意设置了此实现将忽略 methodGenerator,methodCountPerClass
            //TypeSpec.Builder用法请参考(https://github.com/square/javapoet)
//            typeGenerator = { typeBuilder ->
//                //例
//                for (i in 0..<10) {
//                    typeBuilder.addMethod(MethodSpec.methodBuilder("method" + i)
//                            .addCode("" + "int total = 0;\n" + "for (int i = 0; i < 10; i++) {\n" + "  total += i;\n" + "}\n")
//                            .build())
//                }
//            }

            //自定义方法实现（方法名已经实现随机，其它自己实现随机）
            //MethodSpec.Builder用法请参考(https://github.com/square/javapoet)
//            methodGenerator = { methodBuilder ->
//               //例
//                methodBuilder.addCode("" + "int total = 0;\n" + "for (int i = 0; i < 10; i++) {\n" + "  total += i;\n" + "}\n")
//            }
        }

        variantConfig {
            create("debug", config)
            create("release", partCustomConfig)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}