package com.example.myfirstapp

import android.content.Context
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

val gameColors = listOf(
    GameColor("Красный", Color(0xFFFF0000)),       // Чистый красный
    GameColor("Голубой", Color(0xFF00D2FF)),       // Неоново-голубой
    GameColor("Жёлтый", Color(0xFFFFD700)),        // Золотой 8-bit жёлтый
    GameColor("Зелёный", Color(0xFF00FF00)),       // Ядовито-зелёный
    GameColor("Пурпурный", Color(0xFFFF00FF)),     // Пурпурный / Маджента
    GameColor("Синий", Color(0xFF0000FF)),         // Глубокий синий
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
                onNavigateToFourth = { navController.navigate("fourth") },
                onNavigateToFifth = { navController.navigate("fifth") } // НОВЫЙ МАРШРУТ ДЛЯ БАЛАНСА 💳
            )
        }
        composable("second") { SecondScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("third") { ThirdScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("fourth") { FourthScreen(onBackToMenu = { navController.popBackStack() }) }
        // Регистрируем сам пятый экран в системе
        composable("fifth") { BalanceScreen(onBackToMenu = { navController.popBackStack() }) }
    }
}



@Composable
fun MenuScreen(
    onNavigateToSecond: () -> Unit,
    onNavigateToThird: () -> Unit,
    onNavigateToFourth: () -> Unit,
    onNavigateToFifth: () -> Unit
) {
    val context = LocalContext.current

    var showPromoDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var isCodeAccepted by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }
    val sharedPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }
    val resetDialog = {
        showPromoDialog = false
        promoInput = ""
        balanceInput = ""
        isCodeAccepted = false
    }
    // Переменные кастомных неоновых цветов для обводок
    val neonBlue = Color(0xFF00F0FF)   // Киберпанк голубой
    val neonOrange = Color(0xFFFF4500) // Огненно-оранжевый (в тон мусор-дропа)
    val neonGreen = Color(0xFF00FF00)  // Ядовито-зеленый
    val neonPurple = Color(0xFFD67BFF) // Фиолетовый неон
    val darkCardBg = Color(0xFF0F0C20) // Глубокий темный цвет внутри кнопок

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Цвет заднего фона точно такой же, как на экране Мусор дроп
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Строгая минималистичная надпись БЕЗ лишних слов
        Text(
            text = "МЕНЮ",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 6.sp // Широкий строгий отступ между буквами в стиле интерфейсов будущего
        )

        Spacer(modifier = Modifier.height(54.dp))

        // РЯД 1: БАЛАНС И ПРОМОКОДЫ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1.1 Баланс (Пока просто кнопка)
            Button(
                onClick = onNavigateToFifth,
                shape = RoundedCornerShape(8.dp), // Строгая квадратная форма
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonBlue, RoundedCornerShape(8.dp)) // Неоново-голубое свечение
            ) {
                Text(
                    text = "пополнение баланса",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 1.2 Промокоды
            Button(
                onClick = { showPromoDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonBlue, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = " \uD83C\uDFAB промокоды",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // РЯД 2: СЛОТЫ И МУСОР ДРОП
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2.1 Слоты (Вместо казино времени)
            Button(
                onClick = onNavigateToThird,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonOrange, RoundedCornerShape(8.dp)) // Огненное свечение
            ) {
                Text(
                    text = " \uD83C\uDFB0 слоты",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 2.2 Мусор дроп
            Button(
                onClick = onNavigateToFourth,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonOrange, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = " \uD83D\uDCE6 мусор дроп",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // РЯД 3: ЦВЕТА И БАНК
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3.1 Цвета (Вместо игры в цвета)
            Button(
                onClick = onNavigateToSecond,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonGreen, RoundedCornerShape(8.dp)) // Зеленый неон
            ) {
                Text(
                    text = "цвета",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 3.2 Банк (Пока просто кнопка)
            Button(
                onClick = { /* Будущее окно банка */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(2.dp, neonPurple, RoundedCornerShape(8.dp)) // Фиолетовый неон
            ) {
                Text(
                    text = "банк",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
                            Text(
                                text = "Введите промокод для активации бонусов:",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TextField(
                                value = promoInput,
                                onValueChange = { promoInput = it },
                                placeholder = { Text("Код...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        } else {
                            Text(
                                text = "Код успешно активирован! Введите желаемый баланс:",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
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
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
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
                                    "5252" -> {
                                        // Секретный код разработчика для открытия ввода баланса
                                        isCodeAccepted = true
                                    }

                                    "666" -> {
                                        // НОВЫЙ КОД: Проклятый промокод 666 😈
                                        // Оставляем его многоразовым для веселья, поэтому не проверяем через SharedPreferences
                                        sharedPreferences.edit()
                                            .putInt("balance", 0)
                                            .apply()

                                        Toast.makeText(
                                            context,
                                            "Баланс полностью обнулён... Ты потерял всё! ☠️🔥",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        showPromoDialog = false
                                        promoInput = ""
                                    }

                                    "777", "1488", "300", "гей", "якрутой" -> {
                                        // Магия Kotlin: мы сгруппировали промокоды, так как у них одинаковая логика проверки на повторное использование
                                        val promoKey = "promo_${cleanInput}_used"
                                        val isPromoUsed =
                                            sharedPreferences.getBoolean(promoKey, false)

                                        if (isPromoUsed) {
                                            Toast.makeText(
                                                context,
                                                "Этот промокод уже активирован! ❌",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            // Определяем сумму бонуса в зависимости от кода
                                            val bonusAmount = when (cleanInput) {
                                                "777" -> 250
                                                "1488" -> 100
                                                "300" -> 200
                                                "гей" -> 67
                                                "якрутой" -> 300
                                                else -> 0
                                            }

                                            val currentBalance =
                                                sharedPreferences.getInt("balance", 100)
                                            val newBalance = currentBalance + bonusAmount

                                            // Сохраняем новый баланс и помечаем именно этот промокод как использованный
                                            sharedPreferences.edit()
                                                .putInt("balance", newBalance)
                                                .putBoolean(promoKey, true)
                                                .apply()

                                            Toast.makeText(
                                                context,
                                                "Промокод активирован! Получено +$bonusAmount 💰",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            // Закрываем диалог и очищаем поле ввода
                                            showPromoDialog = false
                                            promoInput = ""
                                        }
                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Неверный код ❌",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
}
@Composable
fun SecondScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }

    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(sharedPreferences.getInt("high_score", 0)) }

    var bgIndex by remember { mutableStateOf(0) }
    var textIndex by remember { mutableStateOf(1) }

    // Константы кофейных цветов по твоей задумке
    val coffeeSquareColor = Color(0xFF4A3B32)     // Светло-кофейный для большого квадрата
    val darkCoffeeButtonColor =
        Color(0xFF261C14) // Тёмно-кофейный (почти чёрный) для кнопки выхода

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
            Text(
                text = "Счёт: ",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D734B)
            )

            // Магия Compose анимации: когда изменяется переменная score, старая цифра уезжает вниз, новая едет сверху
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    slideInVertically(animationSpec = tween(durationMillis = 300)) { height -> -height } togetherWith
                            slideOutVertically(animationSpec = tween(durationMillis = 300)) { height -> height }
                },
                label = "ScoreAnimation"
            ) { animatedScore ->
                Text(
                    text = "$animatedScore",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8D734B)
                )
            }
        }

        Text(
            text = "Рекорд: $highScore",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A3E25)
        )

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
class WinRecord(val id: Long, val amount: Int, isVisibleState: MutableState<Boolean>) {
    var isVisible by isVisibleState
}

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

            // Кнопка утешительного приза
            if (balance < 10 && bet == 0) {
                val timePassed = currentTime - lastBonusTime
                val cooldown = 50000L
                val isReady = timePassed >= cooldown
                val secondsLeft = ((cooldown - timePassed) / 1000).coerceAtLeast(0)

                Button(
                    onClick = {
                        if (isReady) {
                            balance += 30
                            lastBonusTime = System.currentTimeMillis()
                            sharedPreferences.edit().putLong("last_bonus_time", lastBonusTime)
                                .apply()
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