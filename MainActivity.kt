package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
data class PromoCode(
    val code: String,
    val bonusCash: Int = 0,
    val bonusSpins: Int = 0,
    val oneTime: Boolean = true
)

val PROMO_CODES: List<PromoCode> = listOf(
    PromoCode("777", bonusSpins = 10),
    PromoCode("1488", bonusCash = 100),
    PromoCode("2026", bonusCash = 100),
    PromoCode("гей", bonusSpins = 5),
    PromoCode("мусор без дропа", bonusCash = 250),
    PromoCode("додеп", bonusCash = 444),
    PromoCode("додеп2", bonusSpins = 20),
    PromoCode("гей2", bonusSpins = 20),
    PromoCode("free", bonusSpins = 10),
    PromoCode("паша", bonusSpins = 67)
)

// ===================== НОВОСТИ-КНОПКИ =====================
// Каждая новость теперь может иметь свой onClick.
// Если onClick == null — карточка просто отображается (не кликабельна).
private data class NewsItem(
    val emoji: String,
    val title: String,
    val description: String,
    val accent: Color,
    val onClick: (() -> Unit)? = null
)

// ===================== ПУНКТ МЕНЮ =====================
private data class MenuEntry(
    val emoji: String,
    val title: String,
    val accent: Color,
    val onClick: () -> Unit
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

    // ===== DRAWER =====
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ===== ПОРЯДОК ВКЛАДОК =====
    val menuEntries = listOf(
        MenuEntry("🎰", "Слоты", NeonOrange) {
            scope.launch { drawerState.close() }
            onNavigateToThird()
        },
        MenuEntry("📦", "Мусор Дроп", NeonOrange) {
            scope.launch { drawerState.close() }
            onNavigateToFourth()
        },
        MenuEntry("💳", "Пополнение", NeonGreen) {
            scope.launch { drawerState.close() }
            onNavigateToFifth()
        },
        MenuEntry("🏦", "Банк", NeonPurple) {
            scope.launch { drawerState.close() }
            onNavigateToEighth()
        },
        MenuEntry("👥", "Мультиплеер", NeonCyan) {
            scope.launch { drawerState.close() }
            onNavigateToTenth()
        },
        MenuEntry("🎁", "Фриспины", GoldAccent) {
            scope.launch { drawerState.close() }
            onNavigateToNineth()
        },
        MenuEntry("📢", "Инфо & Ссылки", NeonCyan) {
            scope.launch { drawerState.close() }
            onNavigateToSixth()
        },
        MenuEntry("❤️", "Донат", NeonRed) {
            scope.launch { drawerState.close() }
            onNavigateToSeventh()
        },
        MenuEntry("🎨", "Цвета", NeonGreen) {
            scope.launch { drawerState.close() }
            onNavigateToSecond()
        },
        MenuEntry("🎫", "Промокоды", NeonBlue) {
            scope.launch { drawerState.close() }
            showPromoDialog = true
        }
    )

    // ===== НОВОСТИ С ДЕЙСТВИЯМИ =====
    val newsItems = listOf(
        NewsItem(
            emoji = "🎰",
            title = "Новое богатое колесо",
            description = "Теперь с ×3 множителем и двумя переходами. Крути слоты и лови джекпот!",
            accent = NeonCyan,
            onClick = { onNavigateToNineth() }   // → Фриспины
        ),
        NewsItem(
            emoji = "⚔️",
            title = "Блютуз-батлы",
            description = "Сразись с другом через Bluetooth. Победитель забирает банк из ставок!",
            accent = NeonRed,
            onClick = { onNavigateToTenth() }    // → Мультиплеер
        ),
        NewsItem(
            emoji = "🎁",
            title = "Промокод FREE",
            description = "Активируй код «free» и получи +10 фриспинов совершенно бесплатно.",
            accent = GoldAccent,
            onClick = { showPromoDialog = true } // → Диалог промокодов
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                balance = balance,
                freeSpins = freeSpins,
                entries = menuEntries,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // ===================== ВЕРХНЯЯ ПАНЕЛЬ С ☰ =====================
            TopBarCasino(
                balance = balance,
                freeSpins = freeSpins,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===================== ЗАГОЛОВОК =====================
            Text(
                text = "📰 НОВОСТИ",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Нажми на карточку, чтобы перейти",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===================== КАРТОЧКИ НОВОСТЕЙ-КНОПОК =====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                newsItems.forEachIndexed { index, news ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(80L * index)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(400)) +
                                slideInVertically(
                                    animationSpec = tween(400),
                                    initialOffsetY = { h -> h / 3 }
                                )
                    ) {
                        NewsCard(news)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===================== БОЛЬШАЯ КНОПКА МЕНЮ СНИЗУ =====================
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { scope.launch { drawerState.open() } },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCardBg,
                    contentColor = GoldAccent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(GoldAccent, Color(0xFFFFA751))),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Открыть меню",
                        tint = GoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "МЕНЮ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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

// ===================== ВЕРХНЯЯ ПАНЕЛЬ С ☰ =====================
@Composable
private fun TopBarCasino(
    balance: Int,
    freeSpins: Int,
    onMenuClick: () -> Unit
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Меню",
                    tint = GoldAccent
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "🎰",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Mysor",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent
            )

            Spacer(modifier = Modifier.weight(1f))

            StatChip(
                emoji = "💰",
                value = balance.toString(),
                accent = GoldAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
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
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

// ===================== ВЫЕЗЖАЮЩЕЕ МЕНЮ (ШИРЕ) =====================
@Composable
private fun DrawerContent(
    balance: Int,
    freeSpins: Int,
    entries: List<MenuEntry>,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0F0C20),
        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        modifier = Modifier.width(340.dp)
    ) {
        // ===== Шапка =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1630), Color(0xFF0F0C20)))
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎰", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mysor",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Твоё премиум-казино",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(
                    emoji = "💰",
                    value = balance.toString(),
                    accent = GoldAccent
                )
                StatChip(
                    emoji = "🎁",
                    value = freeSpins.toString(),
                    accent = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== Пункты меню =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            entries.forEach { entry ->
                DrawerItem(entry = entry)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerItem(entry: MenuEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1730))
            .border(1.dp, entry.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { entry.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = entry.emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = entry.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(text = "▶", fontSize = 15.sp, color = entry.accent)
    }
}

// ===================== КАРТОЧКА НОВОСТИ (КЛИКАБЕЛЬНАЯ) =====================
@Composable
private fun NewsCard(news: NewsItem) {
    val clickable = news.onClick != null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            width = if (clickable) 2.dp else 1.5.dp,
            color = news.accent.copy(alpha = if (clickable) 0.85f else 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { news.onClick?.invoke() }
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(news.accent.copy(alpha = 0.15f))
                    .border(1.dp, news.accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = news.emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = news.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = news.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = news.description,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )

                if (clickable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Открыть",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = news.accent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "▶", fontSize = 12.sp, color = news.accent)
                    }
                }
            }
        }
    }
}

// ===================== КАЗИНО-КНОПКА (оставил на будущее) =====================
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
                textAlign = TextAlign.Center
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