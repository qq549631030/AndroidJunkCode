#  Android垃圾代码生成插件

[![Download](https://api.bintray.com/packages/qq549631030/maven/AndroidJunkCode/images/download.svg) ](https://bintray.com/qq549631030/maven/AndroidJunkCode/_latestVersion)

此插件用于做马甲包时，减小马甲包与主包的代码相似度，避免被OPPO、VIVO等应用市场识别为马甲包。

### 使用方法

根目录的build.gradle中：
```
buildscript {
    dependencies {
        classpath "cn.hx.plugin:android-junk-code:1.0.4"
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
    switch (variant.name) {//变体名称，如果没有设置productFlavors就是buildType名称，如果有设置productFlavors就是flavor+buildType，例如（freeRelease、proRelease）
        case "release":
            androidJunkCode.configMap.put(variant.name, {
                packageBase = "cn.hx.plugin.ui"  //生成java类根包名
                packageCount = 30 //生成包数量
                activityCountPerPackage = 3 //每个包下生成Activity类数量
                otherCountPerPackage = 50  //每个包下生成其它类的数量
                methodCountPerClass = 20  //每个类下生成方法数量
                resPrefix = "junk_"  //生成的layout、drawable、string等资源名前缀
                drawableCount = 300  //生成drawable资源数量
                stringCount = 300  //生成string数量
            })
            break
    }
}
```

### 生成文件所在目录
build/generated/source/junk

### 使用插件[methodCount](https://github.com/KeepSafe/dexcount-gradle-plugin)对比

#### 未加垃圾代码
```
Total methods in app-debug.apk: 26162 (39.92% used)
Total fields in app-debug.apk:  12771 (19.49% used)
Total classes in app-debug.apk:  2897 (4.42% used)
Methods remaining in app-debug.apk: 39373
Fields remaining in app-debug.apk:  52764
Classes remaining in app-debug.apk:  62638
```

#### 加了垃圾代码
```
Total methods in app-release-unsigned.apk: 59733 (91.15% used)
Total fields in app-release-unsigned.apk:  13462 (20.54% used)
Total classes in app-release-unsigned.apk:  4488 (6.85% used)
Methods remaining in app-release-unsigned.apk: 5802
Fields remaining in app-release-unsigned.apk:  52073
Classes remaining in app-release-unsigned.apk:  61047
```
增加了1591个类33571个方法