package com.example.myfirstapp

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Модель для ленты летящих выигрышей
data class WinRecordFr(
    val idFr: String = UUID.randomUUID().toString(),
    val amountFr: Int,
    var isVisibleFr: Boolean = true
)

// Конфигурация 8 секторов для каждого колеса.
// Числа перемешаны равномерно, чтобы одинаковые призы не стояли подряд.
enum class WheelTypeFr(
    val displayNameFr: String,
    val colorFr: Color,
    val sectorsFr: List<String>,
    val sectorColorsFr: List<Color>
) {
    NORMAL(
        "ОБЫЧНОЕ КОЛЕСО",
        Color(0xFFD4AF37),
        // 4 разных значения + джекпот перемешаны: 5,10,5,40,5,10,5,💎
        listOf("5", "10", "5", "40", "5", "10", "5", "💎"),
        listOf(Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A))
    ),
    RICH(
        "БОГАТОЕ КОЛЕСО",
        Color(0xFF00FFCC),
        // 4 разных значения + смена: 10,30,10,100,10,30,10,🗑
        listOf("10", "30", "10", "100", "10", "30", "10", "🗑"),
        listOf(Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65))
    ),
    POOR(
        "БЕДНОЕ КОЛЕСО",
        Color(0xFF800020),
        // 2,5,2,15,2,5,🎁,🎰 — чередуем мелкие и крупные
        listOf("2", "5", "2", "15", "2", "5", "8 🎁", "🎰"),
        listOf(Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017))
    )
}

// Сектор смены колеса — только точное совпадение
private fun isWheelChangeSectorFr(sector: String): Boolean {
    return sector == "💎" || sector == "🗑" || sector == "🎰"
}

// Сектор бонуса фриспинов — только точное совпадение
private fun isFreeSpinBonusSectorFr(sector: String): Boolean {
    return sector == "8 🎁"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FreeSpinsScreen(
    initialWheelFr: WheelTypeFr = WheelTypeFr.NORMAL,
    onWheelChangedFr: (WheelTypeFr) -> Unit = {},
    onBackToMenuFr: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
    }

    val FrScope = rememberCoroutineScope()

    // Баланс — ровно как в FourthScreen
    var FrBalance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    // Фриспины — берутся из сохранения, по умолчанию 0
    var FrFreeSpinsLeft by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }
    var FrCurrentWheel by remember { mutableStateOf(initialWheelFr) }

    val FrWinRecords = remember { mutableStateListOf<WinRecordFr>() }

    var FrIsSpinning by remember { mutableStateOf(false) }
    var FrAutoSpinActive by remember { mutableStateOf(false) }

    val FrWheelRotationAnimatable = remember { Animatable(0f) }

    fun saveBalanceFr(newBalance: Int) {
        sharedPreferences.edit().putInt("balance", newBalance).apply()
    }

    fun saveFreeSpinsFr(newValue: Int) {
        sharedPreferences.edit().putInt("free_spins", newValue).apply()
    }

    fun runSpinLogicFr() {
        if (FrFreeSpinsLeft <= 0 || FrIsSpinning) {
            if (FrFreeSpinsLeft <= 0) {
                FrAutoSpinActive = false
            }
            return
        }

        FrIsSpinning = true
        FrFreeSpinsLeft--
        saveFreeSpinsFr(FrFreeSpinsLeft)

        FrScope.launch {
            val FrTargetRotation = FrWheelRotationAnimatable.value + 1440f + Random.nextInt(360).toFloat()

            FrWheelRotationAnimatable.animateTo(
                targetValue = FrTargetRotation,
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )

            // Сектор 0 рисуется с -90° (верх). Стрелка смотрит вверх (-90° в Canvas).
            // Угол под стрелкой с учётом начального смещения секторов:
            val FrFinalNormalizedAngle = (FrWheelRotationAnimatable.value % 360f + 360f) % 360f
            val FrSectorAngle = 360f / 8f
            val FrAngleUnderPointer = ((-FrFinalNormalizedAngle) % 360f + 360f) % 360f
            val FrTargetIndex = (FrAngleUnderPointer / FrSectorAngle).toInt().coerceIn(0, 7)

            val FrSelectedResult = FrCurrentWheel.sectorsFr[FrTargetIndex]

            when {
                isWheelChangeSectorFr(FrSelectedResult) -> {
                    val FrNewWheel = when (FrCurrentWheel) {
                        WheelTypeFr.NORMAL -> WheelTypeFr.RICH
                        WheelTypeFr.RICH -> WheelTypeFr.POOR
                        WheelTypeFr.POOR -> WheelTypeFr.NORMAL
                    }
                    FrCurrentWheel = FrNewWheel
                    onWheelChangedFr(FrNewWheel)
                }

                isFreeSpinBonusSectorFr(FrSelectedResult) -> {
                    FrFreeSpinsLeft += 8
                    saveFreeSpinsFr(FrFreeSpinsLeft)
                }

                else -> {
                    val FrCleanAmount = FrSelectedResult.trim().toIntOrNull() ?: 0
                    if (FrCleanAmount > 0) {
                        FrBalance += FrCleanAmount
                        saveBalanceFr(FrBalance)

                        val FrNewRecord = WinRecordFr(amountFr = FrCleanAmount)
                        FrWinRecords.add(FrNewRecord)
                        launch {
                            delay(1200)
                            FrNewRecord.isVisibleFr = false
                            FrWinRecords.remove(FrNewRecord)
                        }
                    }
                }
            }
            FrIsSpinning = false
        }
    }

    LaunchedEffect(FrAutoSpinActive, FrIsSpinning) {
        if (FrAutoSpinActive && !FrIsSpinning && FrFreeSpinsLeft > 0) {
            delay(500)
            runSpinLogicFr()
        } else if (FrFreeSpinsLeft <= 0) {
            FrAutoSpinActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "🎰 FREESPINS",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4AF37)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C20)),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFFE259), Color(0xFFFFA751)))
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "СПИНЫ 🎁",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$FrFreeSpinsLeft",
                        fontSize = 20.sp,
                        color = Color(0xFFE94560),
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF3A3F58))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "БАЛАНС 💰",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedContent(
                        targetState = FrBalance,
                        transitionSpec = {
                            slideInVertically { heightFr -> -heightFr } + fadeIn() togetherWith
                                    slideOutVertically { heightFr -> heightFr } + fadeOut()
                        },
                        label = "BalanceAnim"
                    ) { animatedBalanceFr ->
                        Text(
                            text = "$animatedBalanceFr",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = FrCurrentWheel,
                transitionSpec = {
                    slideInHorizontally { widthFr -> widthFr } + fadeIn() togetherWith
                            slideOutHorizontally { widthFr -> -widthFr } + fadeOut()
                },
                label = "WheelShiftAnim"
            ) { targetWheelFr ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 35.dp)
                ) {
                    GraphicWheelCanvasFr(
                        wheelTypeFr = targetWheelFr,
                        rotationAngleFr = FrWheelRotationAnimatable.value,
                        sizeFr = 320
                    )
                }
            }
        }

        // ЛЕНТА ЛЕТЯЩИХ ВВЕРХ ВЫИГРЫШЕЙ
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                FrWinRecords.forEach { recordFr ->
                    key(recordFr.idFr) {
                        AnimatedVisibility(
                            visible = recordFr.isVisibleFr,
                            enter = slideInVertically { heightFr -> heightFr } + fadeIn(
                                animationSpec = tween(300)
                            ),
                            exit = slideOutVertically { heightFr -> -heightFr } + fadeOut(
                                animationSpec = tween(500)
                            )
                        ) {
                            Text(
                                text = "+${recordFr.amountFr} 💰",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }

        // НАЗВАНИЕ КОЛЕСА — прямо над кнопкой КРУТИТЬ
        Text(
            text = FrCurrentWheel.displayNameFr,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = FrCurrentWheel.colorFr,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // КНОПКА КРУТИТЬ
        Button(
            onClick = { runSpinLogicFr() },
            enabled = FrFreeSpinsLeft > 0 && !FrIsSpinning && !FrAutoSpinActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    2.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA751))),
                    RoundedCornerShape(30.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F0C20),
                disabledContainerColor = Color(0xFF0F0C20)
            ),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                text = if (FrFreeSpinsLeft > 0) "КРУТИТЬ 🚀" else "СПИНОВ НЕТ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (FrFreeSpinsLeft > 0 && !FrIsSpinning) Color(0xFFFFD700) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // КНОПКА АВТОПРОКРУТА — одинаковый фон с КРУТИТЬ, можно остановить во время вращения
        Button(
            onClick = { FrAutoSpinActive = !FrAutoSpinActive },
            enabled = FrFreeSpinsLeft > 0 || FrAutoSpinActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(
                    1.5.dp,
                    if (FrAutoSpinActive) Color(0xFF00FFCC) else Color(0xFF3A3F58),
                    RoundedCornerShape(30.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F0C20),
                disabledContainerColor = Color(0xFF0F0C20)
            ),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                text = if (FrAutoSpinActive) "СТОП АВТО 🛑" else "АВТОПРОКРУТ 🔄",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    FrAutoSpinActive -> Color(0xFF00FFCC)
                    FrFreeSpinsLeft <= 0 -> Color.Gray
                    else -> Color.White
                }
            )
        }

        Text(
            text = "Назад в меню",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 20.dp, bottom = 16.dp)
                .clickable {
                    FrAutoSpinActive = false
                    onBackToMenuFr()
                }
        )
    }
}

@Composable
fun GraphicWheelCanvasFr(wheelTypeFr: WheelTypeFr, rotationAngleFr: Float, sizeFr: Int) {
    val FrSlicesCount = 8
    val FrSweepAngle = 360f / FrSlicesCount

    Box(
        modifier = Modifier.size(sizeFr.dp),
        contentAlignment = Alignment.Center
    ) {
        // Крутящееся колесо
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationAngleFr),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val FrCanvasSize = size.minDimension
                val FrRadius = FrCanvasSize / 2f
                val FrCenter = Offset(x = size.width / 2f, y = size.height / 2f)

                for (i in 0 until FrSlicesCount) {
                    val FrStartAngle = i * FrSweepAngle - 90f

                    drawArc(
                        color = wheelTypeFr.sectorColorsFr[i % wheelTypeFr.sectorColorsFr.size],
                        startAngle = FrStartAngle,
                        sweepAngle = FrSweepAngle,
                        useCenter = true,
                        size = Size(FrCanvasSize, FrCanvasSize),
                        topLeft = Offset((size.width - FrCanvasSize) / 2f, (size.height - FrCanvasSize) / 2f)
                    )

                    drawArc(
                        color = wheelTypeFr.colorFr.copy(alpha = 0.4f),
                        startAngle = FrStartAngle,
                        sweepAngle = FrSweepAngle,
                        useCenter = true,
                        size = Size(FrCanvasSize, FrCanvasSize),
                        topLeft = Offset((size.width - FrCanvasSize) / 2f, (size.height - FrCanvasSize) / 2f),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        val FrMedianAngleRad = ((FrStartAngle + FrSweepAngle / 2f) * PI / 180f).toFloat()
                        val FrTextDistance = FrRadius * 0.65f
                        val FrX = FrCenter.x + FrTextDistance * cos(FrMedianAngleRad)
                        val FrY = FrCenter.y + FrTextDistance * sin(FrMedianAngleRad)

                        translate(FrX, FrY)
                        rotate(FrStartAngle + FrSweepAngle / 2f + 90f)

                        val FrPaint = android.graphics.Paint().apply {
                            color = if (isWheelChangeSectorFr(wheelTypeFr.sectorsFr[i]) ||
                                isFreeSpinBonusSectorFr(wheelTypeFr.sectorsFr[i])
                            ) {
                                android.graphics.Color.YELLOW
                            } else {
                                android.graphics.Color.WHITE
                            }
                            textSize = (sizeFr * 0.075f).dp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        drawText(wheelTypeFr.sectorsFr[i], 0f, 0f, FrPaint)
                        restore()
                    }
                }

                drawCircle(
                    color = wheelTypeFr.colorFr,
                    radius = FrRadius,
                    center = FrCenter,
                    style = Stroke(width = 5.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFF0F0C20),
                    radius = FrRadius * 0.18f,
                    center = FrCenter
                )
                drawCircle(
                    color = wheelTypeFr.colorFr,
                    radius = FrRadius * 0.18f,
                    center = FrCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Text(
            text = "🔻",
            fontSize = 36.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-2).dp)
        )
    }
}