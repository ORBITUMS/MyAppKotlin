package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

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
                onNavigateToEighth = { navController.navigate("eighth") },
                onNavigateToNineth = { navController.navigate("nineth") },
                onNavigateToTenth = { navController.navigate("tenth") }
            )
        }
        composable("second") { SecondScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("third") { ThirdScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("fourth") { FourthScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("seventh") { DonateScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("fifth") { BalanceScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("sixth") { InfoScreen(onBackToMenu = { navController.popBackStack() }) }
        composable("eighth") { Bank(onBackToMenu = { navController.popBackStack() }) }
        composable("nineth") { FreeSpinsScreen(onBackToMenuFr = { navController.popBackStack() }) }
        composable("tenth") { MultiplayerScreen(onBackToMenu = { navController.popBackStack() }) }
    }
}

// ===================== ТЕМАТИЧЕСКИЕ ЦВЕТА =====================
private val DarkCardBg = Color(0xFF0F0C20)
private val GoldAccent = Color(0xFFFFD700)
private val NeonBlue = Color(0xFF00F0FF)
private val NeonOrange = Color(0xFFFF4500)
private val NeonGreen = Color(0xFF00FF7F)
private val NeonPurple = Color(0xFFD67BFF)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonRed = Color(0xFFFF3366)

// ===================== СПИСОК ПРОМОКОДОВ =====================
// Чтобы добавить новый промокод — просто добавь строку в список ниже.
//
// Поля:
//   code      — сам код (чувствителен к регистру)
//   bonusCash — сколько добавить к балансу (0, если не нужен)
//   bonusSpins— сколько добавить фриспинов (0, если не нужно)
//   oneTime   — true = можно активировать только один раз, false = многоразовый
data class PromoCode(
    val code: String,
    val bonusCash: Int = 0,
    val bonusSpins: Int = 0,
    val oneTime: Boolean = true
)

val PROMO_CODES: List<PromoCode> = listOf(
    // --- Денежные промокоды ---
    PromoCode("777", bonusSpins = 10),
    PromoCode("1488", bonusCash = 100),
    PromoCode("2026", bonusCash = 100),
    PromoCode("гей", bonusSpins = 5),
    PromoCode("мусор без дропа", bonusCash = 250),
    PromoCode("додеп", bonusCash = 444),
    PromoCode("додеп2", bonusSpins = 20),

    // --- Промокоды на фриспины ---
    PromoCode("гей2", bonusSpins = 20),
    PromoCode("free", bonusSpins = 10),
    PromoCode("паша", bonusSpins = 67)
)

@Composable
fun MenuScreen(
    onNavigateToSecond: () -> Unit,
    onNavigateToThird: () -> Unit,
    onNavigateToFourth: () -> Unit,
    onNavigateToFifth: () -> Unit,
    onNavigateToSixth: () -> Unit,
    onNavigateToSeventh: () -> Unit,
    onNavigateToEighth: () -> Unit,
    onNavigateToNineth: () -> Unit,
    onNavigateToTenth: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
    }

    // ===================== ГЛОБАЛЬНЫЕ СЧЁТЧИКИ =====================
    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }

    fun refreshCounters() {
        balance = sharedPreferences.getInt("balance", 100)
        freeSpins = sharedPreferences.getInt("free_spins", 0)
    }

    var showPromoDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var isCodeAccepted by remember { mutableStateOf(false) }
    var balanceInput by remember { mutableStateOf("") }
    var spinsInput by remember { mutableStateOf("") }

    val resetDialog = {
        showPromoDialog = false
        promoInput = ""
        balanceInput = ""
        spinsInput = ""
        isCodeAccepted = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        // ===================== ВЕРХНЯЯ ПАНЕЛЬ — остаётся сверху =====================
        TopBarCasino(
            balance = balance,
            freeSpins = freeSpins
        )

        // ===================== ЦЕНТРАЛЬНЫЙ БЛОК С КНОПКАМИ =====================
        // weight(1f) заставляет занять всё оставшееся место под шапкой,
        // а verticalArrangement = Center центрирует содержимое по вертикали.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CasinoButton(
                    emoji = "💳",
                    text = "Пополнение",
                    accent = NeonGreen,
                    onClick = onNavigateToFifth,
                    modifier = Modifier.weight(1f)
                )
                CasinoButton(
                    emoji = "🎫",
                    text = "Промокоды",
                    accent = NeonBlue,
                    onClick = { showPromoDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CasinoButton(
                    emoji = "🎰",
                    text = "Слоты",
                    accent = NeonOrange,
                    onClick = onNavigateToThird,
                    modifier = Modifier.weight(1f)
                )
                CasinoButton(
                    emoji = "📦",
                    text = "Мусор Дроп",
                    accent = NeonOrange,
                    onClick = onNavigateToFourth,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CasinoButton(
                    emoji = "🎨",
                    text = "Цвета",
                    accent = NeonGreen,
                    onClick = onNavigateToSecond,
                    modifier = Modifier.weight(1f)
                )
                CasinoButton(
                    emoji = "🏦",
                    text = "Банк",
                    accent = NeonPurple,
                    onClick = onNavigateToEighth,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CasinoButton(
                    emoji = "👥",
                    text = "Мультиплеер",
                    accent = NeonCyan,
                    onClick = onNavigateToTenth,
                    modifier = Modifier.weight(1f)
                )
                CasinoButton(
                    emoji = "🎁",
                    text = "Фриспины",
                    accent = GoldAccent,
                    onClick = onNavigateToNineth,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CasinoButton(
                    emoji = "📢",
                    text = "Инфо & Ссылки",
                    accent = NeonCyan,
                    onClick = onNavigateToSixth,
                    modifier = Modifier.weight(1f)
                )
                CasinoButton(
                    emoji = "❤️",
                    text = "Донат",
                    accent = NeonRed,
                    onClick = onNavigateToSeventh,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // ===================== ДИАЛОГ ПРОМОКОДОВ / РАЗРАБОТЧИКА =====================
    if (showPromoDialog) {
        DeveloperPromoDialog(
            isDevMode = isCodeAccepted,
            promoInput = promoInput,
            onPromoChange = { promoInput = it },
            balanceInput = balanceInput,
            onBalanceChange = { balanceInput = it.filter { c -> c.isDigit() }.take(9) },
            spinsInput = spinsInput,
            onSpinsChange = { spinsInput = it.filter { c -> c.isDigit() }.take(7) },
            onDismiss = resetDialog,
            onConfirm = {
                if (!isCodeAccepted) {
                    val cleanInput = promoInput.trim()
                    when (cleanInput) {
                        // ---- Секретные коды ----
                        "1234" -> {
                            sharedPreferences.edit()
                                .putLong("bank_banned_until", 0L)
                                .apply()
                            isCodeAccepted = true
                        }

                        "666" -> {
                            val banTimeEnd = System.currentTimeMillis() + (10 * 60 * 1000L)
                            sharedPreferences.edit()
                                .putInt("balance", 0)
                                .putInt("free_spins", 0)
                                .putBoolean("loan_approved", false)
                                .putLong("loan_debt", 0L)
                                .putString("loan_borrower", "")
                                .putLong("loan_last_time", 0L)
                                .putLong("bank_banned_until", banTimeEnd)
                                .apply()

                            Toast.makeText(
                                context,
                                "Ты начал жизнь с чистого листа! ☠️🔥📜🔒",
                                Toast.LENGTH_LONG
                            ).show()
                            refreshCounters()
                            resetDialog()
                        }

                        // ---- Обычные промокоды из списка PROMO_CODES ----
                        else -> {
                            val promo = PROMO_CODES.firstOrNull { it.code == cleanInput }
                            if (promo == null) {
                                Toast.makeText(context, "Неверный код ❌", Toast.LENGTH_SHORT).show()
                            } else {
                                val promoKey = "promo_${promo.code}_used"
                                val isPromoUsed = sharedPreferences.getBoolean(promoKey, false)

                                if (promo.oneTime && isPromoUsed) {
                                    Toast.makeText(
                                        context,
                                        "Этот промокод уже активирован! ❌",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    val currentBalance = sharedPreferences.getInt("balance", 100)
                                    val currentSpins = sharedPreferences.getInt("free_spins", 0)

                                    sharedPreferences.edit()
                                        .putInt("balance", currentBalance + promo.bonusCash)
                                        .putInt("free_spins", currentSpins + promo.bonusSpins)
                                        .putBoolean(promoKey, true)
                                        .apply()

                                    val msg = buildString {
                                        append("Промокод активирован!")
                                        if (promo.bonusCash > 0) append(" +${promo.bonusCash} 💰")
                                        if (promo.bonusSpins > 0) append(" +${promo.bonusSpins} 🎁")
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    refreshCounters()
                                    resetDialog()
                                }
                            }
                        }
                    }
                } else {
                    // Применение изменений из режима разработчика
                    val newBalance = balanceInput.toIntOrNull() ?: sharedPreferences.getInt("balance", 100)
                    val newSpins = spinsInput.toIntOrNull() ?: sharedPreferences.getInt("free_spins", 0)

                    sharedPreferences.edit()
                        .putInt("balance", newBalance)
                        .putInt("free_spins", newSpins)
                        .apply()

                    Toast.makeText(
                        context,
                        "Баланс: $newBalance 💰 | Фриспины: $newSpins 🎁",
                        Toast.LENGTH_LONG
                    ).show()
                    refreshCounters()
                    resetDialog()
                }
            }
        )
    }
}

// ===================== ВЕРХНЯЯ ПАНЕЛЬ =====================
@Composable
private fun TopBarCasino(
    balance: Int,
    freeSpins: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFFFFA751)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Логотип слева
            Text(
                text = "🎰",
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mysor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent
            )

            Spacer(modifier = Modifier.weight(1f))

            // Баланс
            StatChip(
                emoji = "💰",
                value = balance.toString(),
                accent = GoldAccent
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Фриспины
            StatChip(
                emoji = "🎁",
                value = freeSpins.toString(),
                accent = NeonCyan
            )
        }
    }
}

@Composable
private fun StatChip(
    emoji: String,
    value: String,
    accent: Color
) {
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
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

// ===================== КАЗИНО-КНОПКА =====================
@Composable
private fun CasinoButton(
    emoji: String,
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkCardBg,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(72.dp)
            .border(2.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ===================== ДИАЛОГ ПРОМОКОДОВ / РАЗРАБОТЧИКА =====================
@Composable
private fun DeveloperPromoDialog(
    isDevMode: Boolean,
    promoInput: String,
    onPromoChange: (String) -> Unit,
    balanceInput: String,
    onBalanceChange: (String) -> Unit,
    spinsInput: String,
    onSpinsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF12101F),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (!isDevMode) "🎫" else "⚙️",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (!isDevMode) "Ввод промокода" else "Режим разработчика",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = GoldAccent
                    )
                    Text(
                        text = if (!isDevMode)
                            "Активируйте бонусный код"
                        else
                            "Управление счётчиками",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column {
                if (!isDevMode) {
                    PremiumTextField(
                        value = promoInput,
                        onValueChange = onPromoChange,
                        placeholder = "Введите промокод…",
                        keyboardType = KeyboardType.Text
                    )
                } else {
                    PremiumTextField(
                        value = balanceInput,
                        onValueChange = onBalanceChange,
                        placeholder = "Баланс",
                        keyboardType = KeyboardType.Number,
                        label = "💰 Баланс"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PremiumTextField(
                        value = spinsInput,
                        onValueChange = onSpinsChange,
                        placeholder = "Фриспины",
                        keyboardType = KeyboardType.Number,
                        label = "🎁 Фриспины"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = Color(0xFF1A1208)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (!isDevMode) "Проверить" else "Применить",
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    label: String? = null
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1730),
                unfocusedContainerColor = Color(0xFF1A1730),
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = Color(0xFF3A3F58),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = GoldAccent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}