package com.example.myfirstapp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DonateScreen(onBackToMenu: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Наш фирменный темный градиент на фоне
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Эмодзи звезды
        Text(
            text = "⭐️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Главный текст доната
        Text(
            text = "МОЖЕТЕ КИДАТЬ ПОДАРКИ\nНА ТГК ЗА ЗВЁЗДЫ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD700), // Золотое свечение в тон кнопки меню
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp,
            lineHeight = 32.sp
        )

        // Отступ перед кнопкой возврата
        Spacer(modifier = Modifier.height(32.dp))

        // Фирменный выход в меню (как на всех остальных экранах)
        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clickable { onBackToMenu() } // Просто возвращает назад без условий
        )
    }
}
