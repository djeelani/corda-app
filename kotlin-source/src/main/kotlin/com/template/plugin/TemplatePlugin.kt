package com.template.plugin

import com.template.api.TemplateApi
import com.template.flow.TemplateFlow
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.node.PluginServiceHub
import net.corda.core.crypto.Party

import java.util.function.Function

class TemplatePlugin : CordaPluginRegistry() {
    override val webApis = listOf(Function(::TemplateApi))
    override val requiredFlows = mapOf(TemplateFlow::class.java.name to setOf(Party::class.java.name))
    override val servicePlugins: List<Function<PluginServiceHub, out Any>> = listOf()
    override val staticServeDirs = mapOf("template" to javaClass.classLoader.getResource("templateWeb").toExternalForm())
}