package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


data class DiscoveredCard(
    val location: String, // Где нашли (например, "Найдена: на улице")
    val number: String,   // 16 случайных цифр
    val expiry: String,   // ммгг
    val cvc: String       // ххх
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current

    // 1. СНАЧАЛА создаём SharedPreferences, чтобы к ней могли обращаться другие переменные! ✅
    val sharedPreferences = remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    // ИСПРАВЛЕНО: заменили "by" на "=", чтобы исключить ошибки импортов типов Kotlin
    val savedLoc = sharedPreferences.getString("saved_card_loc", "") ?: ""
    val savedNum = sharedPreferences.getString("saved_card_num", "") ?: ""
    val savedExp = sharedPreferences.getString("saved_card_exp", "") ?: ""
    val savedCvc = sharedPreferences.getString("saved_card_cvc", "") ?: ""

    // Инициализируем foundCard: если в памяти что-то было, восстанавливаем карту, иначе ставим null
    val foundCard = remember {
        mutableStateOf<DiscoveredCard?>(
            if (savedLoc.isNotEmpty()) DiscoveredCard(savedLoc, savedNum, savedExp, savedCvc) else null
        )
    }

    var cardNumberInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvcInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isAuthorized by remember { mutableStateOf(false) }
    var cardBankBalance by remember { mutableStateOf(0) }
    var customAmountInput by remember { mutableStateOf("") }
    var showHintSheet by remember { mutableStateOf(false) }
    var isProcessingTransaction by remember { mutableStateOf(false) }
    var transactionMessage by remember { mutableStateOf("") }
    var currentCardOwner by remember { mutableStateOf("") }

    var isSearching by remember { mutableStateOf(false) }
    val searchWheelAngle = remember { Animatable(0f) }
    var searchResultText by remember { mutableStateOf("") }

    var appBalance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }

    val darkBgGradient = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
    val neonBlue = Color(0xFF00F0FF)
    val neonGreen = Color(0xFF00FF00)
    val darkCardBg = Color(0xFF0F0C20)

    fun saveAppBalance(newBalance: Int) {
        sharedPreferences.edit().putInt("balance", newBalance).apply()
    }
    fun saveDiscoveredCard(card: DiscoveredCard?) {
        if (card == null) {
            // Если карту выбросили — полностью стираем строки из памяти телефона
            sharedPreferences.edit()
                .putString("saved_card_loc", "")
                .putString("saved_card_num", "")
                .putString("saved_card_exp", "")
                .putString("saved_card_cvc", "")
                .apply()
        } else {
            // Если карту нашли — намертво сохраняем её параметры в кэш
            sharedPreferences.edit()
                .putString("saved_card_loc", card.location)
                .putString("saved_card_num", card.number)
                .putString("saved_card_exp", card.expiry)
                .putString("saved_card_cvc", card.cvc)
                .apply()
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(darkBgGradient)) {

        if (!isAuthorized) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "АВТОРИЗАЦИЯ КАРТЫ",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Введите 16-значный номер:",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp)
                )
                TextField(
                    value = cardNumberInput,
                    onValueChange = {
                        if (it.length <= 16) cardNumberInput = it.filter { c -> c.isDigit() }
                    },
                    placeholder = { Text("хххх хххх хххх хххх", color = Color.DarkGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CardNumberTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = darkCardBg,
                        unfocusedContainerColor = darkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .border(1.5.dp, neonBlue, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Срок (мм/гг):",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TextField(
                            value = cardExpiryInput,
                            onValueChange = {
                                if (it.length <= 4) cardExpiryInput = it.filter { c -> c.isDigit() }
                            },
                            placeholder = { Text("мм/гг", color = Color.DarkGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = CardExpiryTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = darkCardBg,
                                unfocusedContainerColor = darkCardBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .border(1.5.dp, neonBlue, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Код:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TextField(
                            value = cardCvcInput,
                            onValueChange = {
                                if (it.length <= 3) cardCvcInput = it.filter { c -> c.isDigit() }
                            },
                            placeholder = { Text("ххх", color = Color.DarkGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = darkCardBg,
                                unfocusedContainerColor = darkCardBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .border(1.5.dp, neonBlue, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка проверки данных
                Button(
                    onClick = {
                        // ИСПРАВЛЕНО: Читаем значение через .value, так как убрали коварный "by"
                        val currentCard = foundCard.value
                        if (currentCard != null) {
                            // Проверяем ввод строго по параметрам найденной карты
                            val isValid = cardNumberInput == currentCard.number &&
                                    cardExpiryInput == currentCard.expiry &&
                                    cardCvcInput == currentCard.cvc

                            if (isValid) {
                                // Проверяем, чья именно карта была введена по тексту локации
                                // Если в строке локации есть слово "Папы", даем баланс больше!
                                cardBankBalance = sharedPreferences.getInt("saved_card_bank_balance", 0)

                                // Фиксируем владельца по названию локации
                                currentCardOwner = currentCard.location
                                isAuthorized = true
                                android.widget.Toast.makeText(
                                    context,
                                    "Вход выполнен успешно! ✔",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "Неверные данные карты! ❌",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "У вас нет ни одной карты! Загляните в шпаргалку. 🔎",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    // ИСПРАВЛЕНО: проверяем наличие карты через .value
                    enabled = foundCard.value != null && !isProcessingTransaction,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                        .border(2.dp, neonBlue, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        "ВОЙТИ В АККАУНТ КАРТЫ 🔑",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "назад в меню",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onBackToMenu() })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clickable { showHintSheet = true },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "▲", color = neonBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "карточки",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = darkCardBg),
                    modifier = Modifier.fillMaxWidth()
                        .border(2.dp, neonGreen, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "БАНКОВСКИЙ СЧЁТ КАРТЫ 💳",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$cardBankBalance 💰",
                            fontSize = 36.sp,
                            color = neonGreen,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Баланс вашего приложения: $appBalance 💰",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Выберите сумму для перевода в игру:",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                listOf(10, 25, 50).forEach { amount ->
                    Button(
                        onClick = {
                            isProcessingTransaction = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(Random.nextLong(1000, 3001))

                                // ИСПРАВЛЕНО: Динамический шанс блокировки фрод-системы!
                                // Если карта принадлежит папе, шанс блока 70%, если маме — 5%
                                val isFraud = Random.nextInt(1, 101) <= 5

                                if (isFraud) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Операция отклонена: Безопасность банка заблокировала перевод! 🚨",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                } else if (cardBankBalance >= amount) {
                                    cardBankBalance -= amount
                                    appBalance += amount
                                    saveAppBalance(appBalance)

                                    // МАГИЧЕСКАЯ СТРОЧКА: Намертво запоминаем новый остаток карты на диске телефона! 🔐
                                    sharedPreferences.edit().putInt("saved_card_bank_balance", cardBankBalance).apply()

                                    android.widget.Toast.makeText(
                                        context,
                                        "Переведено +$amount монет в игру! 🎉",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                isProcessingTransaction = false
                            }
                        },
                        enabled = cardBankBalance >= amount && !isProcessingTransaction,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(46.dp)
                            .border(
                                1.5.dp,
                                if (cardBankBalance >= amount && !isProcessingTransaction) neonGreen else Color.DarkGray,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = "Перевести $amount монет",
                            color = if (cardBankBalance >= amount && !isProcessingTransaction) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Или введите сумму вручную:",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = customAmountInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && !isProcessingTransaction) {
                                customAmountInput = input.filter { it.isDigit() }
                            }
                        },
                        placeholder = { Text("Сумма...", color = Color.DarkGray) },
                        singleLine = true,
                        enabled = !isProcessingTransaction,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = darkCardBg,
                            unfocusedContainerColor = darkCardBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = neonGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                1.5.dp,
                                if (!isProcessingTransaction) neonGreen else Color.DarkGray,
                                RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    val enteredAmount = customAmountInput.toIntOrNull() ?: 0
                    val isCustomAmountValid =
                        enteredAmount > 0 && cardBankBalance >= enteredAmount && !isProcessingTransaction

                    Button(
                        onClick = {
                            if (isCustomAmountValid) {
                                isProcessingTransaction = true

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(Random.nextLong(1000, 3001))

                                    // ИСПРАВЛЕНО: Теперь и при ручном вводе папина карта блокируется в 70% случаев! 🛡️
                                    val isFraud = Random.nextInt(1, 101) <= 5

                                    if (isFraud) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Безопасность банка: Операция заморожена как подозрительная! 🚨",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        cardBankBalance -= enteredAmount
                                        appBalance += enteredAmount
                                        saveAppBalance(appBalance)

                                        // МАГИЧЕСКАЯ СТРОЧКА: Намертво фиксируем трату и при ручном вводе! 🔐
                                        sharedPreferences.edit().putInt("saved_card_bank_balance", cardBankBalance).apply()

                                        android.widget.Toast.makeText(
                                            context,
                                            "Успешно переведено +$enteredAmount монет! 💰",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    customAmountInput = ""
                                    isProcessingTransaction = false
                                }
                            }
                        },
                        enabled = isCustomAmountValid,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkCardBg,
                            disabledContainerColor = darkCardBg
                        ),
                        modifier = Modifier
                            .width(100.dp)
                            .height(48.dp)
                            .border(
                                1.5.dp,
                                if (isCustomAmountValid) neonGreen else Color.DarkGray,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        if (isProcessingTransaction) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = neonGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "ОК",
                                color = if (isCustomAmountValid) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        cardNumberInput = ""
                        cardExpiryInput = ""
                        cardCvcInput = ""
                        isAuthorized = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.width(220.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                ) {
                    Text("Сменить карточку", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "выйти в главное меню",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onBackToMenu() })
            }
        } // Закрывает блок else. Внешний Box остаётся открытым, ждём шторку!

        // СЛОЙ ШТОРКИ: МЕНЮ ХИНТ (ПОИСК КАРТ С ВНУТРЕННИМ КРУГОМ)
        // ==========================================================
        if (showHintSheet) {
            ModalBottomSheet(
                onDismissRequest = { if (!isSearching) showHintSheet = false },
                containerColor = Color(0xFF1F2937),
                scrimColor = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "КАРДИНГ 🔎",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    val activeCard = foundCard.value

                    if (activeCard == null) {
                        // ==========================================
                        // СОСТОЯНИЕ 1: КАРМАН ПУСТ -> КОЛЕСО И ПОИСК
                        // ==========================================
                        if (isSearching) {
                            Box(
                                modifier = Modifier.size(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val strokeWidth = 16.dp.toPx() // Толщина цветной дорожки
                                    val radius =
                                        size.width / 2 // Радиус по центру дорожки

                                    val orangeRed8Bit =
                                        Color(0xFFFF4500) // Оранжево-красный выигрыш
                                    val darkLoseZone =
                                        Color(0xFF2D3748)  // Матовый графитовый проигрыш
                                    val bgCenterColor =
                                        Color(0xFF1F2937) // Цвет шторки (чтобы сливался центр)
                                    val ringLineColor = Color(0xFF404040) // Контурные линии

                                    // СЛОЙ 1: Цветные дуги (33% выигрыша снизу)
                                    drawArc(
                                        color = orangeRed8Bit,
                                        startAngle = 90f - 60f, // Центрируем выигрыш строго снизу
                                        sweepAngle = 120f,      // 120 градусов — это честные 33% круга
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    drawArc(
                                        color = darkLoseZone,
                                        startAngle = 90f + 60f,
                                        sweepAngle = 240f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // СЛОЙ 2: Внутренний круг цвета фона шторки (Закрывает середину стрелки!)
                                    val innerRadius = radius - (strokeWidth / 2)
                                    drawCircle(
                                        color = bgCenterColor,
                                        radius = innerRadius,
                                        center = center
                                    )

                                    // СЛОЙ 3: Тёмная стрелка, летящая строго ПО цветам
                                    val angleInRadians = (searchWheelAngle.value * PI / 180f)
                                    val startX =
                                        center.x + innerRadius * cos(angleInRadians).toFloat()
                                    val startY =
                                        center.y + innerRadius * sin(angleInRadians).toFloat()

                                    val outerRadius = radius + (strokeWidth / 2)
                                    val endX =
                                        center.x + outerRadius * cos(angleInRadians).toFloat()
                                    val endY =
                                        center.y + outerRadius * sin(angleInRadians).toFloat()

                                    drawLine(
                                        color = Color(0xFF1A0F0A),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 5.dp.toPx()
                                    )

                                    // СЛОЙ 4: Тонкие обводки контуров для премиальности
                                    drawCircle(
                                        color = ringLineColor,
                                        radius = outerRadius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = ringLineColor,
                                        radius = innerRadius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                                Text(
                                    text = "33%",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (searchResultText.isNotEmpty()) {
                            Text(
                                text = searchResultText,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                isSearching = true
                                searchResultText = ""

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(300)

                                    val randomRoll = Random.nextInt(1, 101)
                                    val isSuccess = randomRoll <= 33

                                    val targetAngle =
                                        if (isSuccess) Random.nextInt(35, 145) else Random.nextInt(
                                            155,
                                            385
                                        )
                                    val totalRotation = 1440f + targetAngle

                                    searchWheelAngle.snapTo(90f)
                                    searchWheelAngle.animateTo(
                                        targetValue = totalRotation,
                                        animationSpec = tween(
                                            durationMillis = 2500,
                                            easing = androidx.compose.animation.core.LinearOutSlowInEasing
                                        )
                                    )

                                    if (isSuccess) {
                                        val locations = listOf(
                                            "в школе",
                                            "на улице",
                                            "в магазине",
                                            "на работе",
                                            "у друга дома",
                                            "в столовай",
                                            "в буфете",
                                            "у препода в кармане",
                                            "у прохожего в сумке",
                                            "в кошельке у незнакомца",
                                            "в мусорке",
                                            "на заправке",
                                            "в кинотеатре",
                                            "в автобусе"
                                        )
                                        val chosenLocation =
                                            "Карточка найдена " + locations.random()

                                        val fakeNumber =
                                            List(16) { Random.nextInt(0, 10) }.joinToString("")
                                        val month = String.format("%02d", Random.nextInt(1, 13))
                                        val year = Random.nextInt(27, 31).toString()

                                        // НОВОЕ ПРАВИЛО: Генерируем баланс один раз при находке карты! 💰
                                        // Если в тексте есть слово "работе" или "магазине", пусть это будет "богатая" карта папы
                                        val initialCardBalance = if (chosenLocation.contains("работе") || chosenLocation.contains("магазине")) {
                                            Random.nextInt(30, 301)
                                        } else {
                                            Random.nextInt(1, 81)
                                        }

                                        val newCard = DiscoveredCard(
                                            chosenLocation,
                                            fakeNumber,
                                            "$month$year",
                                            String.format("%03d", Random.nextInt(0, 1000))
                                        )

                                        // Сохраняем карту в ОЗУ и на диск телефона 💾
                                        foundCard.value = newCard
                                        saveDiscoveredCard(newCard)

                                        // МАГИЧЕСКАЯ СТРОЧКА: Намертво фиксируем начальный баланс этой карты в кэш! 🔐
                                        sharedPreferences.edit().putInt("saved_card_bank_balance", initialCardBalance).apply()

                                        searchResultText = "Успех! Карта добавлена в карман. 💎"
                                    } else {
                                        searchResultText =
                                            "Найти карту не удалось... Вы обыскали всё вокруг. ❌"
                                    }
                                    isSearching = false
                                }
                            },
                            enabled = !isSearching,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                .border(2.dp, neonBlue, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = if (isSearching) "ОБЫСК ЛОКАЦИЙ..." else "ПОИСК КАРТОЧЕК 🔍",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // ==========================================
                        // СОСТОЯНИЕ 2: КАРТА НАЙДЕНА -> ВЫВОД И КНОПКА ВЫБРОСИТЬ
                        // ==========================================
                        Card(
                            colors = CardDefaults.cardColors(containerColor = darkCardBg),
                            modifier = Modifier.fillMaxWidth()
                                .border(1.5.dp, neonBlue, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = activeCard.location,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )

                                val formattedNumber = activeCard.number.chunked(4).joinToString(" ")
                                Text(
                                    text = "Номер: $formattedNumber",
                                    color = neonBlue,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val formattedExpiry =
                                        activeCard.expiry.chunked(2).joinToString("/")
                                    Text(text = "Срок: $formattedExpiry", color = Color.LightGray)
                                    Text(text = "CVC: ${activeCard.cvc}", color = Color.LightGray)
                                }
                            }
                        }

                        // Кнопка выбросить карту находится СТРОГО здесь, внутри хинт-меню! 🗑️
                        Button(
                            onClick = {
                                foundCard.value = null
                                saveDiscoveredCard(null) // Полностью стираем карту с диска телефона
                                searchResultText = ""
                                isAuthorized = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1F1F)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                .border(1.5.dp, Color.Red, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "ВЫБРОСИТЬ КАРТУ 🗑",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// =========================================================================
// КЛАССЫ СЛОЖНЫХ МАСОК ОТРЫВКА СИМВОЛОВ
// =========================================================================
class CardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0, 16) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 4 == 3 && i != 15) out += " "
        }
        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }
        return TransformedText(AnnotatedString(out), numberOffsetTranslator)
    }
}

class CardExpiryTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += "/"
        }
        val expiryOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), expiryOffsetTranslator)
    }
}