package com.example.mybudgetbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    onEqualClick: () -> Unit,
) {
    val buttons = listOf(
        listOf("AC", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("()", "0", ".", "=")
    )

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(8.dp)) {
        buttons.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { symbol ->
                    val isOperator = symbol in listOf("÷", "×", "-", "+", "=")
                    val isAction = symbol in listOf("AC", "⌫", "%", "()")
                    
                    Box(
                        modifier = Modifier
                            .weight(1f).aspectRatio(if (symbol == "=") 1.2f else 1f).padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    symbol == "=" -> Color(0xFF4CAF50)
                                    isOperator -> Color(0xFF3E5A46)
                                    isAction -> Color(0xFF4A4A4A)
                                    else -> Color(0xFF333333)
                                }
                            )
                            .clickable {
                                when (symbol) {
                                    "⌫" -> onDeleteClick()
                                    "AC" -> onClearClick()
                                    "=" -> onEqualClick()
                                    "÷" -> onNumberClick("/")
                                    "×" -> onNumberClick("*")
                                    "-" -> onNumberClick("-")
                                    "+" -> onNumberClick("+")
                                    else -> onNumberClick(symbol)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = symbol, color = if (symbol == "=") Color.White else Color(0xFFE0E0E0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
