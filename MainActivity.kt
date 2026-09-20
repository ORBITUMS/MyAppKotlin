package com.example.myfirstapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
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

data class GameColor(val name: String, val color: Color)
// Настоящая, сочная 8-битная палитра из 9 цветов
val gameColors = listOf(
    GameColor("Красный", Color(0xFFFF0000)),       // Чистый красный
    GameColor("Голубой", Color(0xFF00D2FF)),       // Неоново-голубой
    GameColor("Жёлтый", Color(0xFFFFD700)),        // Золотой 8-bit жёлтый
    GameColor("Зелёный", Color(0xFF00FF00)),       // Ядовито-зелёный
    GameColor("Пурпурный", Color(0xFFFF00FF)),     // Пурпурный / Маджента
    GameColor("Синий", Color(0xFF0000FF)),         // Глубокий синий
    // Твои 3 новых цвета:
    GameColor("Чёрный", Color(0xFF1A1A1A)),        // Мягкий чёрный (чтобы текст внутри был виден)
    GameColor("Фиолетовый", Color(0xFF4B0082)), // Тёмно-фиолетовый (Индиго)
    GameColor("Розовый", Color(0xFFFF69B4))        // Ярко-розовый
)


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                onNavigateToSecond = { navController.navigate("second") },
                onNavigateToThird = { navController.navigate("third") },
                onNavigateToFourth = { navController.navigate("fourth") } // НОВЫЙ МАРШРУТ 🚚
            )
        }
        composable("second") { SecondScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("third") { ThirdScreen(onBackToMenu = { navController.popBackStack() }) }
        // Регистрируем сам четвертый экран (создай функцию FourthScreen ниже по коду аналогично другим)
        composable("fourth") { FourthScreen(onBackToMenu = { navController.popBackStack() }) }
    }
}


@Composable
fun MenuScreen(
    onNavigateToSecond: () -> Unit,
    onNavigateToThird: () -> Unit,
    onNavigateToFourth: () -> Unit // Добавили новый аргумент для Мусор дропа
) {
    val context = LocalContext.current

    // ... Твои состояния и resetDialog остаются БЕЗ ИЗМЕНЕНИЙ ...
    var showPromoDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var isCodeAccepted by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }
    val sharedPreferences = remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }
    val resetDialog = {
        showPromoDialog = false
        promoInput = ""
        balanceInput = ""
        isCodeAccepted = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(richLightGradient),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Главное меню", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3E25))
        Spacer(modifier = Modifier.height(40.dp))

        // 1. Игра в цвета
        Button(onClick = onNavigateToSecond, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D734B)), modifier = Modifier.width(220.dp)) {
            Text(text = "игра в цвета", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Казино времени
        Button(onClick = onNavigateToThird, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)), modifier = Modifier.width(220.dp)) {
            Text(text = "🎰 Казино времени", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 3. НОВАЯ КНОПКА: Мусор дроп
        Button(
            onClick = onNavigateToFourth,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E5D4C)), // Кастомный болотный/зеленый цвет
            modifier = Modifier.width(220.dp)
        ) {
            Text(text = "📦 Мусор дроп", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 4. НОВАЯ КНОПКА: Переход на сайт по ссылке
        Button(
            onClick = {
                // Механизм Intent открывает браузер поверх твоего приложения
                val websiteIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=7O-fpUwUtOI") // СЮДА ВПИШИ СВОЮ ССЫЛКУ (обязательно с http:// или https://)
                )
                context.startActivity(websiteIntent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)), // Синий премиальный цвет
            modifier = Modifier.width(220.dp)
        ) {
            Text(text = "игра тянка и карты", fontSize = 16.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 5. Промокоды
        Button(onClick = { showPromoDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3E25)), modifier = Modifier.width(220.dp)) {
            Text(text = "🎫 Промокоды", fontSize = 16.sp, color = Color.White)
        }
    }
    if (showPromoDialog) {
        AlertDialog(
            onDismissRequest = resetDialog,
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
                        Text(text = "Код успешно активирован! Введите желаемый баланс:", modifier = Modifier.padding(bottom = 8.dp))
                        TextField(
                            value = balanceInput,
                            // Ограничиваем ввод 7 цифрами, чтобы избежать переполнения Int (защита от краша/бага)
                            onValueChange = { input ->
                                if (input.length <= 7) {
                                    balanceInput = input.filter { it.isDigit() }
                                }
                            },
                            placeholder = { Text("...") },
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
                        if (!isCodeAccepted) {
                            val cleanInput = promoInput.trim()

                            when (cleanInput) {
                                "7773" -> {
                                    // Секретный код разработчика для открытия ввода баланса
                                    isCodeAccepted = true
                                }
                                "666" -> {
                                    // НОВЫЙ КОД: Проклятый промокод 666 😈
                                    // Оставляем его многоразовым для веселья, поэтому не проверяем через SharedPreferences
                                    sharedPreferences.edit()
                                        .putInt("balance", 0)
                                        .apply()

                                    Toast.makeText(context, "Баланс полностью обнулён... Ты потерял всё! ☠️🔥", Toast.LENGTH_LONG).show()

                                    showPromoDialog = false
                                    promoInput = ""
                                }
                                "777", "1488", "52", "паша" -> {
                                    // Магия Kotlin: мы сгруппировали промокоды, так как у них одинаковая логика проверки на повторное использование
                                    val promoKey = "promo_${cleanInput}_used"
                                    val isPromoUsed = sharedPreferences.getBoolean(promoKey, false)

                                    if (isPromoUsed) {
                                        Toast.makeText(context, "Этот промокод уже активирован! ❌", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Определяем сумму бонуса в зависимости от кода
                                        val bonusAmount = when (cleanInput) {
                                            "777" -> 250
                                            "1488" -> 100  // Твой новый промокод на +100 💰
                                            "52" -> 52     // Твой новый промокод на +52 💰
                                            "паша" -> 102
                                            else -> 0
                                        }

                                        val currentBalance = sharedPreferences.getInt("balance", 100)
                                        val newBalance = currentBalance + bonusAmount

                                        // Сохраняем новый баланс и помечаем именно этот промокод как использованный
                                        sharedPreferences.edit()
                                            .putInt("balance", newBalance)
                                            .putBoolean(promoKey, true)
                                            .apply()

                                        Toast.makeText(context, "Промокод активирован! Получено +$bonusAmount 💰", Toast.LENGTH_LONG).show()

                                        // Закрываем диалог и очищаем поле ввода
                                        showPromoDialog = false
                                        promoInput = ""
                                    }
                                }
                                else -> {
                                    Toast.makeText(context, "Неверный код ❌", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Здесь остаётся твой старый код применения баланса из режима разработчика (для кода 7772)
                            val newBalance = balanceInput.toIntOrNull() ?: 0
                            sharedPreferences.edit().putInt("balance", newBalance).apply()

                            Toast.makeText(
                                context,
                                "Баланс успешно изменён на $newBalance 💰",
                                Toast.LENGTH_SHORT
                            ).show()
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
                TextButton(onClick = resetDialog) {
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

    // Константы кофейных цветов по твоей задумке
    val coffeeSquareColor = Color(0xFF4A3B32)     // Светло-кофейный для большого квадрата
    val darkCoffeeButtonColor = Color(0xFF261C14) // Тёмно-кофейный (почти чёрный) для кнопки выхода

    val nextRound = {
        val newBg = Random.nextInt(gameColors.size)
        var newText = Random.nextInt(gameColors.size)
        // Гарантируем, что цвет круга и текст внутри не совпадут
        while (newText == newBg) {
            newText = Random.nextInt(gameColors.size)
        }
        bgIndex = newBg
        textIndex = newText
    }

    val onColorClick = { clickedIndex: Int ->
        if (clickedIndex == textIndex) {
            score++
            if (score > highScore) {
                highScore = score
                sharedPreferences.edit().putInt("high_score", highScore).apply()
            }
        } else {
            // НОВОЕ ПРАВИЛО: При ошибке счёт полностью сбрасывается в 0
            score = 0
        }
        nextRound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(richLightGradient)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Блок Счёта с плавной анимацией прокрутки цифр (Slide Down)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Счёт: ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D734B))

            // Магия Compose анимации: когда изменяется переменная score, старая цифра уезжает вниз, новая едет сверху
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    slideInVertically(animationSpec = tween(durationMillis = 300)) { height -> -height } togetherWith
                            slideOutVertically(animationSpec = tween(durationMillis = 300)) { height -> height }
                },
                label = "ScoreAnimation"
            ) { animatedScore ->
                Text(text = "$animatedScore", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D734B))
            }
        }

        Text(text = "Рекорд: $highScore", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4A3E25))

        Spacer(modifier = Modifier.weight(1f))

        // НОВОЕ: Большой Квадрат кофейного цвета
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(coffeeSquareColor, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Главный круг внутри квадрата
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(gameColors[bgIndex].color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // ИСПРАВЛЕНО: Теперь выводится строго название цвета, а не рекорд!
                Text(
                    text = gameColors[textIndex].name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Динамическая сетка кнопок (chunked(3) автоматически разделит 9 цветов на 3 ровных ряда по 3 кнопки!)
        val buttonRows = remember { gameColors.withIndex().chunked(3) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in buttonRows) {
                Row {
                    for ((index, gameColor) in row) {
                        SmallColorButton(
                            gameColor = gameColor,
                            onClick = { onColorClick(index) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ИСПРАВЛЕНО: Кнопка выхода теперь тёмно-кофейного (более чёрного) цвета
        Button(
            onClick = onBackToMenu,
            colors = ButtonDefaults.buttonColors(containerColor = darkCoffeeButtonColor),
            modifier = Modifier.width(260.dp)
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
            .clip(CircleShape) // Обрезаем клики и риппл-эффект по кругу
            .background(gameColor.color)
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

    // ТАЙМЕР УТЕШИТЕЛЬНОГО ПРИЗА (работает независимо в фоне)
    var lastBonusTime by remember { mutableStateOf(sharedPreferences.getLong("last_bonus_time", 0L)) }
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
            Text(text = "🎰 СЛОТ-МАШИНА", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFE259), Color(0xFFFFA751)))),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
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
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            }
                        ) { animatedBalance ->
                            Text(text = "$animatedBalance", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Black)
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
                        modifier = Modifier.size(80.dp).background(Color(0xFF1F1A3A), RoundedCornerShape(12.dp)),
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
                    Text(text = "СТАВКА: ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
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
                        Text(text = "$animatedBet 💰", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
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

            // Кнопка утешительного приза
            if (balance < 10 && bet == 0) {
                val timePassed = currentTime - lastBonusTime
                val cooldown = 30000L
                val isReady = timePassed >= cooldown
                val secondsLeft = ((cooldown - timePassed) / 1000).coerceAtLeast(0)

                Button(
                    onClick = {
                        if (isReady) {
                            balance += 30
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
                    .clickable { if (!isSpinning) onBackToMenu() }
            )
        }
    }
}
@Composable
fun FourthScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var betInput by remember { mutableStateOf("") }
    var isSpinning by remember { mutableStateOf(false) }

    val winRecords = remember { mutableStateListOf<WinRecord>() }
    val coroutineScope = rememberCoroutineScope()
    val needleAngle = remember { Animatable(0f) }

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
                Text(text = "БАЛАНС: ", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                // Добавили плавную вертикальную прокрутку цифр баланса
                AnimatedContent(
                    targetState = balance,
                    transitionSpec = {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    },
                    label = "BalanceAnimation"
                ) { animatedBalance ->
                    Text(text = "$animatedBalance 💰", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }

        // 2. ЦЕНТР: КОЛЕСО АПГРЕЙДА (Шоколадный центр, 8-bit зелёный + бело-молочный)
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val strokeWidth = 24.dp.toPx() // Сделали дорожку чуть толще для 8-битного стиля
                val radius = size.width / 2

                val bitGreenColor = Color(0xFF00FF00)
                val milkWhiteColor = Color(0xFFFFFDD0)
                val chocolateColor = Color(0xFF3D2314)
                // Вместо золотого D4AF37 ставим яркий неоново-голубой (Cyan)
                val neonBlueColor = Color(0xFF404040)

                // СЛОЙ 1: Цветные дуги (50 на 50). Рисуются строго по линии radius
                drawArc(
                    color = bitGreenColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = milkWhiteColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // СЛОЙ 2: Внутренний круг шоколадного цвета
                // Его радиус — это внутренний край цветной дорожки, без зазоров!
                val innerRadius = radius - (strokeWidth / 2)
                drawCircle(
                    color = chocolateColor,
                    radius = innerRadius,
                    center = center
                )

                // СЛОЙ 3: Белая стрелка, летящая строго ПО зелёному или молочному цвету
                val angleInRadians = (needleAngle.value * PI / 180f)

                // Стрелка начинается на внутреннем краю цветного кольца и заканчивается на внешнем
                val startX = center.x + innerRadius * cos(angleInRadians).toFloat()
                val startY = center.y + innerRadius * sin(angleInRadians).toFloat()

                val outerRadius = radius + (strokeWidth / 2)
                val endX = center.x + outerRadius * cos(angleInRadians).toFloat()
                val endY = center.y + outerRadius * sin(angleInRadians).toFloat()

                drawLine(
                    color = Color(0xFF1A0F0A), // ИСПРАВЛЕНО: Вместо Color.White поставили тёмный цвет
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 6.dp.toPx()
                )


                drawCircle(
                    color = neonBlueColor,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 6.dp.toPx())
                )
                drawCircle(
                    color = neonBlueColor,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )

            }


            // ИСПРАВЛЕНО: Текст по центру шоколадного круга
            Text(
                text = "50%",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
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
                                enter = slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)),
                                exit = slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(500))
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

            // Поле ввода ставки
            TextField(
                value = betInput,
                onValueChange = { input ->
                    if (input.length <= 6) {
                        betInput = input.filter { it.isDigit() }
                    }
                },
                placeholder = { Text("Сумма ставки...", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F0C20),
                    unfocusedContainerColor = Color(0xFF0F0C20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD4AF37)
                ),
                modifier = Modifier.width(240.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentBet = betInput.toIntOrNull() ?: 0
            val isBetValid = currentBet > 0 && currentBet <= balance

            Button(
                onClick = {
                    if (!isSpinning && isBetValid) {
                        isSpinning = true
                        balance -= currentBet
                        saveBalance(balance)

                        coroutineScope.launch {
                            val isWin = Random.nextBoolean()
                            val targetAngle = if (isWin) Random.nextInt(5, 175) else Random.nextInt(185, 355)
                            val totalRotation = 1440f + targetAngle

                            needleAngle.snapTo(needleAngle.value % 360f)

                            needleAngle.animateTo(
                                targetValue = totalRotation,
                                animationSpec = tween(durationMillis = 2000)
                            )

                            if (isWin) {
                                val winAmount = currentBet * 2
                                balance += winAmount

                                val visibilityState = mutableStateOf(true)
                                val newRecord = WinRecord(
                                    id = System.currentTimeMillis(),
                                    amount = winAmount,
                                    isVisibleState = visibilityState
                                )
                                winRecords.add(newRecord)

                                launch {
                                    kotlinx.coroutines.delay(2000)
                                    newRecord.isVisible = false
                                    kotlinx.coroutines.delay(500)
                                    winRecords.remove(newRecord)
                                }
                            }

                            saveBalance(balance)
                            isSpinning = false
                        }
                    }
                },
                enabled = !isSpinning && isBetValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8D734B),
                    disabledContainerColor = Color(0xFF3E3129)
                ),
                modifier = Modifier.width(240.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isSpinning) "АПГРЕЙД..." else "ЗАПУСТИТЬ АПГРЕЙД ⚡", fontWeight = FontWeight.Bold, color = Color.White)
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
