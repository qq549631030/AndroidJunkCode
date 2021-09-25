package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import org.gradle.api.NamedDomainObjectFactory

class JunkCodeConfigFactory implements NamedDomainObjectFactory<JunkCodeConfig> {

    @Override
    JunkCodeConfig create(String name) {
        return new JunkCodeConfig(name)
    }
}