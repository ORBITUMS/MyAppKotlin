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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                onNavigateToSecond = { navController.navigate("second") },
                onNavigateToThird = { navController.navigate("third") },
                onNavigateToFourth = { navController.navigate("fourth") },
                onNavigateToFifth = { navController.navigate("fifth") },
                onNavigateToSixth = { navController.navigate("sixth") },
                onNavigateToSeventh = { navController.navigate("seventh") },
                onNavigateToEighth = { navController.navigate("eighth") }
            )
        }
        composable("second") { SecondScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("third") { ThirdScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("fourth") { FourthScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("seventh") { DonateScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("fifth") { BalanceScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("sixth") { InfoScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("eighth") { Bank(onBackToMenu = { navController.popBackStack() }) }
    }
}



@Composable
fun MenuScreen(
    onNavigateToSecond: () -> Unit,
    onNavigateToThird: () -> Unit,
    onNavigateToFourth: () -> Unit,
    onNavigateToFifth: () -> Unit,
    onNavigateToSixth: () -> Unit,
    onNavigateToSeventh: () -> Unit,
    onNavigateToEighth: () -> Unit
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
            onClick = onNavigateToEighth,
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

    Spacer(modifier = Modifier.height(16.dp))
    // РЯД 4: ИНФО/ССЫЛКИ И ДОНАТ 🚀 (НОВЫЙ РЯД)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 4.1 ТГК и Ссылки (Используем строгий белый/серый неон или любой другой)
        Button(
            onClick = onNavigateToSixth, // Ведет на экран с ТГК и ссылками
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .border(2.dp, Color.Cyan, RoundedCornerShape(8.dp)) // Бирюзовый/Циан неон
        ) {
            Text(
                text = "📢 инфо & ссылки",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 4.2 Донат
        Button(
            onClick = onNavigateToSeventh,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp)) // Золотое свечение
        ) {
            Text(
                text = "❤️ донат",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
} // Конец Column

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
                                    // Секретный код разработчика: открывает ввод баланса И СРАЗУ снимает КД с банка!
                                    sharedPreferences.edit()
                                        .putLong("bank_banned_until", 0L)
                                        .apply()
                                    isCodeAccepted = true
                                }

                                "666" -> {
                                    val banTimeEnd = System.currentTimeMillis() + (10 * 60 * 1000L) // +10 минут

                                    sharedPreferences.edit()
                                        .putInt("balance", 0)
                                        .putBoolean("loan_approved", false)
                                        .putLong("loan_debt", 0L)
                                        .putString("loan_borrower", "")
                                        .putLong("loan_last_time", 0L)
                                        .putLong("bank_banned_until", banTimeEnd) // Ставим КД на банк
                                        .apply()

                                    Toast.makeText(
                                        context,
                                        "Ты начал жизнь с чистого листа! ☠️🔥📜🔒",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    showPromoDialog = false
                                    promoInput = ""
                                }
                                "777", "1488", "2026", "гей", "Мусор без дропа", "додеп" -> {
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
                                            "777" -> 100
                                            "1488" -> 100
                                            "2026" -> 100
                                            "гей" -> 67
                                            "Мусор без дропа" -> 250
                                            "додеп" -> 444
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
                            // Здесь остаётся твой старый код применения баланса из режима разработчика
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


