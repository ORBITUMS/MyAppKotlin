package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThirdScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

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

    // ТАЙМЕР УТЕШИТЕЛЬНОГО ПРИЗА (работает независимо в фоне)
    var lastBonusTime by remember {
        mutableStateOf(
            sharedPreferences.getLong(
                "last_bonus_time",
                0L
            )
        )
    }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    // ГЛАВНЫЙ КОНТЕЙНЕР ЭКРАНА
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))))
    ) {

        // 1. ВЕРХНЯЯ ПАНЕЛЬ (Прижата к верху экрана)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 40.dp), // Чуть уменьшили отступ, чтобы освободить место кнопкам
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎰 СЛОТ-МАШИНА",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFE259), Color(0xFFFFA751)))
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "МАКС. КУШ 🏆",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$maxWin",
                            fontSize = 20.sp,
                            color = Color(0xFFE94560),
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier.width(1.dp).height(30.dp)
                            .background(Color(0xFF3A3F58))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "БАЛАНС 💰",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedContent(
                            targetState = balance,
                            transitionSpec = {
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            }
                        ) { animatedBalance ->
                            Text(
                                text = "$animatedBalance",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // 2. ИГРОВОЙ АВТОМАТ (Строго по центру экрана)
        Card(
            modifier = Modifier
                .size(340.dp, 150.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            val slots = listOf(slot1, slot2, slot3)
            val animOffsets = listOf(animOffsetY1, animOffsetY2, animOffsetY3)

            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier.size(80.dp)
                            .background(Color(0xFF1F1A3A), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = slotEmojis[slots[i]],
                            fontSize = 42.sp,
                            modifier = Modifier.offset(y = animOffsets[i].value.dp)
                        )
                    }
                }
            }
        }

        // 3. БЛОК УПРАВЛЕНИЯ СНИЗУ (Прижат к самому низу экрана)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), // Отступ от физического низа экрана
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Премиальная лента выигрышей (сделали её чуть компактнее — 50dp, чтобы точно всё влезло)
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
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Окошко текущей ставки
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A3A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "СТАВКА: ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                    AnimatedContent(
                        targetState = bet,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            } else {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            }.using(androidx.compose.animation.SizeTransform(clip = false))
                        }
                    ) { animatedBet ->
                        Text(
                            text = "$animatedBet 💰",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }

            // Панель изменения ставок (-100, -10, +10, +100)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val betSteps = listOf(-100, -10, 10, 100)
                betSteps.forEach { step ->
                    Button(
                        onClick = {
                            bet = if (step < 0) {
                                (bet + step).coerceAtLeast(0)
                            } else {
                                (bet + step).coerceAtMost(balance)
                            }
                        },
                        enabled = !isSpinning && (if (step < 0) bet > 0 else bet < balance),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F58)),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(text = if (step > 0) "+$step" else "$step", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

                                    launch {
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
                modifier = Modifier.width(240.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSpinning) "Крутим..." else "КРУТИТЬ",
                    fontSize = 18.sp,
                    color = if (bet > 0 || isSpinning) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Состояния для нашей рекламы (создайте их в самом верху вашей @Composable функции, если хотите,
            // но для локального экрана можно оставить прямо перед кнопкой)
            var showAdScreen by remember { mutableStateOf(false) }
            var adTimerSeconds by remember { mutableStateOf(15) }

            // Логика таймера рекламы
            LaunchedEffect(showAdScreen) {
                if (showAdScreen) {
                    adTimerSeconds = 15
                    while (adTimerSeconds > 0) {
                        delay(1000L)
                        adTimerSeconds--
                    }
                    // Время вышло — выдаем честную награду
                    balance += 30
                    lastBonusTime = System.currentTimeMillis()
                    sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime).apply()
                    saveCasinoData(balance, maxWin)
                    showAdScreen = false
                }
            }

            // ОБНОВЛЕННЫЙ РЕКЛАМНЫЙ ЭКРАН (Теперь через Dialog — ничего не прыгает вверх!)
            if (showAdScreen) {
                Dialog(
                    onDismissRequest = { /* Запрещаем закрывать рекламу кликом мимо окна */ },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false // Позволяет окну растянуться на ВЕСЬ экран смартфона
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000)) // Глухой стильный чёрный экран
                    ) {
                        // ТАЙНАЯ ПАСХАЛКА: Сделали квадрат меньше (всего 24.dp) в самом углу
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopStart)
                                .clickable {
                                    // Мгновенный секретный пропуск
                                    balance += 30
                                    lastBonusTime = System.currentTimeMillis()
                                    sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime).apply()
                                    saveCasinoData(balance, maxWin)
                                    showAdScreen = false
                                }
                        )

                        // Центральный блок с таймером
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ЭТО РЕКЛАМА",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF222222), // Сделали надпись ещё более тусклой и строгой
                                letterSpacing = 4.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Получение бонуса через: $adTimerSeconds с",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Кнопка утешительного приза
            if (balance < 10 && bet == 0) {
                val timePassed = currentTime - lastBonusTime
                val cooldown = 50000L
                val isReady = timePassed >= cooldown
                val secondsLeft = ((cooldown - timePassed) / 1000).coerceAtLeast(0)

                Button(
                    onClick = {
                        if (isReady) {
                            // Вместо моментальной выдачи запускаем рекламное окно
                            showAdScreen = true
                        }
                    },
                    enabled = isReady && !showAdScreen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF2E4F32)
                    ),
                    modifier = Modifier.width(240.dp).height(40.dp),
                    contentPadding = PaddingValues(0.0.dp)
                ) {
                    Text(
                        text = if (isReady) "Взять +30 монет 🎁" else "Бонус через ${secondsLeft}с ⏳",
                        fontSize = 13.sp,
                        color = if (isReady) Color.White else Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Назад в меню
            Text(
                text = "Назад в меню",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { if (!isSpinning && !showAdScreen) onBackToMenu() }
            )
        }
    }
}

