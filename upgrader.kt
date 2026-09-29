package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ===== Общие цвета — те же, что в главном меню =====
private val FsDarkCardBg = Color(0xFF0F0C20)
private val FsGoldAccent = Color(0xFFFFD700)
private val FsNeonCyan = Color(0xFF00E5FF)
private val FsNeonOrange = Color(0xFFFF4500)
private val FsNeonGreen = Color(0xFF00FF7F)
private val FsNeonPurple = Color(0xFFD67BFF)

@Composable
fun FourthScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    // Глобальные счётчики
    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    val freeSpins = remember { sharedPreferences.getInt("free_spins", 0) }

    var betInput by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }

    val winRecords = remember { mutableStateListOf<WinRecord>() }
    val coroutineScope = rememberCoroutineScope()
    val needleAngle = remember { Animatable(90f) }

    var winInput by remember { mutableStateOf("") }
    var winChance by remember { mutableStateOf(50) }

    // Выбранный пресет, чтобы кнопка подсвечивалась и влияла на ввод
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    // Отслеживаем фокус на поле "Выигрыш" — при фокусе сбрасываем пресет
    var winFieldFocused by remember { mutableStateOf(false) }

    val darkBgGradient = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))

    fun saveBalance(newBalance: Int) {
        sharedPreferences.edit().putInt("balance", newBalance).apply()
    }

    // Функция применения пресета к текущей ставке
    fun applyPresetToBet(preset: String) {
        val currentBet = betInput.toIntOrNull() ?: return
        if (currentBet <= 0) return
        when (preset) {
            "x3" -> {
                winInput = (currentBet * 3).toString()
                winChance = 33
            }
            "x4" -> {
                winInput = (currentBet * 4).toString()
                winChance = 25
            }
            // 70% — целочисленно, без float
            "70%" -> {
                winInput = (currentBet * 100 / 70).toString()
                winChance = 70
            }
            "50%" -> {
                winInput = (currentBet * 2).toString()
                winChance = 50
            }
            "10%" -> {
                winInput = (currentBet * 10).toString()
                winChance = 10
            }
            "100x" -> {
                winInput = (currentBet * 100).toString()
                winChance = 1
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(darkBgGradient)) {

        // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
        TopBarCasinoFourth(
            balance = balance,
            freeSpins = freeSpins,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        )

        // ===== ЦЕНТР: КОЛЕСО АПГРЕЙДА =====
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .offset(y = (-60).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val strokeWidth = 24.dp.toPx()
                val radius = size.width / 2

                val orangeRed8Bit = Color(0xFFFF4500)
                val darkLoseZone = Color(0xFF2D3748)
                val bgCenterColor = Color(0xFF16213E)
                val ringLineColor = Color(0xFF404040)

                val sweepAngle = 360f * (winChance / 100f)
                val startAngle = 90f - (sweepAngle / 2f)

                drawArc(
                    color = orangeRed8Bit,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = darkLoseZone,
                    startAngle = startAngle + sweepAngle,
                    sweepAngle = 360f - sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                val innerRadius = radius - (strokeWidth / 2)
                drawCircle(color = bgCenterColor, radius = innerRadius, center = center)

                val angleInRadians = (needleAngle.value * PI / 180f)
                val startX = center.x + innerRadius * cos(angleInRadians).toFloat()
                val startY = center.y + innerRadius * sin(angleInRadians).toFloat()
                val outerRadius = radius + (strokeWidth / 2)
                val endX = center.x + outerRadius * cos(angleInRadians).toFloat()
                val endY = center.y + outerRadius * sin(angleInRadians).toFloat()

                drawLine(
                    color = Color(0xFF1A0F0A),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 6.dp.toPx()
                )

                drawCircle(color = ringLineColor, radius = outerRadius, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(color = ringLineColor, radius = innerRadius, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(color = Color(0xFF555555), radius = outerRadius + 14.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
            }

            Text(
                text = "$winChance%",
                color = Color.White,
                fontSize = 60.sp,
                fontWeight = FontWeight.Black
            )
        }

        // ===== НИЖНЯЯ КОРОБКА УПРАВЛЕНИЯ =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.height(50.dp).fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    winRecords.forEach { record ->
                        key(record.id) {
                            AnimatedVisibility(
                                visible = record.isVisible,
                                enter = slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(500))
                            ) {
                                Text(
                                    text = "+${record.amount} 💰",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FsGoldAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FsDarkCardBg),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(FsGoldAccent, Color(0xFFFFA751))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "⚡ АПГРЕЙД СТАВКИ",
                        color = FsGoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // === РЯД ПРЕСЕТОВ (новый порядок) ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Порядок: x3, x4, 100x, 70%, 50%, 10%
                        val presets = listOf("x3", "x4", "100x", "70%", "50%", "10%")

                        presets.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            val accent = when (preset) {
                                "x3" -> FsNeonCyan
                                "x4" -> FsNeonGreen
                                "100x" -> FsGoldAccent
                                "70%" -> FsNeonPurple
                                "50%" -> FsNeonOrange
                                "10%" -> FsNeonCyan
                                else -> FsNeonCyan
                            }
                            Button(
                                onClick = {
                                    if (selectedPreset == preset) {
                                        selectedPreset = null
                                    } else {
                                        selectedPreset = preset
                                        if (betInput.isNotEmpty()) applyPresetToBet(preset)
                                    }
                                },
                                enabled = !isSpinning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) accent.copy(alpha = 0.22f) else Color(0xFF1A1730),
                                    disabledContainerColor = Color(0xFF1A1730)
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accent else Color(0xFF3A3F58),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = if (isSelected) accent else Color.White,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // === СТАВКА И ВЫИГРЫШ ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ставка",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                            TextField(
                                value = betInput,
                                onValueChange = { input ->
                                    if (input.length <= 6) {
                                        val clean = input.filter { it.isDigit() }
                                        betInput = clean

                                        val num = clean.toIntOrNull()
                                        if (num == null) {
                                            winInput = ""
                                        } else {
                                            if (selectedPreset != null) {
                                                applyPresetToBet(selectedPreset!!)
                                            } else {
                                                winInput = num.toString()
                                                winChance = 95
                                            }
                                        }
                                    }
                                },
                                placeholder = { Text("0", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1A1730),
                                    unfocusedContainerColor = Color(0xFF1A1730),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = FsNeonOrange,
                                    unfocusedIndicatorColor = Color(0xFF3A3F58)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Выигрыш",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                            TextField(
                                value = winInput,
                                onValueChange = { input ->
                                    // Ручной ввод всегда сбрасывает пресет
                                    if (selectedPreset != null) selectedPreset = null

                                    if (input.length <= 7) {
                                        val clean = input.filter { it.isDigit() }
                                        val currentWin = clean.toIntOrNull()
                                        val currentBet = betInput.toIntOrNull() ?: 0

                                        if (currentWin != null && currentBet > 0) {
                                            val calculatedChance =
                                                ((currentBet.toFloat() / currentWin) * 100).toInt()

                                            if (calculatedChance < 1) {
                                                val maxPossibleWin = currentBet * 100
                                                winInput = maxPossibleWin.toString()
                                                winChance = 1
                                            } else {
                                                winInput = clean
                                                winChance = calculatedChance.coerceIn(1, 95)
                                            }
                                        } else {
                                            winInput = clean
                                        }
                                    }
                                },
                                placeholder = { Text("0", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                // ПУНКТ 1: при касании поля "Выигрыш" — сбрасываем активный пресет
                                modifier = Modifier.onFocusChanged { focusState ->
                                    if (focusState.isFocused && !winFieldFocused) {
                                        selectedPreset = null
                                    }
                                    winFieldFocused = focusState.isFocused
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1A1730),
                                    unfocusedContainerColor = Color(0xFF1A1730),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = FsNeonGreen,
                                    unfocusedIndicatorColor = Color(0xFF3A3F58)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val currentBet = betInput.toIntOrNull() ?: 0
                    val isBetValid = currentBet > 0 && currentBet <= balance

                    Button(
                        onClick = {
                            if (!isSpinning && isBetValid) {
                                isSpinning = true
                                balance -= currentBet
                                saveBalance(balance)

                                coroutineScope.launch {
                                    needleAngle.snapTo(needleAngle.value % 360f)

                                    val sweepAngle = 360f * (winChance / 100f)
                                    val startAngle = 90f - (sweepAngle / 2f)

                                    val randomRoll = Random.nextInt(1, 101)
                                    val isWin = randomRoll <= winChance

                                    val targetAngle = if (isWin) {
                                        Random.nextInt(
                                            startAngle.toInt(),
                                            (startAngle + sweepAngle).toInt()
                                        )
                                    } else {
                                        Random.nextInt(
                                            (startAngle + sweepAngle).toInt(),
                                            (startAngle + 360f).toInt()
                                        )
                                    }

                                    val totalRotation = 2520f + targetAngle

                                    needleAngle.animateTo(
                                        targetValue = totalRotation,
                                        animationSpec = tween(
                                            durationMillis = 4000,
                                            easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                        )
                                    )

                                    if (isWin) {
                                        val winAmount = winInput.toIntOrNull() ?: (currentBet * 2)
                                        balance += winAmount

                                        val newRecord = WinRecord(
                                            id = System.currentTimeMillis(),
                                            amount = winAmount,
                                            isVisibleState = mutableStateOf(true)
                                        )
                                        winRecords.add(newRecord)

                                        launch {
                                            kotlinx.coroutines.delay(2000)
                                            newRecord.isVisible = false
                                            kotlinx.coroutines.delay(500)
                                            winRecords.remove(newRecord)
                                        }
                                    }

                                    if ((betInput.toIntOrNull() ?: 0) > balance) {
                                        betInput = ""
                                        winInput = ""
                                    }

                                    saveBalance(balance)
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = !isSpinning && isBetValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FsNeonOrange,
                            disabledContainerColor = Color(0xFF4A1F10)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSpinning) "АПГРЕЙД..." else "ЗАПУСТИТЬ АПГРЕЙД ⚡",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Назад в меню",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { if (!isSpinning) onBackToMenu() }
                    )
                }
            }
        }
    }
}

// ===== Локальная копия TopBar — 1:1 как в MenuScreen =====
@Composable
private fun TopBarCasinoFourth(
    balance: Int,
    freeSpins: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FsDarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(FsGoldAccent, Color(0xFFFFA751)))
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🎰", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mysor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = FsGoldAccent
            )
            Spacer(modifier = Modifier.weight(1f))
            StatChipFourth(emoji = "💰", value = balance.toString(), accent = FsGoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            StatChipFourth(emoji = "🎁", value = freeSpins.toString(), accent = FsNeonCyan)
        }
    }
}

@Composable
private fun StatChipFourth(emoji: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF1A1730))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}