package com.example.myapplication.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import net.objecthunter.exp4j.ExpressionBuilder

@Composable
fun FunctionPlotterScreen() {
    var expression by remember { mutableStateOf("x^2") }
    var xMin by remember { mutableStateOf("-10") }
    var xMax by remember { mutableStateOf("10") }
    var points by remember { mutableStateOf(100) }
    var errorMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 输入区域
        OutlinedTextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("函数表达式") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = xMin,
                onValueChange = { xMin = it },
                label = { Text("X最小值") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            
            OutlinedTextField(
                value = xMax,
                onValueChange = { xMax = it },
                label = { Text("X最大值") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
        
        Button(
            onClick = {
                try {
                    val minX = xMin.toDouble()
                    val maxX = xMax.toDouble()
                    if (minX >= maxX) {
                        errorMessage = "X最小值必须小于X最大值"
                    } else {
                        errorMessage = ""
                        points = 100 // 重置点数
                    }
                } catch (e: Exception) {
                    errorMessage = "请输入有效的数字范围"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("绘制函数图像")
        }
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // 图表区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        description.isEnabled = false
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)
                        
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    try {
                        val minX = xMin.toDoubleOrNull() ?: -10.0
                        val maxX = xMax.toDoubleOrNull() ?: 10.0
                        
                        if (minX < maxX) {
                            val entries = ArrayList<Entry>()
                            val step = (maxX - minX) / points
                            
                            for (i in 0..points) {
                                val x = minX + i * step
                                try {
                                    // 预处理表达式，确保幂运算的优先级正确
                                    val processedExpression = expression
                                        .replace("x^", "(x)^") // 将x^替换为(x)^
                                        .replace("x", "$x")
                                    
                                    val expressionObj = ExpressionBuilder(processedExpression)
                                        .variables("x")
                                        .build()
                                    expressionObj.setVariable("x", x)
                                    val y = expressionObj.evaluate().toFloat()
                                    
                                    if (!y.isNaN() && !y.isInfinite() && y > -1000 && y < 1000) {
                                        entries.add(Entry(x.toFloat(), y))
                                    }
                                } catch (e: Exception) {
                                    // Skip invalid points
                                }
                            }
                            
                            val dataSet = LineDataSet(entries, expression).apply {
                                color = Color.BLUE
                                setDrawCircles(false)
                                lineWidth = 2f
                                setDrawValues(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                            }
                            
                            chart.data = LineData(dataSet)
                            chart.invalidate()
                        }
                    } catch (e: Exception) {
                        // Error handling
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 