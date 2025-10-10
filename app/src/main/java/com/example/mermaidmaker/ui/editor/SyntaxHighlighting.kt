package com.example.mermaidmaker.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun applySyntaxHighlighting(text: String): AnnotatedString {
    val keywordColor = Color(0xFF9C27B0) // Purple
    val nodeColor = Color(0xFF2196F3) // Blue
    val arrowColor = Color(0xFF00BCD4) // Cyan
    val stringColor = Color(0xFF4CAF50) // Green
    val commentColor = Color(0xFF757575) // Gray

    return buildAnnotatedString {
        val lines = text.split('\n')

        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append('\n')

            if (line.trim().startsWith("%%")) {
                withStyle(SpanStyle(color = commentColor)) {
                    append(line)
                }
                return@forEachIndexed
            }

            var currentIndex = 0
            val trimmedLine = line.trim()

            val keywords = listOf(
                "graph", "flowchart", "sequenceDiagram", "classDiagram",
                "stateDiagram", "erDiagram", "journey", "gantt", "pie",
                "gitgraph", "TD", "TB", "BT", "RL", "LR"
            )

            keywords.forEach { keyword ->
                if (trimmedLine.startsWith(keyword)) {
                    val leadingSpaces = line.indexOf(keyword)
                    append(line.substring(0, leadingSpaces))
                    withStyle(SpanStyle(color = keywordColor)) {
                        append(keyword)
                    }
                    currentIndex = leadingSpaces + keyword.length
                }
            }

            if (currentIndex == 0) {
                var i = 0
                while (i < line.length) {
                    when {
                        line.substring(i).startsWith("-->") -> {
                            withStyle(SpanStyle(color = arrowColor)) { append("-->") }
                            i += 3
                        }
                        line.substring(i).startsWith("->") -> {
                            withStyle(SpanStyle(color = arrowColor)) { append("->") }
                            i += 2
                        }
                        line.substring(i).startsWith("->>") -> {
                            withStyle(SpanStyle(color = arrowColor)) { append("->>") }
                            i += 3
                        }
                        line.substring(i).startsWith("-->>") -> {
                            withStyle(SpanStyle(color = arrowColor)) { append("-->>") }
                            i += 4
                        }
                        line[i] in listOf('[', ']', '(', ')', '{', '}', '<', '>') -> {
                            withStyle(SpanStyle(color = arrowColor)) { append(line[i]) }
                            i++
                        }
                        line[i] == '"' -> {
                            val endQuote = line.indexOf('"', i + 1)
                            if (endQuote != -1) {
                                withStyle(SpanStyle(color = stringColor)) { append(line.substring(i, endQuote + 1)) }
                                i = endQuote + 1
                            } else {
                                append(line[i])
                                i++
                            }
                        }
                        line[i].isLetter() -> {
                            val nodeStart = i
                            while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
                            if (i < line.length && line[i] in listOf('[', '(', '{', '-', ' ')) {
                                withStyle(SpanStyle(color = nodeColor)) { append(line.substring(nodeStart, i)) }
                            } else {
                                append(line.substring(nodeStart, i))
                            }
                        }
                        else -> {
                            append(line[i])
                            i++
                        }
                    }
                }
            } else {
                append(line.substring(currentIndex))
            }
        }
    }
}


