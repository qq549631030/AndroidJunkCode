# Repository Guidelines

## 项目结构与模块组织
`AndroidJunkCode` 是一个多模块 Gradle 项目：
- `library/`：Gradle 插件实现（`cn.hx.plugin.junkcode.*`），可发布到 Maven Central 或 Gradle Plugin Portal。
- `app/`：用于验证插件行为与垃圾代码生成效果的示例 Android 应用。
- `gradle/libs.versions.toml`：统一管理依赖与插件版本。
- `images/`：README 中的效果对比图片资源。
- `build/` 与 `.gradle/`：构建产物目录，不应提交生成文件。

插件核心逻辑应放在 `library/src/main/kotlin/...`，避免把生产逻辑放入示例应用。

## 构建、测试与开发命令
请在仓库根目录使用 Gradle Wrapper：
- `./gradlew.bat clean build`：全量构建所有模块。
- `./gradlew.bat :library:build`：仅构建并校验插件模块。
- `./gradlew.bat :app:assembleDebug`：构建示例应用 Debug APK。
- `./gradlew.bat :app:testDebugUnitTest`：运行 `app/src/test` 下的 JVM 单元测试。
- `./gradlew.bat :app:connectedDebugAndroidTest`：在设备/模拟器上运行仪器测试。

提示：可通过 `gradle.properties` 中的 `PLUGIN_ENABLE=true/false` 控制示例应用构建时是否启用插件。

## 代码风格与命名规范
- Kotlin 风格：`kotlin.code.style=official`（遵循 IDE/ktlint 兼容格式）。
- 缩进：4 个空格，禁止 Tab。
- 类名使用 `PascalCase`；函数/属性使用 `camelCase`；常量使用 `UPPER_SNAKE_CASE`。
- 包名保持小写（例如 `cn.hx.plugin.junkcode.task`）。
- 新增代码尽量保持小而专注，命名延续现有模式（如 `*Task`、`*Ext`、`*Config`）。

## 测试规范
- 测试框架：JUnit4（`app/src/test`）与 AndroidX Instrumentation/Espresso（`app/src/androidTest`）。
- 测试类名使用 `*Test` 后缀，测试方法名应准确表达预期行为。
- 修改生成规则、任务编排或变体逻辑时，需同步补充/更新测试。
- 调整插件扩展逻辑时，至少验证 `debug` 与 `release` 两条配置路径。

## 提交与合并请求规范
历史提交多为简短祈使句（常用中文），通常一条提交聚焦一个结果（如：`解决报错`、`调整发布配置`）。建议遵循：
- 每个提交只做一个逻辑变更。
- 提交标题直接描述结果。
- PR 需包含：变更目的、影响模块（`app`/`library`）、已执行测试命令、行为或输出变化的截图/日志片段。
- 有关联问题或 Wiki 时请附链接。
