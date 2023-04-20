#  Android垃圾代码生成插件

此插件用于做马甲包时，减小马甲包与主包的代码相似度，避免被某些应用市场识别为马甲包。

### 使用方法

根目录的build.gradle中：
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "com.github.qq549631030:android-junk-code:x.x.x"
    }
}
```


app目录的build.gradle模块中：
```groovy
apply plugin: 'com.android.application'
apply plugin: 'android-junk-code'

androidJunkCode {
    variantConfig {
        release {//变体名称，如果没有设置productFlavors就是buildType名称，如果有设置productFlavors就是flavor+buildType，例如（freeRelease、proRelease）
            packageBase = 'cn.hx.plugin.ui,com.aa.model,aa.web'  //生成 java 或者 kotlin 类根包名,逗号分割
            packageCount = 12 //生成包最大数量
            activityCountPerPackage = 30 //每个包下生成 Activity 类最大数量
            excludeActivityJavaFile = false//是否排除生成 Activity 的文件,默认 false(layout 和写入 AndroidManifest.xml 还会执行)，主要用于处理类似神策全埋点编译过慢问题
            otherCountPerPackage = 50  //每个包下生成其它类的最大数量
            methodCountPerClass = 50  //每个类下生成方法最大数量
            resPrefix = "pp_"  //生成的 layout、drawable、string 等资源名前缀
            drawableCount = 200  //生成 drawable 资源最大数量
            stringCount = 200  //生成 string 最大数量
        }
    }
}
```

如果有多个变体共用一个配置可以这样做
```groovy
androidJunkCode {
    def config = {
        packageBase = "cn.hx.plugin.ui"
        packageCount = 30
        activityCountPerPackage = 3
        excludeActivityJavaFile = false
        otherCountPerPackage = 50
        methodCountPerClass = 20
        resPrefix = "junk_"
        drawableCount = 300
        stringCount = 300
    }
    variantConfig {
        debug config
        release config
    }
}
```

如果APP开启了混淆，需要在混淆文件里配置

```
#cn.hx.plugin.ui为前面配置的packageBase
-keep class cn.hx.plugin.ui.** {*;}
```
### 打包
执行配置变体的打包命令：assembleXXX（XXX是你配置的变体，如：assembleRelease、assembleFreeRelease）

### 生成文件所在目录
build/generated/source/junk

### 使用插件[methodCount](https://github.com/KeepSafe/dexcount-gradle-plugin)对比

#### 未加垃圾代码

**项目代码占比 0.13%**

![方法总数](images/before_total.jpg)![项目方法数](images/before_project.jpg)


#### 加了垃圾代码

**项目代码占比 52.93%**

![方法总数](images/after_total.jpg)![项目方法数](images/after_project.jpg)

安利我的两个新库：  
[PriorityDialog](https://github.com/qq549631030/PriorityDialog)（带优先级对话框实现）  
[ActivityResultApi](https://github.com/qq549631030/ActivityResultApi)（Activity Result Api封装，支持免注册调用）
