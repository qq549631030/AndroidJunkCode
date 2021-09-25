package cn.hx.plugin.junkcode.ext

import org.gradle.api.NamedDomainObjectContainer

class AndroidJunkCodeExt {
    @Deprecated(since = "1.0.8")
    Map<String, Closure<JunkCodeConfig>> configMap = [:]

    NamedDomainObjectContainer<JunkCodeConfig> variantConfig
}