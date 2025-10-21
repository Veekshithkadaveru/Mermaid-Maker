package com.example.mermaidmaker.data.ai

enum class CodeLanguage {
    KOTLIN,
    JAVA,
    SQL,
    JAVASCRIPT,
    TYPESCRIPT,
    PYTHON,
    UNKNOWN
}

data class CodeAnalysisResult(
    val language: CodeLanguage,
    val structures: List<CodeStructure>,
    val relationships: List<CodeRelationship>,
    val suggestedDiagramType: String
)

data class CodeStructure(
    val name: String,
    val type: StructureType,
    val properties: List<String>,
    val methods: List<String>,
    val modifiers: List<String>
)

data class CodeRelationship(
    val from: String,
    val to: String,
    val type: RelationshipType,
    val label: String? = null
)

enum class StructureType {
    CLASS,
    INTERFACE,
    ENUM,
    FUNCTION,
    TABLE,
    FIELD,
    PROPERTY
}

enum class RelationshipType {
    INHERITANCE,
    IMPLEMENTATION,
    COMPOSITION,
    AGGREGATION,
    ASSOCIATION,
    DEPENDENCY,
    FOREIGN_KEY,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

object ContextualAnalyzer {
    
    fun analyzeCode(code: String, language: CodeLanguage? = null): CodeAnalysisResult {
        val detectedLanguage = language ?: detectLanguage(code)
        
        return when (detectedLanguage) {
            CodeLanguage.KOTLIN -> analyzeKotlinCode(code)
            CodeLanguage.JAVA -> analyzeJavaCode(code)
            CodeLanguage.SQL -> analyzeSqlSchema(code)
            CodeLanguage.JAVASCRIPT, CodeLanguage.TYPESCRIPT -> analyzeJavaScriptCode(code)
            CodeLanguage.PYTHON -> analyzePythonCode(code)
            CodeLanguage.UNKNOWN -> CodeAnalysisResult(
                language = detectedLanguage,
                structures = emptyList(),
                relationships = emptyList(),
                suggestedDiagramType = "flowchart"
            )
        }
    }
    
    private fun detectLanguage(code: String): CodeLanguage {
        val cleanCode = code.trim().lowercase()
        
        return when {
            cleanCode.contains("class ") && cleanCode.contains("fun ") -> CodeLanguage.KOTLIN
            cleanCode.contains("public class") || cleanCode.contains("private class") -> CodeLanguage.JAVA
            cleanCode.contains("create table") || cleanCode.contains("select ") -> CodeLanguage.SQL
            cleanCode.contains("function ") && cleanCode.contains("var ") -> CodeLanguage.JAVASCRIPT
            cleanCode.contains("interface ") && cleanCode.contains(": ") -> CodeLanguage.TYPESCRIPT
            cleanCode.contains("def ") && cleanCode.contains("class ") -> CodeLanguage.PYTHON
            else -> CodeLanguage.UNKNOWN
        }
    }
    
    private fun analyzeKotlinCode(code: String): CodeAnalysisResult {
        val structures = mutableListOf<CodeStructure>()
        val relationships = mutableListOf<CodeRelationship>()

        val classRegex = Regex("""((?:data\s+|sealed\s+|abstract\s+)?class|interface|object)\s+(\w+)(?:\s*:\s*([^{]+))?""")
        classRegex.findAll(code).forEach { match ->
            val classType = match.groupValues[1]
            val className = match.groupValues[2]
            val inheritance = match.groupValues[3].trim()
            
            val structureType = when {
                classType.contains("interface") -> StructureType.INTERFACE
                classType.contains("class") -> StructureType.CLASS
                else -> StructureType.CLASS
            }
            
            val properties = extractKotlinProperties(code, className)
            val methods = extractKotlinMethods(code, className)
            val modifiers = classType.split("\\s+".toRegex()).filter { it != "class" && it != "interface" }
            
            structures.add(CodeStructure(className, structureType, properties, methods, modifiers))
            
            // Handle inheritance relationships
            if (inheritance.isNotEmpty()) {
                inheritance.split(",").forEach { parent ->
                    val parentName = parent.trim().split("(")[0].trim()
                    if (parentName.isNotEmpty()) {
                        relationships.add(CodeRelationship(
                            from = className,
                            to = parentName,
                            type = if (structureType == StructureType.INTERFACE) RelationshipType.IMPLEMENTATION else RelationshipType.INHERITANCE
                        ))
                    }
                }
            }
        }
        
        // Extract functions
        val functionRegex = Regex("""fun\s+(\w+)\s*\([^)]*\)""")
        functionRegex.findAll(code).forEach { match ->
            val functionName = match.groupValues[1]
            structures.add(CodeStructure(functionName, StructureType.FUNCTION, emptyList(), emptyList(), emptyList()))
        }
        
        return CodeAnalysisResult(
            language = CodeLanguage.KOTLIN,
            structures = structures,
            relationships = relationships,
            suggestedDiagramType = if (structures.any { it.type == StructureType.CLASS || it.type == StructureType.INTERFACE }) "class" else "flowchart"
        )
    }
    
    private fun analyzeJavaCode(code: String): CodeAnalysisResult {
        val structures = mutableListOf<CodeStructure>()
        val relationships = mutableListOf<CodeRelationship>()
        
        // Extract classes and interfaces
        val classRegex = Regex("""(public\s+|private\s+)?(abstract\s+)?(class|interface)\s+(\w+)(?:\s+extends\s+(\w+))?(?:\s+implements\s+([^{]+))?""")
        classRegex.findAll(code).forEach { match ->
            val visibility = match.groupValues[1].trim()
            val isAbstract = match.groupValues[2].isNotEmpty()
            val type = match.groupValues[3]
            val className = match.groupValues[4]
            val extendsClass = match.groupValues[5]
            val implementsInterfaces = match.groupValues[6]
            
            val structureType = if (type == "interface") StructureType.INTERFACE else StructureType.CLASS
            val modifiers = mutableListOf<String>()
            if (visibility.isNotEmpty()) modifiers.add(visibility)
            if (isAbstract) modifiers.add("abstract")
            
            val properties = extractJavaProperties(code, className)
            val methods = extractJavaMethods(code, className)
            
            structures.add(CodeStructure(className, structureType, properties, methods, modifiers))
            
            // Handle inheritance
            if (extendsClass.isNotEmpty()) {
                relationships.add(CodeRelationship(className, extendsClass, RelationshipType.INHERITANCE))
            }
            
            // Handle implementations
            if (implementsInterfaces.isNotEmpty()) {
                implementsInterfaces.split(",").forEach { interfaceStr ->
                    val interfaceName = interfaceStr.trim()
                    if (interfaceName.isNotEmpty()) {
                        relationships.add(CodeRelationship(className, interfaceName, RelationshipType.IMPLEMENTATION))
                    }
                }
            }
        }
        
        return CodeAnalysisResult(
            language = CodeLanguage.JAVA,
            structures = structures,
            relationships = relationships,
            suggestedDiagramType = if (structures.any { it.type == StructureType.CLASS || it.type == StructureType.INTERFACE }) "class" else "flowchart"
        )
    }
    
    private fun analyzeSqlSchema(code: String): CodeAnalysisResult {
        val structures = mutableListOf<CodeStructure>()
        val relationships = mutableListOf<CodeRelationship>()
        
        // Extract tables
        val tableRegex = Regex("""CREATE\s+TABLE\s+(\w+)\s*\((.*?)\)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        tableRegex.findAll(code).forEach { match ->
            val tableName = match.groupValues[1]
            val tableDefinition = match.groupValues[2]
            
            val columns = extractSqlColumns(tableDefinition)
            val foreignKeys = extractSqlForeignKeys(tableDefinition)
            
            structures.add(CodeStructure(tableName, StructureType.TABLE, columns, emptyList(), emptyList()))
            
            // Handle foreign key relationships
            foreignKeys.forEach { (column, referencedTable) ->
                relationships.add(CodeRelationship(
                    from = tableName,
                    to = referencedTable,
                    type = RelationshipType.FOREIGN_KEY,
                    label = column
                ))
            }
        }
        
        return CodeAnalysisResult(
            language = CodeLanguage.SQL,
            structures = structures,
            relationships = relationships,
            suggestedDiagramType = "er"
        )
    }
    
    private fun analyzeJavaScriptCode(code: String): CodeAnalysisResult {
        val structures = mutableListOf<CodeStructure>()
        val relationships = mutableListOf<CodeRelationship>()
        
        // Extract functions
        val functionRegex = Regex("""(?:function\s+(\w+)|const\s+(\w+)\s*=.*?(?:function|\([^)]*\)\s*=>))|(?:(\w+)\s*:\s*(?:function|\([^)]*\)\s*=>))""")
        functionRegex.findAll(code).forEach { match ->
            val functionName = match.groupValues[1].ifEmpty { 
                match.groupValues[2].ifEmpty { match.groupValues[3] }
            }
            if (functionName.isNotEmpty()) {
                structures.add(CodeStructure(functionName, StructureType.FUNCTION, emptyList(), emptyList(), emptyList()))
            }
        }
        
        // Extract classes (ES6)
        val classRegex = Regex("""class\s+(\w+)(?:\s+extends\s+(\w+))?""")
        classRegex.findAll(code).forEach { match ->
            val className = match.groupValues[1]
            val extendsClass = match.groupValues[2]
            
            val methods = extractJavaScriptMethods(code, className)
            structures.add(CodeStructure(className, StructureType.CLASS, emptyList(), methods, emptyList()))
            
            if (extendsClass.isNotEmpty()) {
                relationships.add(CodeRelationship(className, extendsClass, RelationshipType.INHERITANCE))
            }
        }
        
        return CodeAnalysisResult(
            language = CodeLanguage.JAVASCRIPT,
            structures = structures,
            relationships = relationships,
            suggestedDiagramType = if (structures.any { it.type == StructureType.CLASS }) "class" else "flowchart"
        )
    }
    
    private fun analyzePythonCode(code: String): CodeAnalysisResult {
        val structures = mutableListOf<CodeStructure>()
        val relationships = mutableListOf<CodeRelationship>()
        
        // Extract classes
        val classRegex = Regex("""class\s+(\w+)(?:\(([^)]+)\))?:""")
        classRegex.findAll(code).forEach { match ->
            val className = match.groupValues[1]
            val parentClasses = match.groupValues[2]
            
            val methods = extractPythonMethods(code, className)
            structures.add(CodeStructure(className, StructureType.CLASS, emptyList(), methods, emptyList()))
            
            // Handle inheritance
            if (parentClasses.isNotEmpty()) {
                parentClasses.split(",").forEach { parent ->
                    val parentName = parent.trim()
                    if (parentName.isNotEmpty()) {
                        relationships.add(CodeRelationship(className, parentName, RelationshipType.INHERITANCE))
                    }
                }
            }
        }
        
        // Extract functions
        val functionRegex = Regex("""def\s+(\w+)\s*\([^)]*\):""")
        functionRegex.findAll(code).forEach { match ->
            val functionName = match.groupValues[1]
            if (!functionName.startsWith("__")) { // Skip magic methods
                structures.add(CodeStructure(functionName, StructureType.FUNCTION, emptyList(), emptyList(), emptyList()))
            }
        }
        
        return CodeAnalysisResult(
            language = CodeLanguage.PYTHON,
            structures = structures,
            relationships = relationships,
            suggestedDiagramType = if (structures.any { it.type == StructureType.CLASS }) "class" else "flowchart"
        )
    }
    
    // Helper methods for extracting properties and methods
    private fun extractKotlinProperties(code: String, className: String): List<String> {
        val propertyRegex = Regex("""(?:val|var)\s+(\w+)\s*:\s*([^=\n]+)""")
        return propertyRegex.findAll(code).map { "${it.groupValues[1]}: ${it.groupValues[2].trim()}" }.toList()
    }
    
    private fun extractKotlinMethods(code: String, className: String): List<String> {
        val methodRegex = Regex("""fun\s+(\w+)\s*\([^)]*\)(?:\s*:\s*([^{=\n]+))?""")
        return methodRegex.findAll(code).map { 
            val returnType = it.groupValues[2].ifEmpty { "Unit" }
            "${it.groupValues[1]}(): $returnType"
        }.toList()
    }
    
    private fun extractJavaProperties(code: String, className: String): List<String> {
        val fieldRegex = Regex("""(?:private|public|protected)?\s*([A-Za-z_]\w*)\s+(\w+);""")
        return fieldRegex.findAll(code).map { "${it.groupValues[2]}: ${it.groupValues[1]}" }.toList()
    }
    
    private fun extractJavaMethods(code: String, className: String): List<String> {
        val methodRegex = Regex("""(?:public|private|protected)?\s*([A-Za-z_]\w*)\s+(\w+)\s*\([^)]*\)""")
        return methodRegex.findAll(code).map { "${it.groupValues[2]}(): ${it.groupValues[1]}" }.toList()
    }
    
    private fun extractSqlColumns(tableDefinition: String): List<String> {
        val lines = tableDefinition.split(",")
        return lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (!trimmed.uppercase().startsWith("FOREIGN") && 
                !trimmed.uppercase().startsWith("PRIMARY") && 
                !trimmed.uppercase().startsWith("KEY") &&
                !trimmed.uppercase().startsWith("CONSTRAINT")) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) "${parts[0]} ${parts[1]}" else null
            } else null
        }
    }
    
    private fun extractSqlForeignKeys(tableDefinition: String): List<Pair<String, String>> {
        val foreignKeys = mutableListOf<Pair<String, String>>()
        val fkRegex = Regex("""FOREIGN\s+KEY\s*\(\s*(\w+)\s*\)\s+REFERENCES\s+(\w+)""", RegexOption.IGNORE_CASE)
        fkRegex.findAll(tableDefinition).forEach { match ->
            foreignKeys.add(match.groupValues[1] to match.groupValues[2])
        }
        return foreignKeys
    }
    
    private fun extractJavaScriptMethods(code: String, className: String): List<String> {
        val methodRegex = Regex("""(\w+)\s*\([^)]*\)\s*\{""")
        return methodRegex.findAll(code).map { "${it.groupValues[1]}()" }.toList()
    }
    
    private fun extractPythonMethods(code: String, className: String): List<String> {
        val methodRegex = Regex("""def\s+(\w+)\s*\([^)]*\):""")
        return methodRegex.findAll(code).map { "${it.groupValues[1]}()" }.toList()
    }
}