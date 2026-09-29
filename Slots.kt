package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val TsDarkCardBg = Color(0xFF0F0C20)
private val TsGoldAccent = Color(0xFFFFD700)
private val TsNeonGreen = Color(0xFF00FF7F)
private val TsNeonRed = Color(0xFFE94560)
private val TsNeonCyan = Color(0xFF00E5FF)

// ===== Палитра подсветки для каждого эмодзи =====
// 0 → 7️⃣  1 → 💎  2 → 🔔  3 → 🍉  4 → 🍇  5 → 🍋  6 → 🍒
private val SlotNeonColors = listOf(
    Color(0xFFCE2E5B),
    Color(0xFF6FE7FF),
    Color(0xFFFFD700),
    Color(0xFFFF5C7A),
    Color(0xFFB26BFF),
    Color(0xFFFFF56B),
    Color(0xFFE63946)
)

private val SlotIdleBorder = Color(0xFF3A3F58)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThirdScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var maxWin by remember { mutableStateOf(sharedPreferences.getInt("max_win", 0)) }
    // ФРИСПИНЫ ТЕПЕРЬ ПЕРЕМЕННАЯ, ЧТОБЫ ОБНОВЛЯТЬСЯ ПОСЛЕ РЕКЛАМЫ
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }
    var bet by remember { mutableStateOf(0) }

    val winRecords = remember { mutableStateListOf<WinRecord>() }
    val slotEmojis = listOf("7️⃣", "💎", "🔔", "🍉", "🍇", "🍋", "🍒")

    var slot1 by remember { mutableStateOf(0) }
    var slot2 by remember { mutableStateOf(1) }
    var slot3 by remember { mutableStateOf(2) }

    var isSpinning by remember { mutableStateOf(false) }
    var isAutoSpinActive by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val animOffsetY1 = remember { Animatable(0f) }
    val animOffsetY2 = remember { Animatable(0f) }
    val animOffsetY3 = remember { Animatable(0f) }

    var winningEmojiIndex by remember { mutableStateOf(-1) }
    var isWinHighlight by remember { mutableStateOf(false) }
    var isBackgroundHighlight by remember { mutableStateOf(false) }
    var isFullScreenHighlight by remember { mutableStateOf(false) }

    fun saveCasinoData(newBalance: Int, newMaxWin: Int) {
        sharedPreferences.edit()
            .putInt("balance", newBalance)
            .putInt("max_win", newMaxWin)
            .apply()
    }

    fun saveFreeSpins(value: Int) {
        sharedPreferences.edit().putInt("free_spins", value).apply()
    }

    var lastBonusTime by remember {
        mutableStateOf(sharedPreferences.getLong("last_bonus_time", 0L))
    }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    val activeHighlightColor = if (winningEmojiIndex in slotEmojis.indices) {
        SlotNeonColors[winningEmojiIndex]
    } else TsGoldAccent

    val slotBorderColor by animateColorAsState(
        targetValue = if (isWinHighlight) activeHighlightColor else SlotIdleBorder,
        animationSpec = tween(400),
        label = "SlotBorderAnim"
    )
    val slotShadow by animateDpAsState(
        targetValue = if (isWinHighlight) 26.dp else 0.dp,
        animationSpec = tween(400),
        label = "SlotShadowAnim"
    )
    val slotBackgroundColor by animateColorAsState(
        targetValue = if (isBackgroundHighlight) activeHighlightColor.copy(alpha = 0.10f) else Color(0xFF0F0C20),
        animationSpec = tween(400),
        label = "SlotBgAnim"
    )

    val fullScreenFlashAlpha by animateFloatAsState(
        targetValue = if (isFullScreenHighlight) 0.14f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "FullScreenFlashAlpha"
    )

    // ===== ФУНКЦИЯ ОДНОГО СПИНА =====
    fun runSpinCasino() {
        if (isSpinning || bet <= 0 || balance < bet) return

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

                    winningEmojiIndex = matchedSymbolIndex
                    isWinHighlight = true
                    isBackgroundHighlight = isThreeInRow
                    isFullScreenHighlight = isThreeInRow

                    launch {
                        kotlinx.coroutines.delay(2000)
                        isWinHighlight = false
                        isBackgroundHighlight = false
                        isFullScreenHighlight = false
                    }

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

    // ===== АВТОПРОКРУТ =====
    LaunchedEffect(isAutoSpinActive, isSpinning, bet, balance) {
        if (isAutoSpinActive && !isSpinning) {
            if (bet <= 0 || balance < bet) {
                isAutoSpinActive = false
            } else {
                delay(600)
                if (isAutoSpinActive) runSpinCasino()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))))
    ) {

        // ===== ПОДСВЕТКА ВСЕГО ЗАДНЕГО ФОНА =====
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(activeHighlightColor.copy(alpha = fullScreenFlashAlpha))
        )

        // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
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
                    Box(
                        modifier = Modifier.width(1.dp).height(30.dp)
                            .background(Color(0xFF3A3F58))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ФРИСПИНЫ 🎁",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedContent(
                            targetState = freeSpins,
                            transitionSpec = {
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            }
                        ) { animatedSpins ->
                            Text(
                                text = "$animatedSpins",
                                fontSize = 20.sp,
                                color = TsNeonCyan,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // ===== ИГРОВОЙ АВТОМАТ =====
        Card(
            modifier = Modifier
                .size(340.dp, 150.dp)
                .align(Alignment.Center)
                .offset(y = (-30).dp)
                .shadow(slotShadow, RoundedCornerShape(24.dp))
                .border(2.dp, slotBorderColor, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = slotBackgroundColor),
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
                        modifier = Modifier
                            .size(80.dp)
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
                                enter = slideInVertically { h -> h } + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically { h -> -h } + fadeOut(animationSpec = tween(500))
                            ) {
                                Text(
                                    text = "+${record.amount} 💰",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TsGoldAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TsDarkCardBg),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(TsGoldAccent, Color(0xFFFFA751))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🎰 УПРАВЛЕНИЕ СТАВКОЙ",
                        color = TsGoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1730)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TsGoldAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "СТАВКА:  ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            AnimatedContent(
                                targetState = bet,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        slideInVertically { h -> -h } + fadeIn() togetherWith
                                                slideOutVertically { h -> h } + fadeOut()
                                    } else {
                                        slideInVertically { h -> h } + fadeIn() togetherWith
                                                slideOutVertically { h -> -h } + fadeOut()
                                    }
                                },
                                label = "BetAnim"
                            ) { animatedBet ->
                                Text(
                                    text = "$animatedBet 💰",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TsGoldAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val betSteps = listOf(-1000, -100, -10, 10, 100, 1000)
                        betSteps.forEach { step ->
                            val isIncrease = step > 0
                            val accent = if (isIncrease) TsNeonGreen else TsNeonRed
                            val isEnabled = !isSpinning && !isAutoSpinActive &&
                                    (if (step < 0) bet > 0 else bet < balance)

                            Button(
                                onClick = {
                                    bet = if (step < 0) {
                                        (bet + step).coerceAtLeast(0)
                                    } else {
                                        (bet + step).coerceAtMost(balance)
                                    }
                                },
                                enabled = isEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A1730),
                                    disabledContainerColor = Color(0xFF1A1730)
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isEnabled) accent.copy(alpha = 0.85f) else Color(0xFF3A3F58),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = if (isIncrease) "+$step" else "$step",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isEnabled) accent else Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val autoAccent = TsNeonCyan
                        Button(
                            onClick = { isAutoSpinActive = !isAutoSpinActive },
                            enabled = bet > 0 && (balance >= bet || isAutoSpinActive),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A1730),
                                disabledContainerColor = Color(0xFF1A1730)
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(
                                    width = if (isAutoSpinActive) 2.dp else 1.dp,
                                    color = if (isAutoSpinActive) autoAccent else Color(0xFF3A3F58),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = if (isAutoSpinActive) "СТОП 🛑" else "АВТО 🔄",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAutoSpinActive) autoAccent else Color.White
                            )
                        }

                        Button(
                            onClick = { runSpinCasino() },
                            enabled = !isSpinning && !isAutoSpinActive && bet > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TsNeonRed,
                                disabledContainerColor = Color(0xFF552233)
                            ),
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSpinning) "Крутим..." else "КРУТИТЬ 🎰",
                                fontSize = 16.sp,
                                color = if (bet > 0 || isSpinning) Color.White else Color.Gray,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var showAdScreen by remember { mutableStateOf(false) }
                    var adTimerSeconds by remember { mutableStateOf(15) }

                    // ===== РЕКЛАМА: ЗА 15 СЕКУНД ДАЁТ +3 ФРИСПИНА =====
                    LaunchedEffect(showAdScreen) {
                        if (showAdScreen) {
                            adTimerSeconds = 15
                            while (adTimerSeconds > 0) {
                                delay(1000L)
                                adTimerSeconds--
                            }
                            // НАГРАДА: +3 ФРИСПИНА
                            freeSpins += 3
                            saveFreeSpins(freeSpins)

                            lastBonusTime = System.currentTimeMillis()
                            sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime).apply()
                            showAdScreen = false
                        }
                    }

                    if (showAdScreen) {
                        Dialog(
                            onDismissRequest = { /* Запрещаем закрывать */ },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF000000))
                            ) {
                                // Пасхалка — тоже +3 фриспина
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopStart)
                                        .clickable {
                                            freeSpins += 3
                                            saveFreeSpins(freeSpins)

                                            lastBonusTime = System.currentTimeMillis()
                                            sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime).apply()
                                            showAdScreen = false
                                        }
                                )

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ЭТО РЕКЛАМА",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF222222),
                                        letterSpacing = 4.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Награда через: $adTimerSeconds с",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "🎁 +3 фриспина",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TsNeonCyan
                                    )
                                }
                            }
                        }
                    }

                    if (balance < 10 && bet == 0 && !isAutoSpinActive) {
                        val timePassed = currentTime - lastBonusTime
                        val cooldown = 50000L
                        val isReady = timePassed >= cooldown
                        val secondsLeft = ((cooldown - timePassed) / 1000).coerceAtLeast(0)

                        Button(
                            onClick = {
                                if (isReady) showAdScreen = true
                            },
                            enabled = isReady && !showAdScreen,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color(0xFF2E4F32)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isReady) "Взять +3 фриспина 🎁" else "Бонус через ${secondsLeft}с ⏳",
                                fontSize = 13.sp,
                                color = if (isReady) Color.White else Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = "Назад в меню",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { if (!isSpinning && !showAdScreen) onBackToMenu() }
                    )
                }
            }
        }
    }
}