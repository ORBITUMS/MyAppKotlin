package com.example.myfirstapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoScreen(onBackToMenu: () -> Unit) {
    // Инструмент для безопасного открытия ссылок в системе
    val uriHandler = LocalUriHandler.current

    // Переменные дизайна, совпадающие с MenuScreen
    val darkCardBg = Color(0xFF0F0C20)
    val neonBlue = Color(0xFF00F0FF)
    val neonPurple = Color(0xFFD67BFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок экрана
        Text(
            text = "ИНФОРМАЦИЯ",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // БЛОК 1: ТЕЛЕГРАМ КАНАЛ
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Наш официальный Telegram-канал. Новости проекта, раздачи промокодов и общение (вместе с кодом от банка!!).",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = { uriHandler.openUri("https://t.me/CazikAlmazik") },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .border(2.dp, neonBlue, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "📢 Перейти в Telegram",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // БЛОК 2: ДРУГОЕ ПРИЛОЖЕНИЕ НА GITHUB
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "проект на GitHub. Открытый исходный код, и просто полезное приложение.",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = { uriHandler.openUri("https://github.com/revanced/revanced-manager") }, // Замените на вашу ссылку
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .border(2.dp, neonPurple, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "📦 Посмотреть на GitHub",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Наш стандартный выход в меню
        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clickable { onBackToMenu() }
        )
    }
}
