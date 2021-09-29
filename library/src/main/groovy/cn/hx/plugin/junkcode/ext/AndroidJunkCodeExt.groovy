package cn.hx.plugin.junkcode.ext

import org.gradle.api.NamedDomainObjectContainer

class AndroidJunkCodeExt {
    Map<String, Closure<JunkCodeConfig>> configMap = [:]

    NamedDomainObjectContainer<JunkCodeConfig> variantConfig
}