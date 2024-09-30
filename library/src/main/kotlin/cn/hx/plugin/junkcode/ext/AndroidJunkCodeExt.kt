package cn.hx.plugin.junkcode.ext

import org.gradle.api.NamedDomainObjectContainer

open class AndroidJunkCodeExt(val variantConfig: NamedDomainObjectContainer<JunkCodeConfig>) {
    var debug = false
}