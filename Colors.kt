package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class GameColor(val name: String, val color: Color)

val gameColors = listOf(
    GameColor("Красный", Color(0xFFFF0000)),
    GameColor("Голубой", Color(0xFF00D2FF)),
    GameColor("Жёлтый", Color(0xFFFFD700)),
    GameColor("Зелёный", Color(0xFF00FF00)),
    GameColor("Пурпурный", Color(0xFFFF00FF)),
    GameColor("Синий", Color(0xFF0000FF)),
    GameColor("Чёрный", Color(0xFF1A1A1A)),
    GameColor("Фиолетовый", Color(0xFF4B0082)),
    GameColor("Розовый", Color(0xFFFF69B4))
)

val richLightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFDF9), Color(0xFFF9EED8))
)

// ===== Общие цвета для верхней панели =====
private val SecDarkCardBg = Color(0xFF0F0C20)
private val SecGoldAccent = Color(0xFFFFD700)
private val SecNeonCyan = Color(0xFF00E5FF)

@Composable
fun SecondScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current

    // Префы для рекорда
    val sharedPreferences =
        remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    // Префы казино для баланса/фриспинов
    val casinoPreferences =
        remember { context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE) }

    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(sharedPreferences.getInt("high_score", 0)) }

    // Глобальные счётчики
    var balance by remember { mutableStateOf(casinoPreferences.getInt("balance", 100)) }
    val freeSpins = remember { casinoPreferences.getInt("free_spins", 0) }

    var bgIndex by remember { mutableStateOf(0) }
    var textIndex by remember { mutableStateOf(1) }

    val coffeeSquareColor = Color(0xFF4A3B32)
    val darkCoffeeButtonColor = Color(0xFF261C14)

    fun saveBalance(newBalance: Int) {
        casinoPreferences.edit().putInt("balance", newBalance).apply()
    }

    val nextRound = {
        val newBg = Random.nextInt(gameColors.size)
        var newText = Random.nextInt(gameColors.size)
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
            // ПУНКТ 2: за каждое правильное нажатие +1 монета в общий баланс
            balance += 1
            saveBalance(balance)
        } else {
            score = 0
        }
        nextRound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(richLightGradient)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
        TopBarCasinoSecond(
            balance = balance,
            freeSpins = freeSpins
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ===== СЧЁТ ИГРЫ =====
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Счёт: ",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D734B)
            )
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

        // ===== КРУГ =====
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(coffeeSquareColor, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(gameColors[bgIndex].color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gameColors[textIndex].name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ===== СЕТКА КНОПОК =====
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

        Button(
            onClick = onBackToMenu,
            colors = ButtonDefaults.buttonColors(containerColor = darkCoffeeButtonColor),
            modifier = Modifier.width(260.dp)
        ) {
            Text(text = "Выйти на главный экран", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SmallColorButton(gameColor: GameColor, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .size(55.dp)
            .clip(CircleShape)
            .background(gameColor.color)
            .clickable { onClick() }
    )
}

// ===== Верхняя панель для SecondScreen =====
@Composable
private fun TopBarCasinoSecond(
    balance: Int,
    freeSpins: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecDarkCardBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(SecGoldAccent, Color(0xFFFFA751)))
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
                color = SecGoldAccent
            )
            Spacer(modifier = Modifier.weight(1f))

            StatChipSecond(emoji = "💰", value = balance, accent = SecGoldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            StatChipSecond(emoji = "🎁", value = freeSpins, accent = SecNeonCyan)
        }
    }
}

@Composable
private fun StatChipSecond(emoji: String, value: Int, accent: Color) {
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
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                slideInVertically { h -> -h } + androidx.compose.animation.fadeIn() togetherWith
                        slideOutVertically { h -> h } + androidx.compose.animation.fadeOut()
            },
            label = "StatChipSecondAnim"
        ) { animatedValue ->
            Text(
                text = animatedValue.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

class WinRecordC(val id: Long, val amount: Int, isVisibleState: MutableState<Boolean>) {
    var isVisible by isVisibleState
}