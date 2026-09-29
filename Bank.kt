package com.example.myfirstapp

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ===== Общие цвета/константы (1:1 как в главном меню) =====
private val BkDarkCardBg = Color(0xFF0F0C20)
private val BkGoldAccent = Color(0xFFFFD700)
private val BkNeonCyan = Color(0xFF00E5FF)

@Composable
fun Bank(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
    }

    // Проверяем, не капает ли еще КД от промокода 666
    val bannedUntil = sharedPreferences.getLong("bank_banned_until", 0L)
    val isBanned = System.currentTimeMillis() < bannedUntil

    if (isBanned) {
        val timeLeftMinutes = ((bannedUntil - System.currentTimeMillis()) / 60000L) + 1
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            TopBarCasinoBank(
                balance = sharedPreferences.getInt("balance", 100),
                freeSpins = sharedPreferences.getInt("free_spins", 0)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🔒 БАНК ЗАБЛОКИРОВАН",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF3333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Системы банка перезагружаются после кода 666.\nДоступ восстановится через: $timeLeftMinutes мин.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Назад в меню",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onBackToMenu() }
                )
            }
        }
    } else {
        var isBankUnlocked by remember {
            mutableStateOf(sharedPreferences.getBoolean("bank_unlocked", false))
        }

        Crossfade(targetState = isBankUnlocked, label = "BankScreenTransition") { unlocked ->
            if (!unlocked) {
                BankAuthScreen(
                    sharedPreferences = sharedPreferences,
                    onAccessGranted = {
                        sharedPreferences.edit().putBoolean("bank_unlocked", true).apply()
                        isBankUnlocked = true
                    },
                    onBackToMenu = onBackToMenu
                )
            } else {
                ActiveBankScreen(sharedPreferences = sharedPreferences, onBackToMenu = onBackToMenu)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAuthScreen(
    sharedPreferences: android.content.SharedPreferences,
    onAccessGranted: () -> Unit,
    onBackToMenu: () -> Unit
) {
    var pinCode by remember { mutableStateOf("") }
    val neonPurple = Color(0xFFD67BFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
        TopBarCasinoBank(
            balance = sharedPreferences.getInt("balance", 100),
            freeSpins = sharedPreferences.getInt("free_spins", 0)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "КОД",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Секретный код для доступа к сейфу банка\nможно найти в нашем ТГК проекта! 📢",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = pinCode,
                onValueChange = { input ->
                    if (input.length <= 3 && input.all { it.isDigit() }) {
                        pinCode = input
                        if (input == "450") {
                            onAccessGranted()
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "XXX",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = neonPurple,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = BkDarkCardBg,
                    unfocusedContainerColor = BkDarkCardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(160.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Назад в меню",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onBackToMenu() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBankScreen(sharedPreferences: android.content.SharedPreferences, onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    var loanInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    // Глобальные счётчики
    var appBalance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }

    // Данные кредита
    var isLoanApproved by remember {
        mutableStateOf(sharedPreferences.getBoolean("loan_approved", false))
    }
    var currentDebt by remember {
        mutableStateOf(sharedPreferences.getLong("loan_debt", 0L))
    }
    var savedName by remember {
        mutableStateOf(sharedPreferences.getString("loan_borrower", "") ?: "")
    }

    val pathLines = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val neonRed = Color(0xFFFF3333)
    val neonGreen = Color(0xFF00FF00)
    val darkCardBg = BkDarkCardBg

    // Оффлайн-удвоение долга
    LaunchedEffect(Unit) {
        if (isLoanApproved && currentDebt > 0) {
            val lastSavedTime = sharedPreferences.getLong("loan_last_time", 0L)
            if (lastSavedTime > 0L) {
                val timePassedMs = System.currentTimeMillis() - lastSavedTime
                val minutesPassed = timePassedMs / 60000L

                for (i in 0 until minutesPassed.coerceAtMost(30)) {
                    if (currentDebt < 1_000_000_000_000L) {
                        currentDebt *= 2
                    }
                }
                if (currentDebt > 1_000_000_000_000L) {
                    currentDebt = 1_000_000_000_000L
                }

                sharedPreferences.edit()
                    .putLong("loan_debt", currentDebt)
                    .putLong("loan_last_time", System.currentTimeMillis())
                    .apply()
            }
        }
    }

    // Живой таймер удвоения долга
    LaunchedEffect(isLoanApproved) {
        if (isLoanApproved) {
            while (true) {
                delay(60000L)

                if (currentDebt < 1_000_000_000_000L) {
                    currentDebt *= 2
                    if (currentDebt > 1_000_000_000_000L) {
                        currentDebt = 1_000_000_000_000L
                    }

                    sharedPreferences.edit()
                        .putLong("loan_debt", currentDebt)
                        .putLong("loan_last_time", System.currentTimeMillis())
                        .apply()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
        TopBarCasinoBank(
            balance = appBalance,
            freeSpins = freeSpins
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isLoanApproved) {
            // === ЭТАП 1: ОФОРМЛЕНИЕ КРЕДИТА ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "БАНК",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = neonRed,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ВЗЯТЬ КРЕДИТ (МАКСИМУМ 10 000 \$)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = loanInput,
                    onValueChange = { input ->
                        if (input.all { char -> char.isDigit() }) {
                            val num = input.toLongOrNull() ?: 0L
                            if (num <= 10000) loanInput = input
                        }
                    },
                    label = { Text("Сумма займа ($)") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonRed,
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = darkCardBg,
                        unfocusedContainerColor = darkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Расшифровка подписи") },
                    placeholder = { Text("Пример: А. А. Аркадонский", color = Color.DarkGray) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonRed,
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = darkCardBg,
                        unfocusedContainerColor = darkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Нарисуйте вашу подпись ниже:",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(darkCardBg, RoundedCornerShape(8.dp))
                        .border(2.dp, neonRed, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val path = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = path
                                    pathLines.add(path)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath?.let { path ->
                                        pathLines.remove(path)
                                        path.lineTo(change.position.x, change.position.y)
                                        pathLines.add(path)
                                    }
                                },
                                onDragEnd = { currentPath = null }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        pathLines.forEach { path ->
                            drawPath(path = path, color = Color.Cyan, style = Stroke(width = 6f))
                        }
                    }
                    if (pathLines.isEmpty()) {
                        Text(
                            text = "[ МЕСТО ДЛЯ ПОДПИСИ ]",
                            color = Color(0x33FFFFFF),
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (loanInput.isNotEmpty() && nameInput.isNotEmpty() && pathLines.isNotEmpty()) {
                            val inputSum = loanInput.toIntOrNull() ?: 0
                            if (inputSum in 1..10000) {
                                currentDebt = inputSum.toLong()
                                savedName = nameInput
                                isLoanApproved = true

                                val currentGlobalBalance = sharedPreferences.getInt("balance", 100)
                                val newGlobalBalance = currentGlobalBalance + inputSum
                                appBalance = newGlobalBalance

                                sharedPreferences.edit()
                                    .putBoolean("loan_approved", true)
                                    .putLong("loan_debt", currentDebt)
                                    .putString("loan_borrower", savedName)
                                    .putLong("loan_last_time", System.currentTimeMillis())
                                    .putInt("balance", newGlobalBalance)
                                    .apply()

                                Toast.makeText(context, "Кредит одобрен! +$inputSum $ зачислены! 💰", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(2.dp, neonGreen, RoundedCornerShape(8.dp))
                ) {
                    Text("ПОДПИСАТЬ ДОГОВОР 📝", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

        } else {
            // === ЭТАП 2: ЭКРАН ТЕКУЩЕГО ЗАЙМА ===
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "БАНК",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = neonRed,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ТЕКУЩИЙ ДОЛГ БАНКУ:",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$currentDebt $",
                    fontSize = 42.sp,
                    color = neonRed,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "Заёмщик: $savedName",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // === МИНИМАЛИСТИЧНАЯ КНОПКА ПОГАШЕНИЯ ===
                Button(
                    onClick = {
                        val currentGlobalBalance = sharedPreferences.getInt("balance", 100)

                        if (currentGlobalBalance >= currentDebt) {
                            val updatedBalance = currentGlobalBalance - currentDebt.toInt()
                            appBalance = updatedBalance

                            sharedPreferences.edit()
                                .putBoolean("loan_approved", false)
                                .putLong("loan_debt", 0L)
                                .putString("loan_borrower", "")
                                .putLong("loan_last_time", 0L)
                                .putInt("balance", updatedBalance)
                                .apply()

                            isLoanApproved = false
                            currentDebt = 0L
                            loanInput = ""
                            nameInput = ""
                            pathLines.clear()

                            Toast.makeText(context, "Кредит успешно закрыт! Долг списан. 🎉", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Недостаточно денег на балансе игры для погашения! ❌", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .border(1.dp, Color.Cyan.copy(alpha = 0.8f), RoundedCornerShape(50))
                ) {
                    Text(
                        text = "Погасить кредит",
                        color = Color.Cyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .clickable { onBackToMenu() }
        )
    }
}

// ===== Верхняя панель — 1:1 как в главном меню, но с анимацией цифр =====
@Composable
private fun TopBarCasinoBank(
    balance: Int,
    freeSpins: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BkDarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(BkGoldAccent, Color(0xFFFFA751)))
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
                color = BkGoldAccent
            )
            Spacer(modifier = Modifier.weight(1f))

            // ПЛАВНАЯ АНИМАЦИЯ БАЛАНСА
            StatChipBank(emoji = "💰", value = balance.toString(), accent = BkGoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            // ПЛАВНАЯ АНИМАЦИЯ ФРИСПИНОВ
            StatChipBank(emoji = "🎁", value = freeSpins.toString(), accent = BkNeonCyan)
        }
    }
}

@Composable
private fun StatChipBank(emoji: String, value: String, accent: Color) {
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
        // Анимируем именно значение
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                slideInVertically { h -> -h } + fadeIn() togetherWith
                        slideOutVertically { h -> h } + fadeOut()
            },
            label = "StatChipValueAnim"
        ) { animatedValue ->
            Text(
                text = animatedValue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}