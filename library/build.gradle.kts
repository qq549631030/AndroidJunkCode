import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
}

val libraryProperties = Properties().apply {
    project.layout.projectDirectory.file("gradle.properties").asFile.inputStream().use(::load)
}

fun libraryProperty(name: String) = providers.provider {
    libraryProperties.getProperty(name) ?: error("Missing library gradle property '$name'")
}

if (libraryProperty("publishToMaven").get().toBoolean()) {
    pluginManager.apply("com.vanniktech.maven.publish")
    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
    }
} else {
    pluginManager.apply("com.gradle.plugin-publish")
    group = "io.github.qq549631030"//这里group id 不一样
    version = libraryProperty("VERSION_NAME").get()
    configure<GradlePluginDevelopmentExtension> {
        website.set(libraryProperty("POM_URL"))
        vcsUrl.set(libraryProperty("POM_SCM_URL"))
        plugins {
            create("androidJunkCode") {
                id = "io.github.qq549631030.android-junk-code"
                implementationClass = "cn.hx.plugin.junkcode.plugin.AndroidJunkCodePlugin"
                displayName = "AndroidJunkCode plugin"
                description = libraryProperty("POM_DESCRIPTION").get()
                tags.set(listOf("android", "generate", "junk", "code"))
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin.api)
    implementation(libs.javapoet)
}