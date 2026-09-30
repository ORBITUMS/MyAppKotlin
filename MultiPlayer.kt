package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

// ===== Общие цвета (как в остальных экранах) =====
 val MpDarkBg = Color(0xFF0F0C20)
 val MpGoldAccent = Color(0xFFFFD700)
 val MpNeonCyan = Color(0xFF00E5FF)
 val MpNeonGreen = Color(0xFF00FF7F)
 val MpNeonPurple = Color(0xFFD67BFF)

// ===== Скидочные пакеты фриспинов =====
// spins — сколько даём, price — итоговая цена со скидкой, discountPercent — сколько процентов скидка (для отображения)
private data class SpinPack(
    val title: String,
    val spins: Int,
    val price: Int,
    val discountPercent: Int,
    val accent: Color,
    val tag: String? = null
)

private val SPIN_PACKS = listOf(
    SpinPack("Мини", 10, 250, 0, MpNeonGreen),
    SpinPack("Старт", 25, 625, 0, MpNeonCyan),
    SpinPack("Популярный", 50, 1200, 5, MpGoldAccent, tag = "🔥 ХИТ"),
    SpinPack("Большой", 100, 2250, 10, MpNeonPurple, tag = "💎 ВЫГОДА"),
    SpinPack("Огромный", 250, 5000, 20, Color(0xFFFF4500), tag = "⚡ ТОП"),
    SpinPack("Мега", 500, 9000, 28, Color(0xFFE94560), tag = "👑 ЛЕГЕНДА")
)

@Composable
fun MultiplayerScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
    }

    // Экран: 0 = главный, 1 = магазин фриспинов, 2 = мультиплеер
    var screen by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
    ) {
        when (screen) {
            0 -> MpMainScreen(
                sharedPreferences = sharedPreferences,
                onBuySpins = { screen = 1 },
                onBattle = { screen = 2 },
                onBackToMenu = onBackToMenu
            )
            1 -> BuySpinsScreen(
                sharedPreferences = sharedPreferences,
                onBack = { screen = 0 }
            )
            2 -> BattleScreen(
                sharedPreferences = sharedPreferences,
                onBack = { screen = 0 },
                onGoToShop = { screen = 1 }
            )
        }
    }
}

// ===================== ГЛАВНЫЙ ЭКРАН =====================
@Composable
private fun MpMainScreen(
    sharedPreferences: android.content.SharedPreferences,
    onBuySpins: () -> Unit,
    onBattle: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val balance = sharedPreferences.getInt("balance", 100)
    val freeSpins = sharedPreferences.getInt("free_spins", 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Верхнее меню как на других экранах
        TopBarCasinoMultiplayer(
            balance = balance,
            freeSpins = freeSpins
        )

        // Заголовок по центру
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 МУЛЬТИПЛЕЕР",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = MpGoldAccent,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Играй против друзей через Bluetooth",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Кнопка БАТЛ
            MpBigButton(
                emoji = "⚔️",
                title = "БАТЛ",
                subtitle = "Сразись с другом",
                accent = MpNeonCyan,
                onClick = onBattle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка КУПИТЬ ФРИСПИНЫ
            MpBigButton(
                emoji = "🎁",
                title = "КУПИТЬ ФРИСПИНЫ",
                subtitle = "Пополни запас для батла",
                accent = MpGoldAccent,
                onClick = onBuySpins
            )
        }

        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable { onBackToMenu() }
        )
    }
}

@Composable
private fun MpBigButton(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 42.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Text(text = "▶", fontSize = 22.sp, color = accent)
        }
    }
}

// ===================== ЭКРАН ПОКУПКИ ФРИСПИНОВ =====================
@Composable
private fun BuySpinsScreen(
    sharedPreferences: android.content.SharedPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }
    var selectedPack by remember { mutableStateOf<SpinPack?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        TopBarCasinoMultiplayer(
            balance = balance,
            freeSpins = freeSpins
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🎁 МАГАЗИН ФРИСПИНОВ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MpGoldAccent,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Базовые 25 монет за 1 спин.\nЧем больше пакет — тем больше скидка.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Сетка 2x3
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Разбиваем список на пары и рисуем рядами
            SPIN_PACKS.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { pack ->
                        SpinPackCard(
                            pack = pack,
                            canAfford = balance >= pack.price,
                            onClick = { selectedPack = pack },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Если в ряду 1 элемент — добавь пустышку для выравнивания
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Text(
            text = "Назад",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .clickable { onBack() }
        )
    }

    // Диалог подтверждения покупки
    if (selectedPack != null) {
        PurchaseDialog(
            pack = selectedPack!!,
            balance = balance,
            onDismiss = { selectedPack = null },
            onConfirm = {
                val pack = selectedPack!!
                if (balance >= pack.price) {
                    balance -= pack.price
                    freeSpins += pack.spins

                    sharedPreferences.edit()
                        .putInt("balance", balance)
                        .putInt("free_spins", freeSpins)
                        .apply()

                    android.widget.Toast.makeText(
                        context,
                        "Куплено +${pack.spins} фриспинов за ${pack.price} 💰",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                selectedPack = null
            }
        )
    }
}

@Composable
private fun SpinPackCard(
    pack: SpinPack,
    canAfford: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) MpDarkBg else Color(0xFF1A1730)
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

            // Тег скидки в правом верхнем углу
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

            // Скидка в левом верхнем углу (если есть)
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
                    color = if (canAfford) MpGoldAccent else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun PurchaseDialog(
    pack: SpinPack,
    balance: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
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
                    color = MpGoldAccent
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
                    color = MpNeonCyan,
                    fontSize = 14.sp
                )
                Text(
                    text = "Стоимость: ${pack.price} 💰",
                    color = MpGoldAccent,
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
            androidx.compose.material3.Button(
                onClick = onConfirm,
                enabled = balance >= pack.price,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MpGoldAccent,
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
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = Color.Gray)
            }
        }
    )
}

// ===================== ВЕРХНЕЕ МЕНЮ =====================
@Composable
fun TopBarCasinoMultiplayer(
    balance: Int,
    freeSpins: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(MpGoldAccent, Color(0xFFFFA751)))
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🎰", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mysor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MpGoldAccent
            )
            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.animation.AnimatedContent(
                targetState = balance,
                transitionSpec = {
                    androidx.compose.animation.slideInVertically { h -> -h } +
                            androidx.compose.animation.fadeIn() togetherWith
                            androidx.compose.animation.slideOutVertically { h -> h } +
                            androidx.compose.animation.fadeOut()
                },
                label = "BalanceAnim"
            ) { animatedBalance ->
                MpStatChip(emoji = "💰", value = animatedBalance.toString(), accent = MpGoldAccent)
            }

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.animation.AnimatedContent(
                targetState = freeSpins,
                transitionSpec = {
                    androidx.compose.animation.slideInVertically { h -> -h } +
                            androidx.compose.animation.fadeIn() togetherWith
                            androidx.compose.animation.slideOutVertically { h -> h } +
                            androidx.compose.animation.fadeOut()
                },
                label = "SpinsAnim"
            ) { animatedSpins ->
                MpStatChip(emoji = "🎁", value = animatedSpins.toString(), accent = MpNeonCyan)
            }
        }
    }
}

@Composable
fun MpStatChip(emoji: String, value: String, accent: Color) {
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