package com.example.myfirstapp

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.Crossfade
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒 БАНК ЗАБЛОКИРОВАН", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF3333))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Системы банка перезагружаются после кода 666.\nДоступ восстановится через: $timeLeftMinutes мин.",
                    color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Text("Назад в меню", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.clickable { onBackToMenu() })
            }
        }
    } else {
        var isBankUnlocked by remember {
            mutableStateOf(sharedPreferences.getBoolean("bank_unlocked", false))
        }

        Crossfade(targetState = isBankUnlocked, label = "BankScreenTransition") { unlocked ->
            if (!unlocked) {
                BankAuthScreen(
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
fun BankAuthScreen(onAccessGranted: () -> Unit, onBackToMenu: () -> Unit) {
    var pinCode by remember { mutableStateOf("") }
    val neonPurple = Color(0xFFD67BFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 24.dp),
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
                focusedContainerColor = Color(0xFF0F0C20),
                unfocusedContainerColor = Color(0xFF0F0C20),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBankScreen(sharedPreferences: android.content.SharedPreferences, onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    var loanInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    // Загружаем сохраненные данные из SharedPreferences
    var isLoanApproved by remember {
        mutableStateOf(sharedPreferences.getBoolean("loan_approved", false))
    }
    var currentDebt by remember {
        mutableStateOf(sharedPreferences.getLong("loan_debt", 0L))
    }
    var savedName by remember {
        mutableStateOf(sharedPreferences.getString("loan_borrower", "") ?: "")
    }

    // Линии для рисования подписи
    val pathLines = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val neonRed = Color(0xFFFF3333)
    val neonGreen = Color(0xFF00FF00)
    val darkCardBg = Color(0xFF0F0C20)

    // Расчёт пропущенного времени при перезаходе в банк (офлайн-удвоение долга)
    // Расчёт пропущенного времени при перезаходе в банк (офлайн-удвоение долга с потолком в 1 ТРИЛЛИОН)
    LaunchedEffect(Unit) {
        if (isLoanApproved && currentDebt > 0) {
            val lastSavedTime = sharedPreferences.getLong("loan_last_time", 0L)
            if (lastSavedTime > 0L) {
                val timePassedMs = System.currentTimeMillis() - lastSavedTime
                val minutesPassed = timePassedMs / 60000L

                // Каждую пропущенную минуту удваиваем долг, но не превышаем 1 триллион
                for (i in 0 until minutesPassed.coerceAtMost(30)) {
                    if (currentDebt < 1_000_000_000_000L) {
                        currentDebt *= 2
                    }
                }
                // Намертво фиксируем на 1 триллионе, если ушли за предел
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

    // ТАЙМЕР НА ЭКРАНЕ: Удваивает долг каждую минуту прямо перед глазами, останавливаясь на 1 ТРИЛЛИОНЕ
    LaunchedEffect(isLoanApproved) {
        if (isLoanApproved) {
            while (true) {
                delay(60000L) // 1 минута

                if (currentDebt < 1_000_000_000_000L) {
                    currentDebt *= 2
                    // Проверяем, чтобы одиночный шаг не перешагнул через триллион
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
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "БАНК",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = neonRed,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoanApproved) {
            // ЭТАП 1: ОФОРМЛЕНИЕ КРЕДИТА
            Text(
                text = "ВЗЯТЬ КРЕДИТ (МАКСИМУМ 10 000 \$)",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода суммы кредита (только цифры)
            OutlinedTextField(
                value = loanInput,
                onValueChange = { input ->
                    if (input.all { char -> char.isDigit() }) {
                        // Проверяем лимит в 10 000 перед обновлением текста
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

            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода расшифровки подписи (теперь без скрытых ограничений на русские буквы!)
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нарисуйте вашу подпись ниже:",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ХОЛСТ ДЛЯ РИСОВАНИЯ ПОДПИСИ
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
                            onDrag = { change, dragAmount ->
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

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка одобрения договора с фиксацией и НАЧИСЛЕНИЕМ баланса
            Button(
                onClick = {
                    if (loanInput.isNotEmpty() && nameInput.isNotEmpty() && pathLines.isNotEmpty()) {
                        val inputSum = loanInput.toIntOrNull() ?: 0
                        if (inputSum in 1..10000) {
                            currentDebt = inputSum.toLong()
                            savedName = nameInput
                            isLoanApproved = true

                            // Читаем текущий игровой баланс, плюсуем заём и сохраняем всё вместе
                            val currentGlobalBalance = sharedPreferences.getInt("balance", 100)
                            val newGlobalBalance = currentGlobalBalance + inputSum

                            sharedPreferences.edit()
                                .putBoolean("loan_approved", true)
                                .putLong("loan_debt", currentDebt)
                                .putString("loan_borrower", savedName)
                                .putLong("loan_last_time", System.currentTimeMillis())
                                .putInt("balance", newGlobalBalance) // Деньги зачислены на счёт!
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

        } else {
            // ЭТАП 2: ЭКРАН ТЕКУЩЕГО ЗАЙМА С КНОПКОЙ ПОГАШЕНИЯ
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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

                // КНОПКА ПОГАШЕНИЯ КРЕДИТА
                Button(
                    onClick = {
                        val currentGlobalBalance = sharedPreferences.getInt("balance", 100)

                        // Проверяем, хватает ли общего баланса игры для закрытия долга
                        if (currentGlobalBalance >= currentDebt) {
                            val updatedBalance = currentGlobalBalance - currentDebt.toInt()

                            // Сбрасываем все данные о кредите в памяти и обновляем баланс
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
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(2.dp, Color.Cyan, RoundedCornerShape(8.dp))
                ) {
                    Text("ПОГАСИТЬ КРЕДИТ ПОЛНОСТЬЮ 💳", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable { onBackToMenu() }
        )
    }
}

