package com.example.mermaidmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ClickableExample() {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.Gray)
            .clickable() { println("Box clicked") }
    ) {
        Text(
            text = "Hello Compose",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .clickable { println("Text clicked") }
        )
    }

}


@Preview
@Composable
fun WeightVsFillPreview() {
    ClickableExample()
}