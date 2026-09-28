package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

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