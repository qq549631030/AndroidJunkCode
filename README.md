#  Android垃圾代码生成插件

此插件用于做马甲包时，减小马甲包与主包的代码相似度，避免被OPPO、VIVO等应用市场识别为马甲包。

### 使用方法

根目录的build.gradle中：
```
buildscript {
    dependencies {
        classpath "cn.hx.plugin:android-junk-code:1.0.0"
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

androidJunkCode {
    variants = ["debug"] //要生成垃圾代码的variant列表 eg：["vivoRelease","oppoRelease"]
    packages = ["cn.hx.test"] //要在哪些包名下生成Java类 eg:["cn.hx.test","cn.hx.demo"]
    activityCountPerPackage = 2 //每个包下生成Activity数量
    otherCountPerPackage = 10  //每个包下生成普通类数量
    methodCountPerClass = 10  每个类生生成方法数量
    resPrefix = "hx_junk_"  生成的layout、drawable、string等资源名前缀
    drawableCount = 30  生成drawable资源数量
    stringCount = 30  生成string数量
}
```