package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseConverterScreen() {
    var inputValue by remember { mutableStateOf("") }
    var fromBase by remember { mutableStateOf(10) }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val bases = listOf(2, 8, 10, 16)
    val baseNames = mapOf(
        2 to "二进制",
        8 to "八进制",
        10 to "十进制",
        16 to "十六进制"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "进制转换",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 输入区域
        Text(
            text = "输入数值",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = inputValue,
            onValueChange = { 
                inputValue = it 
                errorMessage = ""
            },
            placeholder = { Text("输入要转换的数值") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 来源进制选择
        Text(
            text = "选择输入数值的进制",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 进制名称显示区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bases.forEach { base ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = fromBase == base,
                        onClick = { 
                            fromBase = base 
                            errorMessage = ""
                        }
                    )
                    Text(
                        text = baseNames[base] ?: "${base}进制",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // 转换结果
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "转换结果",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                bases.forEach { base ->
                    if (base != fromBase) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = baseNames[base] ?: "${base}进制",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            
                            // 调整结果文本区域
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = try {
                                        if (inputValue.isBlank()) {
                                            ""
                                        } else {
                                            val decimalValue = when (fromBase) {
                                                10 -> inputValue.toLong()
                                                else -> inputValue.toLong(fromBase)
                                            }
                                            
                                            when (base) {
                                                10 -> decimalValue.toString()
                                                else -> decimalValue.toString(base).uppercase()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "无效输入，请检查数值是否符合${baseNames[fromBase]}格式"
                                        ""
                                    },
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        if (base != bases.last() && base != fromBase) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
} 