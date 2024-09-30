plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    alias(libs.plugins.kotlin.jvm)
}

gradlePlugin {
    website.set(project.properties["POM_URL"].toString())
    vcsUrl.set(project.properties["POM_SCM_URL"].toString())
    plugins {
        create("androidJunkCode") {
            id = "io.github.qq549631030.android-junk-code"
            implementationClass = "cn.hx.plugin.junkcode.plugin.AndroidJunkCodePlugin"
            displayName = "AndroidJunkCode plugin"
            description = project.properties["POM_DESCRIPTION"].toString()
            tags.set(listOf("android", "generate", "junk", "code"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin.api)
    implementation(gradleKotlinDsl())
    implementation(libs.javapoet)
}