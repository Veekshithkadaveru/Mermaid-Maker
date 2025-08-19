package com.example.mermaidmaker.data.template

import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.first

class TemplateLoader(
    private val templateRepository: TemplateRepository
) {
    
    suspend fun loadBuiltInTemplates() {
        // Check if built-in templates are already loaded
        val existingTemplates = templateRepository.getAllTemplates().first()
        val builtInTemplatesExist = existingTemplates.any { it.isBuiltIn }
        
        if (!builtInTemplatesExist) {
            // Load all built-in templates
            BuiltInTemplates.allTemplates.forEach { template ->
                templateRepository.insertTemplate(template)
            }
        }
    }
    
    suspend fun reloadBuiltInTemplates() {
        // Delete existing built-in templates
        val existingTemplates = templateRepository.getAllTemplates().first()
        existingTemplates
            .filter { it.isBuiltIn }
            .forEach { template ->
                templateRepository.deleteTemplate(template.id)
            }
        
        // Load fresh built-in templates
        BuiltInTemplates.allTemplates.forEach { template ->
            templateRepository.insertTemplate(template)
        }
    }
    
    suspend fun isTemplateDataInitialized(): Boolean {
        val existingTemplates = templateRepository.getAllTemplates().first()
        return existingTemplates.any { it.isBuiltIn }
    }
    
    suspend fun getTemplateCount(): Int {
        return templateRepository.getAllTemplates().first().size
    }
    
    suspend fun getBuiltInTemplateCount(): Int {
        return templateRepository.getAllTemplates().first().count { it.isBuiltIn }
    }
}