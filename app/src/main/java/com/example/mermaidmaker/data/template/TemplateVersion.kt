package com.example.mermaidmaker.data.template

data class TemplateVersion(
    val version: Int,
    val templateIds: Set<String>,
    val description: String
)

object TemplateVersionManager {
    
    const val CURRENT_VERSION = 1
    
    private val versions = listOf(
        TemplateVersion(
            version = 1,
            templateIds = setOf(
                "flowchart_basic",
                "flowchart_process", 
                "flowchart_algorithm",
                "sequence_api",
                "sequence_auth",
                "sequence_payment",
                "class_basic",
                "class_ecommerce",
                "state_user_session",
                "state_order",
                "er_blog",
                "er_ecommerce",
                "gitgraph_feature",
                "journey_shopping"
            ),
            description = "Initial built-in templates with comprehensive diagram type coverage"
        )
    )
    
    fun getCurrentVersion(): TemplateVersion {
        return versions.last()
    }
    
    fun getVersion(version: Int): TemplateVersion? {
        return versions.find { it.version == version }
    }
    
    fun getAllVersions(): List<TemplateVersion> {
        return versions
    }
    
    fun getTemplatesForVersion(version: Int): List<String> {
        return getVersion(version)?.templateIds?.toList() ?: emptyList()
    }
    
    fun isValidTemplate(templateId: String, version: Int = CURRENT_VERSION): Boolean {
        return getVersion(version)?.templateIds?.contains(templateId) == true
    }
    
    fun getNewTemplatesInVersion(fromVersion: Int, toVersion: Int): Set<String> {
        val fromTemplates = getVersion(fromVersion)?.templateIds ?: emptySet()
        val toTemplates = getVersion(toVersion)?.templateIds ?: emptySet()
        return toTemplates - fromTemplates
    }
    
    fun getRemovedTemplatesInVersion(fromVersion: Int, toVersion: Int): Set<String> {
        val fromTemplates = getVersion(fromVersion)?.templateIds ?: emptySet()
        val toTemplates = getVersion(toVersion)?.templateIds ?: emptySet()
        return fromTemplates - toTemplates
    }
}