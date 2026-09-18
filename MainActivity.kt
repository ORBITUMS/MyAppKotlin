package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.animation.AnimatedVisibility
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

val richLightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFDF9), Color(0xFFF9EED8))
)

// Цвета для игры "Струп"
data class GameColor(val name: String, val color: Color)
val gameColors = listOf(
    GameColor("Красный", Color(0xFFFF6B6B)),
    GameColor("Голубой", Color(0xFF4DADFF)),
    GameColor("Жёлтый", Color(0xFFFFD93D)),
    GameColor("Зелёный", Color(0xFF6BCB77)),
    GameColor("Пурпурный", Color(0xFFD67BFF)),
    GameColor("Синий", Color(0xFF4B65F6))
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                onNavigateToSecond = { navController.navigate("second") },
                onNavigateToThird = { navController.navigate("third") }
            )
        }
        composable("second") { SecondScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("third") { ThirdScreen(onBackToMenu = { navController.popBackStack() }) }
    }
}

@Composable
fun MenuScreen(onNavigateToSecond: () -> Unit, onNavigateToThird: () -> Unit) {
    val context = LocalContext.current
    var showPromoDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var isCodeAccepted by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(richLightGradient),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Главное меню", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3E25))
        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = { onNavigateToSecond() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D734B)), modifier = Modifier.width(220.dp)) {
            Text(text = "Перейти к игре", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigateToThird() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)), modifier = Modifier.width(220.dp)) {
            Text(text = "🎰 Казино Рояль", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showPromoDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3E25)), modifier = Modifier.width(220.dp)) {
            Text(text = "🎫 Промокоды", fontSize = 16.sp, color = Color.White)
        }
    }

    if (showPromoDialog) {
        AlertDialog(
            onDismissRequest = {
                showPromoDialog = false
                promoInput = ""
                balanceInput = ""
                isCodeAccepted = false
            },
            title = {
                Text(
                    text = if (!isCodeAccepted) "Ввод промокода" else "Режим разработчика ⚙️",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A3E25)
                )
            },
            text = {
                Column {
                    if (!isCodeAccepted) {
                        Text(text = "Введите промокод для активации бонусов:", modifier = Modifier.padding(bottom = 8.dp))
                        TextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = { Text("Код...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                        )
                    } else {
                        Text(text = "Код успешно активирован! Введите желаемый баланс для казино:", modifier = Modifier.padding(bottom = 8.dp))
                        TextField(
                            value = balanceInput,
                            onValueChange = { balanceInput = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("Пример: 5000") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sharedPreferences = context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)

                        if (!isCodeAccepted) {
                            val cleanInput = promoInput.trim()

                            if (cleanInput == "7772") {
                                // Секретный код разработчика (остаётся многоразовым для тестов)
                                isCodeAccepted = true
                            } else if (cleanInput == "777") {
                                // Проверяем, был ли промокод использован ранее
                                val isPromoUsed = sharedPreferences.getBoolean("promo_777_used", false)

                                if (isPromoUsed) {
                                    Toast.makeText(context, "Этот промокод уже активирован! ❌", Toast.LENGTH_LONG).show()
                                } else {
                                    // Начисляем бонус
                                    val currentBalance = sharedPreferences.getInt("balance", 100)
                                    val newBalance = currentBalance + 250

                                    // Сохраняем новый баланс и помечаем промокод как использованный
                                    sharedPreferences.edit()
                                        .putInt("balance", newBalance)
                                        .putBoolean("promo_777_used", true)
                                        .apply()

                                    Toast.makeText(context, "Промокод активирован! Получено +250 💰", Toast.LENGTH_LONG).show()
                                    showPromoDialog = false
                                    promoInput = ""
                                }
                            } else {
                                Toast.makeText(context, "Неверный код ❌", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Применение баланса из режима разработчика
                            val newBalance = balanceInput.toIntOrNull() ?: 0
                            sharedPreferences.edit().putInt("balance", newBalance).apply()

                            Toast.makeText(context, "Баланс успешно изменён на $newBalance 💰", Toast.LENGTH_SHORT).show()
                            showPromoDialog = false
                            promoInput = ""
                            balanceInput = ""
                            isCodeAccepted = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D734B))
                ) {
                    Text(text = if (!isCodeAccepted) "Проверить" else "Применить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPromoDialog = false
                    promoInput = ""
                    balanceInput = ""
                    isCodeAccepted = false
                }) {
                    Text(text = "Отмена", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFFFFFDF9)
        )
    }
}


@Composable
fun SecondScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(sharedPreferences.getInt("high_score", 0)) }
    var bgIndex by remember { mutableStateOf(0) }
    var textIndex by remember { mutableStateOf(1) }

    val nextRound = {
        val newBg = Random.nextInt(6)
        var newText = Random.nextInt(6)
        while (newText == newBg) { newText = Random.nextInt(6) }
        bgIndex = newBg
        textIndex = newText
    }

    Box(modifier = Modifier.fillMaxSize().background(richLightGradient)) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Счёт: $score", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D734B))
            Text(text = "Рекорд: $highScore", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4A3E25))
            Spacer(modifier = Modifier.height(28.dp))
            Box(modifier = Modifier.size(190.dp).background(gameColors[bgIndex].color, shape = CircleShape), contentAlignment = Alignment.Center) {
                Text(text = gameColors[textIndex].name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(modifier = Modifier.height(48.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    for (i in 0..2) {
                        SmallColorButton(gameColor = gameColors[i], onClick = {
                            if (i == textIndex) {
                                score++
                                if (score > highScore) {
                                    highScore = score
                                    sharedPreferences.edit().putInt("high_score", highScore).apply()
                                }
                            } else if (score > 0) { score-- }
                            nextRound()
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Второй ряд кнопок цветов
                Row {
                    for (i in 3..5) {
                        SmallColorButton(gameColor = gameColors[i], onClick = {
                            if (i == textIndex) {
                                score++
                                if (score > highScore) {
                                    highScore = score
                                    sharedPreferences.edit().putInt("high_score", highScore).apply()
                                }
                            } else if (score > 0) {
                                score--
                            }
                            nextRound()
                        })
                    }
                }
            }
        }

        // Кнопка выхода (находится внутри Box, выровнена по нижнему центру)
        Button(
            onClick = { onBackToMenu() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB13B3B)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Выйти на главный экран", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun SmallColorButton(gameColor: GameColor, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .size(55.dp)
            .background(gameColor.color, shape = CircleShape)
            .clickable { onClick() }
    )
}

// --- ЭКРАН КАЗИНО ---

class WinRecord(val id: Long, val amount: Int, isVisibleState: MutableState<Boolean>) {
    var isVisible by isVisibleState
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThirdScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var maxWin by remember { mutableStateOf(sharedPreferences.getInt("max_win", 0)) }
    var bet by remember { mutableStateOf(0) }

    val winRecords = remember { mutableStateListOf<WinRecord>() }

    val slotEmojis = listOf("7️⃣", "💎", "🔔", "🍉", "🍇", "🍋", "🍒")

    var slot1 by remember { mutableStateOf(0) }
    var slot2 by remember { mutableStateOf(1) }
    var slot3 by remember { mutableStateOf(2) }

    var isSpinning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val animOffsetY1 = remember { Animatable(0f) }
    val animOffsetY2 = remember { Animatable(0f) }
    val animOffsetY3 = remember { Animatable(0f) }

    fun saveCasinoData(newBalance: Int, newMaxWin: Int) {
        sharedPreferences.edit()
            .putInt("balance", newBalance)
            .putInt("max_win", newMaxWin)
            .apply()
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))))) {

        // ВЕРХНЯЯ ПАНЕЛЬ: ТИТУЛ И VIP-ОКОШКО С ПЛАВНЫМ БАЛАНСОМ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎰 СЛОТ-МАШИНА", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFE259), Color(0xFFFFA751)))),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "МАКС. КУШ 🏆", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = "$maxWin", fontSize = 20.sp, color = Color(0xFFE94560), fontWeight = FontWeight.Black)
                    }

                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF3A3F58)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "БАЛАНС 💰", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                        AnimatedContent(
                            targetState = balance,
                            transitionSpec = {
                                slideInVertically { height -> -height } + fadeIn() with
                                        slideOutVertically { height -> height } + fadeOut()
                            }
                        ) { animatedBalance ->
                            Text(text = "$animatedBalance", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Игровой автомат (барабаны)
        Card(
            modifier = Modifier.size(340.dp, 160.dp).align(Alignment.Center).offset(y = (-30).dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Барабан 1
                Box(
                    modifier = Modifier.size(80.dp).background(Color(0xFF1F1A3A), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slotEmojis[slot1],
                        fontSize = 42.sp,
                        modifier = Modifier.offset(y = animOffsetY1.value.dp)
                    )
                }
                // Барабан 2
                Box(
                    modifier = Modifier.size(80.dp).background(Color(0xFF1F1A3A), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slotEmojis[slot2],
                        fontSize = 42.sp,
                        modifier = Modifier.offset(y = animOffsetY2.value.dp)
                    )
                }
                // Барабан 3
                Box(
                    modifier = Modifier.size(80.dp).background(Color(0xFF1F1A3A), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slotEmojis[slot3],
                        fontSize = 42.sp,
                        modifier = Modifier.offset(y = animOffsetY3.value.dp)
                    )
                }
            }
        }

        // БЛОК УПРАВЛЕНИЯ СНИЗУ
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ПРЕМИАЛЬНАЯ ЛЕНТА С ИДЕАЛЬНО ПЛАВНЫМ ЗАТУХАНИЕМ
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ПРЕМИАЛЬНОЕ ОКОШКО СТАВКИ С АНИМАЦИЕЙ СВЕРХУ ВНИЗ
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A3A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "СТАВКА: ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )

                    AnimatedContent(
                        targetState = bet,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> -height } + fadeIn() with
                                        slideOutVertically { height -> height } + fadeOut()
                            } else {
                                slideInVertically { height -> height } + fadeIn() with
                                        slideOutVertically { height -> -height } + fadeOut()
                            }.using(androidx.compose.animation.SizeTransform(clip = false))
                        }
                    ) { animatedBet ->
                        Text(
                            text = "$animatedBet 💰",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }

            // Панель изменения ставок (-100, -10, +10, +100)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = { bet = (bet - 100).coerceAtLeast(0) },
                    enabled = !isSpinning && bet > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) { Text("-100", color = Color.White) }

                Button(
                    onClick = { bet = (bet - 10).coerceAtLeast(0) },
                    enabled = !isSpinning && bet > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) { Text("-10", color = Color.White) }

                Button(
                    onClick = { bet = (bet + 10).coerceAtMost(balance) },
                    enabled = !isSpinning && bet < balance,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) { Text("+10", color = Color.White) }

                Button(
                    onClick = { bet = (bet + 100).coerceAtMost(balance) },
                    enabled = !isSpinning && bet < balance,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) { Text("+100", color = Color.White) }
            }

            // Кнопка КРУТИТЬ
            Button(
                onClick = {
                    if (!isSpinning && bet > 0 && balance >= bet) {
                        balance -= bet
                        isSpinning = true

                        coroutineScope.launch {
                            launch {
                                for (i in 1..8) {
                                    slot1 = Random.nextInt(slotEmojis.size)
                                    animOffsetY1.animateTo(-20f, tween(50))
                                    animOffsetY1.animateTo(20f, tween(50))
                                }
                                animOffsetY1.animateTo(0f, tween(50))
                            }
                            launch {
                                for (i in 1..12) {
                                    slot2 = Random.nextInt(slotEmojis.size)
                                    animOffsetY2.animateTo(-20f, tween(50))
                                    animOffsetY2.animateTo(20f, tween(50))
                                }
                                animOffsetY2.animateTo(0f, tween(50))
                            }
                            launch {
                                for (i in 1..16) {
                                    slot3 = Random.nextInt(slotEmojis.size)
                                    animOffsetY3.animateTo(-20f, tween(50))
                                    animOffsetY3.animateTo(20f, tween(50))
                                }
                                animOffsetY3.animateTo(0f, tween(50))

                                isSpinning = false

                                var winReward = 0.0
                                var matchedSymbolIndex = -1
                                var isThreeInRow = false

                                if (slot1 == slot2 && slot2 == slot3) {
                                    isThreeInRow = true
                                    matchedSymbolIndex = slot1
                                } else if (slot1 == slot2 || slot1 == slot3) {
                                    matchedSymbolIndex = slot1
                                } else if (slot2 == slot3) {
                                    matchedSymbolIndex = slot2
                                }

                                if (matchedSymbolIndex != -1) {
                                    val multiplier = when (matchedSymbolIndex) {
                                        0 -> if (isThreeInRow) 100.0 else 3.0
                                        1 -> if (isThreeInRow) 60.0 else 2.0
                                        2 -> if (isThreeInRow) 40.0 else 1.5
                                        3 -> if (isThreeInRow) 30.0 else 1.0
                                        4 -> if (isThreeInRow) 25.0 else 1.0
                                        5 -> if (isThreeInRow) 20.0 else 0.5
                                        6 -> if (isThreeInRow) 15.0 else 0.5
                                        else -> 0.0
                                    }
                                    winReward = bet * multiplier
                                }

                                val finalWin = winReward.toInt()
                                if (finalWin > 0) {
                                    balance += finalWin
                                    if (finalWin > maxWin) maxWin = finalWin

                                    val newRecord = WinRecord(
                                        id = System.currentTimeMillis(),
                                        amount = finalWin,
                                        isVisibleState = mutableStateOf(true)
                                    )
                                    winRecords.add(newRecord)

                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        newRecord.isVisible = false
                                        kotlinx.coroutines.delay(500)
                                        winRecords.remove(newRecord)
                                    }
                                }

                                if (bet > balance) {
                                    bet = balance
                                }
                                saveCasinoData(balance, maxWin)
                            }
                        }
                    }
                },
                enabled = !isSpinning && bet > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE94560),
                    disabledContainerColor = Color(0xFF552233)
                ),
                modifier = Modifier.width(240.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSpinning) "Крутим..." else "КРУТИТЬ",
                    fontSize = 18.sp,
                    color = if (bet > 0 || isSpinning) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Таймер утешительного приза
            var lastBonusTime by remember { mutableStateOf(sharedPreferences.getLong("last_bonus_time", 0L)) }
            var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

            LaunchedEffect(balance, bet) {
                while (balance < 10 && bet == 0) {
                    currentTime = System.currentTimeMillis()
                    kotlinx.coroutines.delay(1000)
                }
            }

            if (balance < 10 && bet == 0) {
                val timePassed = currentTime - lastBonusTime
                val cooldown = 30000L
                val isReady = timePassed >= cooldown
                val secondsLeft = ((cooldown - timePassed) / 1000).coerceAtLeast(0)

                Button(
                    onClick = {
                        if (isReady) {
                            balance += 100
                            lastBonusTime = System.currentTimeMillis()
                            sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime).apply()
                            saveCasinoData(balance, maxWin)
                        }
                    },
                    enabled = isReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF2E4F32)
                    ),
                    modifier = Modifier.width(240.dp)
                ) {
                    Text(
                        text = if (isReady) "Взять +100 монет 🎁" else "Бонус через ${secondsLeft}с ⏳",
                        fontSize = 14.sp,
                        color = if (isReady) Color.White else Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Назад в меню",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { if (!isSpinning) onBackToMenu() }
            )
        }
    }
}
