package com.example.mermaidmaker.data.processing

fun String.escapeForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}


fun String.unescapeFromJs(): String {
    return this
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\`", "`")
        .replace("\\$", "$")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
        // Handle Unicode escapes that might be in the SVG
        .replace(Regex("\\\\u([0-9a-fA-F]{4})")) { matchResult ->
            val hexValue = matchResult.groupValues[1]
            try {
                Integer.parseInt(hexValue, 16).toChar().toString()
            } catch (_: NumberFormatException) {
                matchResult.value
            }
        }
}


