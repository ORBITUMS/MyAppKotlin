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
import androidx.compose.ui.graphics.graphicsLayer
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

// ===== Модель для ленты выигрышей =====
data class WinRecordFr(
    val idFr: String = UUID.randomUUID().toString(),
    val amountFr: Int,
    val isSpinsBonus: Boolean = false,
    val isBombMessage: Boolean = false,
    val isDebtCleared: Boolean = false,
    val isLockMessage: Boolean = false,
    val isMultiplierMessage: Boolean = false
)

// ===== КОЛЁСА =====
// NORMAL — низкие + ×2 + 🔒 + 💣 + переход на RICH (💎)
// RICH  — высокие + ×3 + переход на POOR (🗑) и на NORMAL (🎰)
// POOR  — очень низкие + 💣 + 🎁 + 💰 + ×2 + переход на NORMAL (🎰)
enum class WheelTypeFr(
    val displayNameFr: String,
    val colorFr: Color,
    val sectorsFr: List<String>,
    val sectorColorsFr: List<Color>
) {
    NORMAL(
        "ОБЫЧНОЕ КОЛЕСО",
        Color(0xFFD4AF37),
        listOf("5", "10", "5", "7", "×2", "10", "💣", "5", "5", "10", "🔒", "💎"),
        listOf(
            Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A),
            Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A),
            Color(0xFF2A1B54), Color(0xFF3D2C7A), Color(0xFF2A1B54), Color(0xFF3D2C7A)
        )
    ),
    RICH(
        "БОГАТОЕ КОЛЕСО",
        Color(0xFF00FFCC),
        // Было два 🗑 и 💎. Теперь: один 🗑, один 🎰 (переход в обычное).
        listOf("🗑", "30", "7", "×3", "100", "30", "7", "50", "🎰", "7", "100", "30"),
        listOf(
            Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65),
            Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65),
            Color(0xFF0D5245), Color(0xFF117A65), Color(0xFF0D5245), Color(0xFF117A65)
        )
    ),
    POOR(
        "БЕДНОЕ КОЛЕСО",
        Color(0xFF7B4A21),
        listOf("💣", "🔒", "2", "×2", "5", "8 🎁", "💣", "7", "5", "15", "💰", "🎰"),
        listOf(
            Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017),
            Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017),
            Color(0xFF4A0010), Color(0xFF660017), Color(0xFF4A0010), Color(0xFF660017)
        )
    )
}

// ===== Проверки секторов =====
private fun isNormalChangeSectorFr(s: String): Boolean = s == "💎"
private fun isRichToPoorSectorFr(s: String): Boolean = s == "🗑"
private fun isRichToNormalSectorFr(s: String): Boolean = s == "🎰"
private fun isPoorChangeSectorFr(s: String): Boolean = s == "🎰"
private fun isFreeSpinBonusSectorFr(s: String): Boolean = s == "8 🎁"
private fun isBombSectorFr(s: String): Boolean = s == "💣"
private fun isLoanPayoffSectorFr(s: String): Boolean = s == "💰"
private fun isSevenFr(s: String): Boolean = s == "7"
private fun isLockSectorFr(s: String): Boolean = s == "🔒"
private fun isMultiplier2Fr(s: String): Boolean = s == "×2"
private fun isMultiplier3Fr(s: String): Boolean = s == "×3"

private const val PREF_KEY_WHEEL = "current_wheel"
private const val MAX_MULTIPLIER = 54

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

    var FrBalance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var FrFreeSpinsLeft by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }

    var FrCurrentWheel by remember {
        mutableStateOf(
            try {
                val savedName = sharedPreferences.getString(PREF_KEY_WHEEL, null)
                if (savedName != null) WheelTypeFr.valueOf(savedName) else initialWheelFr
            } catch (e: Exception) {
                initialWheelFr
            }
        )
    }

    var FrSevenProgress by remember {
        mutableStateOf(sharedPreferences.getInt("seven_progress", 0))
    }
    var FrLockActive by remember {
        mutableStateOf(sharedPreferences.getBoolean("lock_active", false))
    }
    var FrNextMultiplier by remember {
        mutableStateOf(sharedPreferences.getInt("next_multiplier", 1))
    }

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
    fun saveWheelFr(wheel: WheelTypeFr) {
        sharedPreferences.edit().putString(PREF_KEY_WHEEL, wheel.name).apply()
    }
    fun saveSevenProgress(value: Int) {
        sharedPreferences.edit().putInt("seven_progress", value).apply()
    }
    fun saveLockActive(value: Boolean) {
        sharedPreferences.edit().putBoolean("lock_active", value).apply()
    }
    fun saveNextMultiplier(value: Int) {
        sharedPreferences.edit().putInt("next_multiplier", value).apply()
    }

    fun pushRecordFr(
        amount: Int,
        isSpins: Boolean = false,
        isBomb: Boolean = false,
        isDebt: Boolean = false,
        isLock: Boolean = false,
        isMult: Boolean = false
    ) {
        val r = WinRecordFr(
            amountFr = amount,
            isSpinsBonus = isSpins,
            isBombMessage = isBomb,
            isDebtCleared = isDebt,
            isLockMessage = isLock,
            isMultiplierMessage = isMult
        )
        FrWinRecords.add(r)
        FrScope.launch {
            delay(3600)
            FrWinRecords.remove(r)
        }
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
            val FrTargetRotation = FrWheelRotationAnimatable.value + 1800f + Random.nextInt(360).toFloat()

            FrWheelRotationAnimatable.animateTo(
                targetValue = FrTargetRotation,
                animationSpec = tween(
                    durationMillis = 5000,
                    easing = CubicBezierEasing(0.10f, 0.0f, 0.05f, 1.0f)
                )
            )

            val FrFinalNormalizedAngle = (FrWheelRotationAnimatable.value % 360f + 360f) % 360f
            val FrSectorAngle = 360f / 12f
            val FrAngleUnderPointer = ((-FrFinalNormalizedAngle) % 360f + 360f) % 360f
            val FrTargetIndex = (FrAngleUnderPointer / FrSectorAngle).toInt().coerceIn(0, 11)

            val FrSelectedResult = FrCurrentWheel.sectorsFr[FrTargetIndex]

            // ===== ПРОВЕРКА ЗАМКА =====
            if (FrLockActive) {
                FrLockActive = false
                saveLockActive(false)
                pushRecordFr(0, isLock = true)
                FrIsSpinning = false
                return@launch
            }

            // ===== ПРОГРЕСС 7 =====
            if (isSevenFr(FrSelectedResult)) {
                FrSevenProgress += 1
                if (FrSevenProgress > 3) FrSevenProgress = 3
                saveSevenProgress(FrSevenProgress)

                if (FrSevenProgress >= 3) {
                    val jackpot = 1000
                    FrBalance += jackpot
                    saveBalanceFr(FrBalance)
                    pushRecordFr(jackpot)
                    FrSevenProgress = 0
                    saveSevenProgress(0)
                }
            } else {
                if (FrSevenProgress > 0) {
                    FrSevenProgress = 0
                    saveSevenProgress(0)
                }
            }

            when {
                // ===== БОМБА =====
                isBombSectorFr(FrSelectedResult) -> {
                    val lost = 5
                    FrFreeSpinsLeft = (FrFreeSpinsLeft - lost).coerceAtLeast(0)
                    saveFreeSpinsFr(FrFreeSpinsLeft)
                    pushRecordFr(lost, isBomb = true)
                }

                // ===== ЗАМОК =====
                isLockSectorFr(FrSelectedResult) -> {
                    FrLockActive = true
                    saveLockActive(true)
                    pushRecordFr(0, isLock = true)
                }

                // ===== МНОЖИТЕЛИ =====
                isMultiplier2Fr(FrSelectedResult) -> {
                    FrNextMultiplier = (FrNextMultiplier.coerceAtLeast(1) * 2)
                        .coerceAtMost(MAX_MULTIPLIER)
                    saveNextMultiplier(FrNextMultiplier)
                    pushRecordFr(2, isMult = true)
                }
                isMultiplier3Fr(FrSelectedResult) -> {
                    FrNextMultiplier = (FrNextMultiplier.coerceAtLeast(1) * 3)
                        .coerceAtMost(MAX_MULTIPLIER)
                    saveNextMultiplier(FrNextMultiplier)
                    pushRecordFr(3, isMult = true)
                }

                // ===== ПОГАШЕНИЕ КРЕДИТА =====
                isLoanPayoffSectorFr(FrSelectedResult) -> {
                    val hadDebt = sharedPreferences.getLong("loan_debt", 0L) > 0
                    if (hadDebt) {
                        sharedPreferences.edit()
                            .putBoolean("loan_approved", false)
                            .putLong("loan_debt", 0L)
                            .putString("loan_borrower", "")
                            .putLong("loan_last_time", 0L)
                            .apply()
                        pushRecordFr(0, isDebt = true)
                    }
                }

                // ===== +8 СПИНОВ =====
                isFreeSpinBonusSectorFr(FrSelectedResult) -> {
                    FrFreeSpinsLeft += 8
                    saveFreeSpinsFr(FrFreeSpinsLeft)
                    pushRecordFr(8, isSpins = true)
                }

                // ===== ПЕРЕХОДЫ =====
                // NORMAL -> RICH
                FrCurrentWheel == WheelTypeFr.NORMAL && isNormalChangeSectorFr(FrSelectedResult) -> {
                    FrCurrentWheel = WheelTypeFr.RICH
                    saveWheelFr(WheelTypeFr.RICH)
                    onWheelChangedFr(WheelTypeFr.RICH)
                }
                // RICH -> POOR
                FrCurrentWheel == WheelTypeFr.RICH && isRichToPoorSectorFr(FrSelectedResult) -> {
                    FrCurrentWheel = WheelTypeFr.POOR
                    saveWheelFr(WheelTypeFr.POOR)
                    onWheelChangedFr(WheelTypeFr.POOR)
                }
                // RICH -> NORMAL
                FrCurrentWheel == WheelTypeFr.RICH && isRichToNormalSectorFr(FrSelectedResult) -> {
                    FrCurrentWheel = WheelTypeFr.NORMAL
                    saveWheelFr(WheelTypeFr.NORMAL)
                    onWheelChangedFr(WheelTypeFr.NORMAL)
                }
                // POOR -> NORMAL
                FrCurrentWheel == WheelTypeFr.POOR && isPoorChangeSectorFr(FrSelectedResult) -> {
                    FrCurrentWheel = WheelTypeFr.NORMAL
                    saveWheelFr(WheelTypeFr.NORMAL)
                    onWheelChangedFr(WheelTypeFr.NORMAL)
                }

                // ===== ОБЫЧНЫЙ ВЫИГРЫШ =====
                else -> {
                    val baseAmount = FrSelectedResult.trim().toIntOrNull() ?: 0
                    if (baseAmount > 0) {
                        val multiplier = FrNextMultiplier.coerceAtLeast(1)
                        val totalWin = baseAmount * multiplier

                        FrBalance += totalWin
                        saveBalanceFr(FrBalance)
                        pushRecordFr(totalWin)

                        if (multiplier > 1) {
                            FrNextMultiplier = 1
                            saveNextMultiplier(1)
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

    val bgTop = remember(FrSevenProgress) {
        when (FrSevenProgress) {
            0 -> Color(0xFF111827)
            1 -> Color(0xFF1A1630)
            2 -> Color(0xFF251B45)
            else -> Color(0xFF311E5C)
        }
    }
    val bgBottom = remember(FrSevenProgress) {
        when (FrSevenProgress) {
            0 -> Color(0xFF1F2937)
            1 -> Color(0xFF2A2340)
            2 -> Color(0xFF382A5A)
            else -> Color(0xFF4A3480)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("СПИНЫ 🎁", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$FrFreeSpinsLeft", fontSize = 18.sp, color = Color(0xFFE94560), fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF3A3F58)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("БАЛАНС 💰", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    AnimatedContent(
                        targetState = FrBalance,
                        transitionSpec = {
                            slideInVertically { h -> -h } + fadeIn() togetherWith
                                    slideOutVertically { h -> h } + fadeOut()
                        },
                        label = "BalanceAnim"
                    ) { a ->
                        Text("$a", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                if (FrNextMultiplier > 1) {
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF3A3F58)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("МНОЖ.", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("×${FrNextMultiplier}", fontSize = 18.sp, color = Color(0xFF00FFCC), fontWeight = FontWeight.Black)
                    }
                }
                if (FrLockActive) {
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF3A3F58)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ЗАМОК", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("🔒", fontSize = 18.sp)
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
                    slideInHorizontally { w -> w } + fadeIn() togetherWith
                            slideOutHorizontally { w -> -w } + fadeOut()
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
                        sizeFr = 320,
                        lockActive = FrLockActive
                    )
                }
            }
        }

        Box(
            modifier = Modifier.height(100.dp).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                FrWinRecords.forEach { recordFr ->
                    key(recordFr.idFr) {
                        WinFloatingTextFr(record = recordFr)
                    }
                }
            }
        }

        Text(
            text = FrCurrentWheel.displayNameFr,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = FrCurrentWheel.colorFr,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SevenSlotsRowFr(progress = FrSevenProgress)

        Spacer(modifier = Modifier.height(12.dp))

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

// ===== Компонент 3 слотов прогресса =====
@Composable
private fun SevenSlotsRowFr(progress: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val filled = i < progress
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(44.dp)
                    .border(
                        width = 2.dp,
                        color = if (filled) Color(0xFFFFD700) else Color(0xFF3A3F58),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        if (filled) Color(0xFF3A2D0A) else Color(0xFF0F0C20),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "7️⃣",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (filled) 1f else 0.25f
                    }
                )
            }
        }
    }
}

// ===== Летящий выигрыш =====
@Composable
fun WinFloatingTextFr(record: WinRecordFr) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(record.idFr) {
        alphaAnim.animateTo(1f, tween(durationMillis = 500, easing = LinearEasing))
        delay(1800)
        alphaAnim.animateTo(0f, tween(durationMillis = 1200, easing = LinearEasing))
    }

    val (text, color) = when {
        record.isDebtCleared -> "💰 КРЕДИТ ПОГАШЕН" to Color(0xFF00FF7F)
        record.isLockMessage -> "🔒 СЛЕДУЮЩИЙ СПИН ОТМЕНЁН" to Color(0xFFD67BFF)
        record.isMultiplierMessage -> "×${record.amountFr} НА СЛЕДУЮЩИЙ ВЫИГРЫШ" to Color(0xFF00FFCC)
        record.isBombMessage -> "💣 -${record.amountFr} 🎁" to Color(0xFFFF3366)
        record.isSpinsBonus -> "+${record.amountFr} 🎁" to Color(0xFF6FE7FF)
        else -> "+${record.amountFr} 💰" to Color(0xFFFFD700)
    }

    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = color,
        modifier = Modifier.graphicsLayer { this.alpha = alphaAnim.value }
    )
}

// ===== Отрисовка колеса =====
@Composable
fun GraphicWheelCanvasFr(
    wheelTypeFr: WheelTypeFr,
    rotationAngleFr: Float,
    sizeFr: Int,
    lockActive: Boolean = false
) {
    val FrSlicesCount = 12
    val FrSweepAngle = 360f / FrSlicesCount

    Box(
        modifier = Modifier.size(sizeFr.dp),
        contentAlignment = Alignment.Center
    ) {
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
                        val FrTextDistance = FrRadius * 0.68f
                        val FrX = FrCenter.x + FrTextDistance * cos(FrMedianAngleRad)
                        val FrY = FrCenter.y + FrTextDistance * sin(FrMedianAngleRad)

                        translate(FrX, FrY)
                        rotate(FrStartAngle + FrSweepAngle / 2f + 90f)

                        val sector = wheelTypeFr.sectorsFr[i]
                        val sectorColor = when {
                            sector == "7" -> android.graphics.Color.rgb(255, 215, 0)
                            sector == "💎" -> android.graphics.Color.rgb(110, 231, 255)
                            sector == "🗑" -> android.graphics.Color.rgb(255, 100, 100)
                            sector == "🎰" -> android.graphics.Color.rgb(0, 255, 204)
                            sector == "8 🎁" -> android.graphics.Color.rgb(110, 231, 255)
                            sector == "💰" -> android.graphics.Color.rgb(0, 255, 127)
                            sector == "💣" -> android.graphics.Color.rgb(255, 51, 102)
                            sector == "🔒" -> android.graphics.Color.rgb(214, 123, 255)
                            sector == "×2" -> android.graphics.Color.rgb(0, 255, 204)
                            sector == "×3" -> android.graphics.Color.rgb(0, 255, 204)
                            else -> android.graphics.Color.WHITE
                        }

                        val FrPaint = android.graphics.Paint().apply {
                            color = sectorColor
                            textSize = (sizeFr * 0.062f).dp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        drawText(sector, 0f, 0f, FrPaint)
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
            text = if (lockActive) "🔒" else "🔻",
            fontSize = 36.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-2).dp)
        )
    }
}