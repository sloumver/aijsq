package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.objecthunter.exp4j.ExpressionBuilder
import java.text.DecimalFormat

@Composable
fun BasicCalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val decimalFormat = remember { DecimalFormat("#.########") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 显示区域
        DisplayArea(
            expression = expression,
            result = result,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 按钮区域
        CalculatorButtonGrid(
            onNumberClick = { number ->
                expression += number
            },
            onOperatorClick = { operator ->
                if (operator == "+/-") {
                    // 处理数字正负号转换
                    if (expression.isNotEmpty()) {
                        // 简单处理：在前面添加或移除负号
                        if (expression.startsWith("-")) {
                            expression = expression.substring(1)
                        } else {
                            expression = "-$expression"
                        }
                    }
                } else {
                    expression += " $operator "
                }
            },
            onClearClick = {
                expression = ""
                result = ""
            },
            onBackspaceClick = {
                if (expression.isNotEmpty()) {
                    expression = if (expression.endsWith(" ")) {
                        expression.dropLast(3)
                    } else {
                        expression.dropLast(1)
                    }
                }
            },
            onEqualsClick = {
                if (expression.isNotEmpty()) {
                    try {
                        val expressionEvaluator = ExpressionBuilder(
                            expression
                                .replace("×", "*")
                                .replace("÷", "/")
                        ).build()
                        val value = expressionEvaluator.evaluate()
                        result = decimalFormat.format(value)
                    } catch (e: Exception) {
                        result = "错误"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )
    }
}

@Composable
fun DisplayArea(
    expression: String,
    result: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expression,
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = result,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CalculatorButtonGrid(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onClearClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onEqualsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttons = listOf(
        listOf("C", "+/-", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "DEL", "=")
    )
    
    Column(modifier = modifier.fillMaxSize()) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { button ->
                    CalculatorButton(
                        text = button,
                        onClick = {
                            when (button) {
                                "C" -> onClearClick()
                                "DEL" -> onBackspaceClick()
                                "=" -> onEqualsClick()
                                "+/-" -> onOperatorClick("+/-")
                                in listOf("+", "-", "×", "÷", "%") -> onOperatorClick(button)
                                else -> onNumberClick(button)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOperator = text in listOf("+", "-", "×", "÷", "%", "=", "( )")
    val isFunction = text in listOf("C", "DEL")
    
    val buttonColor = when {
        isFunction -> MaterialTheme.colorScheme.error
        isOperator -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val textColor = when {
        isFunction -> MaterialTheme.colorScheme.onError
        isOperator -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(buttonColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Backspace",
                tint = textColor
            )
        } else {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
} 