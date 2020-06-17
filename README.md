#  Android垃圾代码生成插件

[![Download](https://api.bintray.com/packages/qq549631030/maven/AndroidJunkCode/images/download.svg) ](https://bintray.com/qq549631030/maven/AndroidJunkCode/_latestVersion)

此插件用于做马甲包时，减小马甲包与主包的代码相似度，避免被OPPO、VIVO等应用市场识别为马甲包。

### 使用方法

根目录的build.gradle中：
```
buildscript {
    dependencies {
        classpath "cn.hx.plugin:android-junk-code:1.0.1"
    }
}
```
app目录的build.gradle模块中：
```
apply plugin: 'com.android.application'
apply plugin: 'android-junk-code'

android {
    //xxx
}

android.applicationVariants.all { variant ->
    switch (variant.name) {
        case "debug":
        case "release":
            androidJunkCode.configMap.put(variant.name, {
                packageBase = "cn.hx.plugin.ui"  //生成java类根包名
                packageCount = 30 //生成包数量
                activityCountPerPackage = 3 //每个包下生成Activity数量
                otherCountPerPackage = 10  //每个类生生成方法数量
                methodCountPerClass = 10  //每个类生生成方法数量
                resPrefix = "lite_junk_"  //生成的layout、drawable、string等资源名前缀
                drawableCount = 30  //生成drawable资源数量
                stringCount = 30  //生成string数量
            })
            break
    }
}
```