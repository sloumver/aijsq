package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

enum class GeometryShape(val displayName: String) {
    RECTANGLE("矩形"),
    SQUARE("正方形"),
    CIRCLE("圆形"),
    TRIANGLE("三角形"),
    TRAPEZOID("梯形"),
    POLYGON("正多边形")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeometryCalculatorScreen() {
    var selectedShape by remember { mutableStateOf(GeometryShape.RECTANGLE) }
    var result by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "几何图形面积计算",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 图形选择
        ShapeSelector(selectedShape) { selectedShape = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 不同图形的输入区域
        when (selectedShape) {
            GeometryShape.RECTANGLE -> RectangleInputs { result = it }
            GeometryShape.SQUARE -> SquareInputs { result = it }
            GeometryShape.CIRCLE -> CircleInputs { result = it }
            GeometryShape.TRIANGLE -> TriangleInputs { result = it }
            GeometryShape.TRAPEZOID -> TrapezoidInputs { result = it }
            GeometryShape.POLYGON -> PolygonInputs { result = it }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 结果显示
        if (result.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "面积",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Text(
                        text = result,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShapeSelector(
    selectedShape: GeometryShape,
    onShapeSelected: (GeometryShape) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeometryShape.values().take(3).forEach { shape ->
            FilterChip(
                selected = selectedShape == shape,
                onClick = { onShapeSelected(shape) },
                label = { Text(shape.displayName) },
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeometryShape.values().drop(3).forEach { shape ->
            FilterChip(
                selected = selectedShape == shape,
                onClick = { onShapeSelected(shape) },
                label = { Text(shape.displayName) },
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun RectangleInputs(onCalculate: (String) -> Unit) {
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    
    InputField(value = length, onValueChange = { length = it }, label = "长度")
    InputField(value = width, onValueChange = { width = it }, label = "宽度")
    
    CalculateButton(onClick = {
        val l = length.toDoubleOrNull()
        val w = width.toDoubleOrNull()
        
        if (l != null && w != null && l > 0 && w > 0) {
            val area = l * w
            onCalculate(String.format("%.2f 平方单位", area))
        } else {
            onCalculate("请输入有效的正数值")
        }
    })
}

@Composable
fun SquareInputs(onCalculate: (String) -> Unit) {
    var side by remember { mutableStateOf("") }
    
    InputField(value = side, onValueChange = { side = it }, label = "边长")
    
    CalculateButton(onClick = {
        val s = side.toDoubleOrNull()
        
        if (s != null && s > 0) {
            val area = s * s
            onCalculate(String.format("%.2f 平方单位", area))
        } else {
            onCalculate("请输入有效的正数值")
        }
    })
}

@Composable
fun CircleInputs(onCalculate: (String) -> Unit) {
    var radius by remember { mutableStateOf("") }
    
    InputField(value = radius, onValueChange = { radius = it }, label = "半径")
    
    CalculateButton(onClick = {
        val r = radius.toDoubleOrNull()
        
        if (r != null && r > 0) {
            val area = PI * r * r
            onCalculate(String.format("%.2f 平方单位", area))
        } else {
            onCalculate("请输入有效的正数值")
        }
    })
}

@Composable
fun TriangleInputs(onCalculate: (String) -> Unit) {
    var method by remember { mutableStateOf("底高") }
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    
    // 计算方式选择
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        FilterChip(
            selected = method == "底高",
            onClick = { method = "底高" },
            label = { Text("底边和高") },
            modifier = Modifier.padding(end = 8.dp)
        )
        
        FilterChip(
            selected = method == "三边",
            onClick = { method = "三边" },
            label = { Text("三边长") }
        )
    }
    
    if (method == "底高") {
        InputField(value = a, onValueChange = { a = it }, label = "底边长")
        InputField(value = height, onValueChange = { height = it }, label = "高")
        
        CalculateButton(onClick = {
            val base = a.toDoubleOrNull()
            val h = height.toDoubleOrNull()
            
            if (base != null && h != null && base > 0 && h > 0) {
                val area = 0.5 * base * h
                onCalculate(String.format("%.2f 平方单位", area))
            } else {
                onCalculate("请输入有效的正数值")
            }
        })
    } else {
        InputField(value = a, onValueChange = { a = it }, label = "边长a")
        InputField(value = b, onValueChange = { b = it }, label = "边长b")
        InputField(value = c, onValueChange = { c = it }, label = "边长c")
        
        CalculateButton(onClick = {
            val sideA = a.toDoubleOrNull()
            val sideB = b.toDoubleOrNull()
            val sideC = c.toDoubleOrNull()
            
            if (sideA != null && sideB != null && sideC != null && 
                sideA > 0 && sideB > 0 && sideC > 0) {
                // 检验三角形是否合法
                if (sideA + sideB > sideC && sideA + sideC > sideB && sideB + sideC > sideA) {
                    // 海伦公式
                    val s = (sideA + sideB + sideC) / 2
                    val area = sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))
                    onCalculate(String.format("%.2f 平方单位", area))
                } else {
                    onCalculate("三边长不能构成三角形")
                }
            } else {
                onCalculate("请输入有效的正数值")
            }
        })
    }
}

@Composable
fun TrapezoidInputs(onCalculate: (String) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    
    InputField(value = a, onValueChange = { a = it }, label = "上底长")
    InputField(value = b, onValueChange = { b = it }, label = "下底长")
    InputField(value = height, onValueChange = { height = it }, label = "高")
    
    CalculateButton(onClick = {
        val topBase = a.toDoubleOrNull()
        val bottomBase = b.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        
        if (topBase != null && bottomBase != null && h != null && 
            topBase > 0 && bottomBase > 0 && h > 0) {
            val area = 0.5 * (topBase + bottomBase) * h
            onCalculate(String.format("%.2f 平方单位", area))
        } else {
            onCalculate("请输入有效的正数值")
        }
    })
}

@Composable
fun PolygonInputs(onCalculate: (String) -> Unit) {
    var sides by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    
    InputField(value = sides, onValueChange = { sides = it }, label = "边数")
    InputField(value = length, onValueChange = { length = it }, label = "边长")
    
    CalculateButton(onClick = {
        val n = sides.toIntOrNull()
        val l = length.toDoubleOrNull()
        
        if (n != null && l != null && n >= 3 && l > 0) {
            // 正多边形面积公式: area = (n * s^2) / (4 * tan(π/n))
            val area = (n * l.pow(2)) / (4 * Math.tan(PI / n))
            onCalculate(String.format("%.2f 平方单位", area))
        } else {
            onCalculate("请输入有效值 (边数 ≥ 3, 边长 > 0)")
        }
    })
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun CalculateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text("计算面积")
    }
} 