# Android垃圾代码生成插件

此插件用于做马甲包时，减小马甲包与主包的代码相似度，避免被某些应用市场识别为马甲包。

使用方法见[wiki](https://github.com/qq549631030/AndroidJunkCode/wiki)

使用插件[methodCount](https://github.com/KeepSafe/dexcount-gradle-plugin)对比

|                               | 方法总数                             | 项目方法数                               |
| ----------------------------- |:--------------------------------:|:-----------------------------------:|
| 未加垃圾代码<br/><br/>项目代码占比 0.13%  | ![方法总数](images/before_total.jpg) | ![项目方法数](images/before_project.jpg) |
| 加了垃圾代码<br/><br/>项目代码占比 52.93% | ![方法总数](images/after_total.jpg)  | ![项目方法数](images/after_project.jpg)  |

安利我的两个新库：  
[PriorityDialog](https://github.com/qq549631030/PriorityDialog)（带优先级对话框实现）  
[ActivityResultApi](https://github.com/qq549631030/ActivityResultApi)（Activity Result Api封装，支持免注册调用）
