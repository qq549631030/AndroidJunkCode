package cn.hx.plugin.junkcode.ext

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

class AndroidJunkCodeExt {

    NamedDomainObjectContainer<JunkCodeConfig> variantConfig

    void variantConfig(Action<? super NamedDomainObjectContainer<JunkCodeConfig>> action) {
        action.execute(variantConfig)
    }
}