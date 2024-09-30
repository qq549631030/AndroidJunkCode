plugins {
    groovy
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(gradleApi())
    implementation(libs.javapoet)
    compileOnly(libs.gradle.api)
}