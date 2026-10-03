package com.example.myfirstapp

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ===== Локальные цвета для этого экрана =====
private val DsDarkBg = Color(0xFF0F0C20)
private val DsGoldAccent = Color(0xFFFFD700)
private val DsNeonCyan = Color(0xFF00E5FF)
private val DsNeonGreen = Color(0xFF00FF7F)
private val DsNeonPurple = Color(0xFFD67BFF)

// ===== ЛОКАЛЬНАЯ МОДЕЛЬ ПАКЕТА =====
// Отдельное имя DsSpinPack, чтобы не конфликтовать с SpinPack из MultiplayerScreen.kt
private data class DsSpinPack(
    val title: String,
    val spins: Int,
    val price: Int,
    val discountPercent: Int,
    val accent: Color,
    val tag: String? = null
)

// ===== СПИСОК ПАКЕТОВ =====
// Здесь именно DsSpinPack(...), а не SPIN_PACKS(...) — это разные вещи.
private val DS_SPIN_PACKS = listOf(
    DsSpinPack("Мини", 10, 250, 0, DsNeonGreen),
    DsSpinPack("Старт", 25, 625, 0, DsNeonCyan),
    DsSpinPack("Популярный", 50, 1200, 5, DsGoldAccent, tag = "🔥 ХИТ"),
    DsSpinPack("Большой", 100, 2250, 10, DsNeonPurple, tag = "💎 ВЫГОДА"),
    DsSpinPack("Огромный", 250, 5000, 20, Color(0xFFFF4500), tag = "⚡ ТОП"),
    DsSpinPack("Мега", 500, 9000, 28, Color(0xFFE94560), tag = "👑 ЛЕГЕНДА")
)

@Composable
fun DonateScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("casino_prefs", android.content.Context.MODE_PRIVATE)
    }

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }

    var selectedPack by remember { mutableStateOf<DsSpinPack?>(null) }

    // ===== ОВЕРЛЕЙ ОБРАБОТКИ =====
    var showProcessing by remember { mutableStateOf(false) }
    var pendingSpins by remember { mutableStateOf(0) }
    var pendingPrice by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "ProcessingProgress"
    )

    LaunchedEffect(showProcessing) {
        if (showProcessing) {
            progress = 0f
            val steps = Random.nextInt(5, 9)
            val stepDelay = Random.nextLong(300L, 500L)
            for (i in 1..steps) {
                delay(stepDelay)
                progress = i.toFloat() / steps
            }
            delay(200)

            freeSpins += pendingSpins
            sharedPreferences.edit().putInt("free_spins", freeSpins).apply()

            Toast.makeText(
                context,
                "Куплено +$pendingSpins фриспинов за $pendingPrice 💰",
                Toast.LENGTH_SHORT
            ).show()

            showProcessing = false
            progress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ===== ЗАГОЛОВОК МАГАЗИНА =====
        Text(
            text = "🎁 МАГАЗИН ФРИСПИНОВ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = DsGoldAccent,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Базовые 25 монет за 1 спин.\nЧем больше пакет — тем больше скидка.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ===== СЕТКА ПАКЕТОВ 2×3 =====
        DS_SPIN_PACKS.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { pack ->
                    DsSpinPackCard(
                        pack = pack,
                        canAfford = balance >= pack.price,
                        onClick = { selectedPack = pack },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== НАЗАД В МЕНЮ =====
        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable { onBackToMenu() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ===== ДИАЛОГ ПОДТВЕРЖДЕНИЯ =====
    if (selectedPack != null) {
        val pack = selectedPack!!
        AlertDialog(
            onDismissRequest = { selectedPack = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF12101F),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎁", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Подтверждение",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DsGoldAccent
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Пакет «${pack.title}»",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Получишь: ${pack.spins} 🎁 фриспинов",
                        color = DsNeonCyan,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Стоимость: ${pack.price} 💰",
                        color = DsGoldAccent,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "У тебя сейчас: $balance 💰",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (pack.discountPercent > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔥 Скидка ${pack.discountPercent}% уже учтена",
                            color = Color(0xFFE94560),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (balance >= pack.price) {
                            balance -= pack.price
                            sharedPreferences.edit().putInt("balance", balance).apply()

                            pendingSpins = pack.spins
                            pendingPrice = pack.price

                            selectedPack = null
                            showProcessing = true
                        }
                    },
                    enabled = balance >= pack.price,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DsGoldAccent,
                        contentColor = Color(0xFF1A1208),
                        disabledContainerColor = Color(0xFF3A3F58)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (balance >= pack.price) "Купить" else "Не хватает",
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPack = null }) {
                    Text(text = "Отмена", color = Color.Gray)
                }
            }
        )
    }

    // ===== ОВЕРЛЕЙ "ОБРАБОТКА" =====
    if (showProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12101F)),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(DsGoldAccent, Color(0xFFFFA751)))
                ),
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = DsGoldAccent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DsGoldAccent
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1A1730))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(DsGoldAccent, Color(0xFFFFA751))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

// ===== Карточка пакета =====
@Composable
private fun DsSpinPackCard(
    pack: DsSpinPack,
    canAfford: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) DsDarkBg else Color(0xFF1A1730)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (pack.tag != null) 2.dp else 1.dp,
            color = if (canAfford) pack.accent.copy(alpha = 0.85f) else Color(0xFF3A3F58)
        ),
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = canAfford) { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (pack.tag != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(pack.accent.copy(alpha = 0.20f))
                        .border(1.dp, pack.accent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = pack.tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = pack.accent
                    )
                }
            }

            if (pack.discountPercent > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE94560))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "-${pack.discountPercent}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎁", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pack.spins}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = pack.accent
                )
                Text(
                    text = "фриспинов",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pack.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pack.price} 💰",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (canAfford) DsGoldAccent else Color.Gray
                )
            }
        }
    }
}