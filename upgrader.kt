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
import androidx.compose.foundation.clickable
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


@Composable
fun FourthScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var betInput by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }

    val winRecords = remember { mutableStateListOf<WinRecord>() }
    val coroutineScope = rememberCoroutineScope()
    // Было: Animatable(0f) -> Стало: Animatable(90f)
    val needleAngle = remember { Animatable(90f) }

    var winInput by remember { mutableStateOf("") }
    var winChance by remember { mutableStateOf(50) } // По умолчанию 50%


    // ЯРКАЯ 8-БИТНАЯ ПАЛИТРА И КАСТОМНЫЕ ЦВЕТА
    val darkBgGradient = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
    val bitGreenColor = Color(0xFF00FF00)   // Ядовито-зелёный 8-bit
    val bitRedColor = Color(0xFFFF0000)     // Чистый красный 8-bit
    val coffeeCenterColor = Color(0xFF4A3B32) // Кофейный цвет для центра круга
    val goldBorderColor = Color(0xFFD4AF37)   // Золотой цвет для обводки

    fun saveBalance(newBalance: Int) {
        sharedPreferences.edit().putInt("balance", newBalance).apply()
    }

    Box(modifier = Modifier.fillMaxSize().background(darkBgGradient)) {

        // 1. ВЕРХНЯЯ ПАНЕЛЬ: БАЛАНС С ПЛАВНОЙ СМЕНОЙ ЦИФР
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
            border = androidx.compose.foundation.BorderStroke(2.dp, goldBorderColor),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "БАЛАНС: ",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                // Добавили плавную вертикальную прокрутку цифр баланса
                AnimatedContent(
                    targetState = balance,
                    transitionSpec = {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    },
                    label = "BalanceAnimation"
                ) { animatedBalance ->
                    Text(
                        text = "$animatedBalance 💰",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 2. ЦЕНТР: КОЛЕСО АПГРЕЙДА (Сдвинуто чуть вверх, сектор центрирован снизу)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .offset(y = (-40).dp), // СДВИГ ВВЕРХ: Поднимаем колесо на 40dp, чтобы разгрузить нижнюю панель 🧭
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)

                // ВАЖНОЕ ИСПРАВЛЕНИЕ ГЕОМЕТРИИ:
                // Теперь радиус — это внутренний центр дорожки, чтобы цвета и обводки не вылезали наружу!
                val strokeWidth = 24.dp.toPx()
                val radius = size.width / 2

                val orangeRed8Bit = Color(0xFFFF4500)
                val darkLoseZone = Color(0xFF2D3748)
                val bgCenterColor = Color(0xFF16213E)
                val ringLineColor = Color(0xFF404040)

                val sweepAngle = 360f * (winChance / 100f)

                // МАТЕМАТИКА ПОВОРОТА: Вычисляем угол так, чтобы оранжевый сектор всегда был ПОВАРАЧЕН СТРОГО К НИЗУ
                // 90 градусов (низ экрана) минус половина размера самого сектора
                val startAngle = 90f - (sweepAngle / 2f)

                // СЛОЙ 1: Цветные дуги с динамическим стартовым углом
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

                // СЛОЙ 2: Внутренний круг цвета заднего фона
                val innerRadius = radius - (strokeWidth / 2)
                drawCircle(
                    color = bgCenterColor,
                    radius = innerRadius,
                    center = center
                )

                // СЛОЙ 3: Тёмная стрелка, летящая ПО цветам
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

                // СЛОЙ 4: Контурные обводки главного кольца
                drawCircle(
                    color = ringLineColor,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = ringLineColor,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // СЛОЙ 5: Третье декоративное кольцо контура снаружи
                drawCircle(
                    color = Color(0xFF555555),
                    radius = outerRadius + 14.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Text(
                text = "$winChance%",
                color = Color.White,
                fontSize = 60.sp,
                fontWeight = FontWeight.Black
            )
        }


        // 3. БЛОК УПРАВЛЕНИЯ СНИЗУ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ЛЕНТА ЛЕТЯЩИХ ВВЕРХ ВЫИГРЫШЕЙ
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
                                enter = slideInVertically { height -> height } + fadeIn(
                                    animationSpec = tween(300)
                                ),
                                exit = slideOutVertically { height -> -height } + fadeOut(
                                    animationSpec = tween(500)
                                )
                            ) {
                                Text(
                                    text = "+${record.amount} 💰",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. ОДИН ОБЩИЙ РЯД КНОПОК ДЛЯ УПРАВЛЕНИЯ ВЫИГРЫШЕМ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, // Равномерно распределяем все 6 кнопок в один ряд
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Список всех наших пресетов для выигрыша
                val presets = listOf("x3", "x4", "x8", "50%", "10%", "1%")

                presets.forEach { preset ->
                    Button(
                        onClick = {
                            val currentBet = betInput.toIntOrNull() ?: 0
                            if (currentBet > 0) {
                                when (preset) {
                                    // Кнопки-множители выигрыша
                                    "x3" -> {
                                        winInput = (currentBet * 3).toString()
                                        winChance = 33
                                    }

                                    "x4" -> {
                                        winInput = (currentBet * 4).toString()
                                        winChance = 25
                                    }

                                    "x8" -> {
                                        winInput = (currentBet * 8).toString()
                                        winChance = 12
                                    }
                                    // Кнопки фиксированных шансов (меняют выигрыш обратно пропорционально)
                                    "50%" -> {
                                        winInput = (currentBet * 2).toString()
                                        winChance = 50
                                    }

                                    "10%" -> {
                                        winInput = (currentBet * 10).toString()
                                        winChance = 10
                                    }

                                    "1%" -> {
                                        winInput = (currentBet * 100).toString()
                                        winChance = 1
                                    }
                                }
                            }
                        },
                        enabled = !isSpinning && betInput.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f) // Каждая кнопка получит равную ширину
                            .padding(horizontal = 2.dp)
                            .height(28.dp)
                    ) {
                        Text(text = preset, fontSize = 11.sp, color = Color.White, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ставка",
                        color = Color.Gray,
                        fontSize = 12.sp,
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
                                    // При ручном изменении ставки выигрыш изначально равен ей же
                                    winInput = num.toString()
                                    winChance = 95
                                }
                            }
                        },
                        placeholder = { Text("0", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F0C20),
                            unfocusedContainerColor = Color(0xFF0F0C20),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFFFF4500)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ПРАВОЕ ПОЛЕ: ВЫИГРЫШ (С ЗАЩИТОЙ ОТ ШАНСА МЕНЬШЕ 1%)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Выигрыш",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                    TextField(
                        value = winInput,
                        onValueChange = { input ->
                            if (input.length <= 7) {
                                val clean = input.filter { it.isDigit() }
                                val currentWin = clean.toIntOrNull()
                                val currentBet = betInput.toIntOrNull() ?: 0

                                if (currentWin != null && currentBet > 0) {
                                    val calculatedChance =
                                        ((currentBet.toFloat() / currentWin) * 100).toInt()

                                    // Если игрок руками вводит огромный выигрыш, срезаем его до 1% шанса
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F0C20),
                            unfocusedContainerColor = Color(0xFF0F0C20),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFFFF4500)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            val currentBet = betInput.toIntOrNull() ?: 0
            val isBetValid = currentBet > 0 && currentBet <= balance

            Button(
                onClick = {
                    if (!isSpinning && isBetValid) {
                        isSpinning = true
                        balance -= currentBet
                        saveBalance(balance)

                        // Запускаем ОДНУ корутину для всего процесса апгрейда
                        coroutineScope.launch {

                            // МАГИЧЕСКАЯ СТРОЧКА: Срезаем лишние обороты, оставляя стрелку ровно в той же точке, где она стояла!
                            needleAngle.snapTo(needleAngle.value % 360f)

                            // 1. Считаем угол оранжевого сектора и его смещение, чтобы он был снизу
                            val sweepAngle = 360f * (winChance / 100f)
                            val startAngle = 90f - (sweepAngle / 2f)

                            // 2. Честный ролл: генерируем случайное число от 1 до 100
                            val randomRoll = Random.nextInt(1, 101)
                            val isWin = randomRoll <= winChance

                            // 3. Выбираем случайный угол остановки с учётом поворота колеса вниз
                            val targetAngle = if (isWin) {
                                // Если выиграл — целимся строго внутрь оранжевого сектора (от его начала до его конца)
                                Random.nextInt(
                                    startAngle.toInt(),
                                    (startAngle + sweepAngle).toInt()
                                )
                            } else {
                                // Если проиграл — целимся в серую зону (от конца оранжевого сектора и дальше по кругу)
                                Random.nextInt(
                                    (startAngle + sweepAngle).toInt(),
                                    (startAngle + 360f).toInt()
                                )
                            }

                            // 4. Закручиваем стрелку на 7 полных оборотов вперед
                            val totalRotation = 2520f + targetAngle

                            // 5. Запускаем анимацию на 4 секунды с реалистичным замедлением в конце
                            needleAngle.animateTo(
                                targetValue = totalRotation,
                                animationSpec = tween(
                                    durationMillis = 4000,
                                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                )
                            )

                            // 6. Логика начисления монет при успешном апгрейде
                            if (isWin) {
                                // Берем сумму выигрыша прямо из правого текстового поля (или дефолт х2, если пусто)
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

                            // Если после прокрутки баланс стал меньше текущей ставки, сбрасываем поля
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
                    containerColor = Color(0xFFFF4500), // Поставили огненный оранжево-красный в тон колесу
                    disabledContainerColor = Color(0xFF4A1F10)
                ),
                modifier = Modifier.width(240.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSpinning) "АПГРЕЙД..." else "ЗАПУСТИТЬ АПГРЕЙД ⚡",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Назад в меню",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { if (!isSpinning) onBackToMenu() }
            )
        }
    }
}

