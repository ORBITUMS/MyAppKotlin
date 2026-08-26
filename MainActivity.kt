package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

val richLightGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFDF9),
        Color(0xFFF9EED8)
    )
)

data class GameColor(
    val name: String,
    val color: Color
)

val gameColors = listOf(
    GameColor("Красный", Color(0xFFFF6B6B)),
    GameColor("Голубой", Color(0xFF4DADFF)),
    GameColor("Жёлтый", Color(0xFFFFD93D)),
    GameColor("Зелёный", Color(0xFF6BCB77)),
    GameColor("Пурпурный", Color(0xFFD67BFF)),
    GameColor("Синий", Color(0xFF4B65F6))
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(onNavigateToSecond = { navController.navigate("second") })
        }
        composable("second") {
            SecondScreen(onBackToMenu = { navController.popBackStack() })
        }
    }
}

@Composable
fun MenuScreen(onNavigateToSecond: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(richLightGradient),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Главное меню",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A3E25)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onNavigateToSecond() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D734B))
        ) {
            Text(text = "Перейти к игре", fontSize = 18.sp, color = Color.White)
        }
    }
}

// ОБНОВЛЕННЫЙ ВТОРОЙ ЭКРАН С ИГРОЙ СТРОГО ПО ЦЕНТРУ
@Composable
fun SecondScreen(onBackToMenu: () -> Unit) {
    var score by remember { mutableStateOf(0) }
    var bgIndex by remember { mutableStateOf(0) }
    var textIndex by remember { mutableStateOf(1) }

    val nextRound = {
        val newBg = Random.nextInt(6)
        var newText = Random.nextInt(6)
        while (newText == newBg) {
            newText = Random.nextInt(6)
        }
        bgIndex = newBg
        textIndex = newText
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(richLightGradient)
    ) {
        // 🎯 Весь контент игры теперь выровнен строго по центру экрана (Alignment.Center)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Счётчик очков
            Text(
                text = "Счёт: $score",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D734B)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Большой центральный круг-обманщик
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

            Spacer(modifier = Modifier.height(48.dp))

            // 🎛️ 6 маленьких кружков для выбора ответа
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    for (i in 0..2) {
                        SmallColorButton(gameColor = gameColors[i], onClick = {
                            if (i == textIndex) score++ else if (score > 0) score = 0
                            nextRound()
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    for (i in 3..5) {
                        SmallColorButton(gameColor = gameColors[i], onClick = {
                            if (i == textIndex) score++ else if (score > 0) score = 0
                            nextRound()
                        })
                    }
                }
            }
        }

        // Кнопка выхода остается в самом низу
        Button(
            onClick = { onBackToMenu() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB13B3B)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(text = "Выйти на главный экран", fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun SmallColorButton(gameColor: GameColor, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .size(55.dp)
            .background(gameColor.color, shape = CircleShape)
            .clickable { onClick() }
    )
}
