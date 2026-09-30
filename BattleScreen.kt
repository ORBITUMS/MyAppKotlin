package com.example.myfirstapp

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val BsNeonRed = Color(0xFFE94560)

private enum class BattleStage { LOBBY, CREATE_FORM, DEVICE_LIST, SEARCHING, IN_BATTLE }

@Composable
fun BattleScreen(
    sharedPreferences: android.content.SharedPreferences,
    onBack: () -> Unit,
    onGoToShop: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var balance by remember { mutableStateOf(sharedPreferences.getInt("balance", 100)) }
    var freeSpins by remember { mutableStateOf(sharedPreferences.getInt("free_spins", 0)) }

    var betSpinsInput by remember { mutableStateOf("") }
    var betCoinsInput by remember { mutableStateOf("") }

    var myEarned by remember { mutableStateOf(0) }
    var opponentEarned by remember { mutableStateOf(0) }

    var mySpinsLeft by remember { mutableStateOf(0) }
    var opponentSpinsLeft by remember { mutableStateOf(0) }

    var stage by remember { mutableStateOf(BattleStage.LOBBY) }
    var isHost by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }

    var battleEndedReason by remember { mutableStateOf<String?>(null) }
    var showEndOverlay by remember { mutableStateOf(false) }

    val btManager = remember { BluetoothBattleManager(context, scope) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (!allGranted) {
            Toast.makeText(context, "Без разрешений Bluetooth не работает ❌", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        btManager.onConnected = {
            isConnected = true
            stage = BattleStage.IN_BATTLE
            battleEndedReason = null
            // Сообщаем сопернику свои стартовые спины
            btManager.send(BattleMessage("SPINS", listOf(mySpinsLeft.toString())))
            Toast.makeText(context, "Соединение установлено ✔", Toast.LENGTH_SHORT).show()
        }
        btManager.onMessage = { msg ->
            when (msg.type) {
                "MYWIN" -> {
                    val amount = msg.fields.getOrNull(0)?.toIntOrNull() ?: 0
                    opponentEarned += amount
                }
                "SPINS" -> {
                    val remaining = msg.fields.getOrNull(0)?.toIntOrNull() ?: 0
                    opponentSpinsLeft = remaining
                }
                "END" -> {
                    battleEndedReason = "Конец батла от соперника"
                }
                "BYE" -> {
                    battleEndedReason = "Соперник вышел"
                }
            }
        }
        btManager.onError = { err -> battleEndedReason = err }
        btManager.onTimeout = {
            battleEndedReason = "Соперник бездействовал 2 минуты — победа твоя!"
            val myCoins = betCoinsInput.toIntOrNull() ?: 0
            balance += myCoins * 2
            sharedPreferences.edit().putInt("balance", balance).apply()
            isConnected = false
            btManager.stop()
        }
        btManager.onOpponentLeft = {
            battleEndedReason = "Соединение потеряно. Ставки сгорели."
            isConnected = false
            btManager.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937))))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        TopBarCasinoMultiplayer(balance = balance, freeSpins = freeSpins)
        Spacer(modifier = Modifier.height(16.dp))

        when (stage) {
            BattleStage.LOBBY -> BattleLobby(
                balance = balance,
                freeSpins = freeSpins,
                myEarned = myEarned,
                opponentEarned = opponentEarned,
                onCreateClick = { stage = BattleStage.CREATE_FORM },
                onJoinClick = {
                    if (btManager.hasPermissions()) {
                        stage = BattleStage.CREATE_FORM   // ← клиент тоже вводит ставку
                    } else {
                        permissionLauncher.launch(btManager.requiredPermissions())
                    }
                },
                onGoToShop = onGoToShop,
                modifier = Modifier.weight(1f)
            )

            BattleStage.CREATE_FORM -> BattleCreateForm(
                balance = balance,
                freeSpins = freeSpins,
                isHost = isHost,
                betSpinsInput = betSpinsInput,
                onSpinsChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) betSpinsInput = input
                },
                betCoinsInput = betCoinsInput,
                onCoinsChange = { input ->
                    if (input.length <= 9 && input.all { it.isDigit() }) betCoinsInput = input
                },
                onStartBattle = {
                    val spinsBet = betSpinsInput.toIntOrNull() ?: 0
                    val coinsBet = betCoinsInput.toIntOrNull() ?: 0

                    val validSpins = spinsBet in 1..freeSpins
                    val validCoins = coinsBet in 0..balance

                    if (!validSpins) {
                        Toast.makeText(context, "Число фриспинов от 1 до $freeSpins ❌", Toast.LENGTH_SHORT).show()
                    } else if (!validCoins) {
                        Toast.makeText(context, "Ставка монет от 0 до $balance ❌", Toast.LENGTH_SHORT).show()
                    } else if (!btManager.hasPermissions()) {
                        permissionLauncher.launch(btManager.requiredPermissions())
                    } else {
                        freeSpins -= spinsBet
                        balance -= coinsBet
                        sharedPreferences.edit()
                            .putInt("free_spins", freeSpins)
                            .putInt("balance", balance)
                            .apply()

                        myEarned = 0
                        opponentEarned = 0
                        mySpinsLeft = spinsBet
                        opponentSpinsLeft = 0

                        if (isHost) {
                            stage = BattleStage.SEARCHING
                            btManager.startServer()
                        } else {
                            stage = BattleStage.DEVICE_LIST
                        }
                    }
                },
                onBack = { stage = BattleStage.LOBBY },
                modifier = Modifier.weight(1f)
            )

            BattleStage.DEVICE_LIST -> DeviceListScreen(
                btManager = btManager,
                onPick = { device ->
                    stage = BattleStage.SEARCHING
                    btManager.connectTo(device)
                },
                onBack = { stage = BattleStage.CREATE_FORM },
                modifier = Modifier.weight(1f)
            )

            BattleStage.SEARCHING -> BattleSearch(
                isHost = isHost,
                isConnected = isConnected,
                onCancel = {
                    btManager.stop()
                    val spinsBet = betSpinsInput.toIntOrNull() ?: 0
                    val coinsBet = betCoinsInput.toIntOrNull() ?: 0
                    balance += coinsBet
                    freeSpins += spinsBet
                    sharedPreferences.edit()
                        .putInt("balance", balance)
                        .putInt("free_spins", freeSpins)
                        .apply()
                    stage = BattleStage.LOBBY
                },
                modifier = Modifier.weight(1f)
            )

            BattleStage.IN_BATTLE -> InBattleScreen(
                balance = balance,
                freeSpins = freeSpins,
                myEarned = myEarned,
                opponentEarned = opponentEarned,
                mySpinsLeft = mySpinsLeft,
                opponentSpinsLeft = opponentSpinsLeft,
                betSpins = betSpinsInput.toIntOrNull() ?: 0,
                betCoins = betCoinsInput.toIntOrNull() ?: 0,
                onSpin = { winAmount ->
                    myEarned += winAmount
                    btManager.heartbeat()
                    btManager.send(BattleMessage("MYWIN", listOf(winAmount.toString())))
                },
                onSpinsChanged = { newLeft ->
                    mySpinsLeft = newLeft
                    btManager.heartbeat()
                    btManager.send(BattleMessage("SPINS", listOf(newLeft.toString())))
                },
                onExit = {
                    btManager.send(BattleMessage("BYE"))
                    btManager.stop()
                    isConnected = false
                    stage = BattleStage.LOBBY
                    myEarned = 0
                    opponentEarned = 0
                    mySpinsLeft = 0
                    opponentSpinsLeft = 0
                },
                onBothFinished = { myFinal, oppFinal ->
                    val betCoins = betCoinsInput.toIntOrNull() ?: 0
                    if (myFinal > oppFinal) {
                        balance += betCoins * 2
                        sharedPreferences.edit().putInt("balance", balance).apply()
                    }
                    showEndOverlay = true
                    btManager.send(BattleMessage("END"))
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (battleEndedReason != null) {
            BattleEndDialog(
                reason = battleEndedReason!!,
                myEarned = myEarned,
                opponentEarned = opponentEarned,
                onDismiss = {
                    btManager.stop()
                    battleEndedReason = null
                    isConnected = false
                    stage = BattleStage.LOBBY
                    myEarned = 0
                    opponentEarned = 0
                }
            )
        }

        Text(
            text = "Назад",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .clickable {
                    btManager.stop()
                    battleEndedReason = null
                    if (stage == BattleStage.LOBBY) onBack() else stage = BattleStage.LOBBY
                }
        )
    }

    if (showEndOverlay) {
        EndGameOverlay(
            myName = "ТЫ",
            opponentName = "СОПЕРНИК",
            myEarned = myEarned,
            opponentEarned = opponentEarned,
            onDismiss = {
                showEndOverlay = false
                btManager.stop()
                isConnected = false
                stage = BattleStage.LOBBY
                myEarned = 0
                opponentEarned = 0
                mySpinsLeft = 0
                opponentSpinsLeft = 0
            }
        )
    }
}

// ===================== ЛОББИ =====================
@Composable
private fun BattleLobby(
    balance: Int,
    freeSpins: Int,
    myEarned: Int,
    opponentEarned: Int,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onGoToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚔️ БАТЛ", fontSize = 28.sp, fontWeight = FontWeight.Black,
            color = MpNeonCyan, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(24.dp))

        BattleMiniStats(balance, freeSpins, myEarned, opponentEarned)
        Spacer(modifier = Modifier.height(32.dp))

        if (freeSpins <= 0) {
            Text("🎁 У тебя 0 фриспинов.\nБез них создать батл нельзя.",
                color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onGoToShop,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1730),
                    contentColor = MpGoldAccent
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .border(1.5.dp, MpGoldAccent, RoundedCornerShape(14.dp))
            ) {
                Text("🎁 В магазин фриспинов", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        } else {
            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1730),
                    contentColor = MpNeonCyan
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .border(1.5.dp, MpNeonCyan, RoundedCornerShape(14.dp))
            ) {
                Text("⚔️ Создать батл", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onJoinClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1730),
                    contentColor = MpNeonGreen
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .border(1.5.dp, MpNeonGreen, RoundedCornerShape(14.dp))
            ) {
                Text("🔗 Подключиться к батлу", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun BattleMiniStats(balance: Int, freeSpins: Int, myEarned: Int, opponentEarned: Int) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(listOf(MpGoldAccent, Color(0xFFFFA751)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniStatRow("💰", "Баланс", balance.toString(), MpGoldAccent)
            MiniStatRow("🎁", "Фриспины", freeSpins.toString(), MpNeonCyan)
            MiniStatRow("🏆", "Ты выиграл", myEarned.toString(), MpNeonGreen)
            MiniStatRow("👤", "Соперник выиграл", opponentEarned.toString(), BsNeonRed)
        }
    }
}

@Composable
private fun MiniStatRow(emoji: String, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Text(value, fontSize = 16.sp, color = accent, fontWeight = FontWeight.Black)
    }
}

// ===================== ФОРМА СОЗДАНИЯ =====================
@Composable
private fun BattleCreateForm(
    balance: Int,
    freeSpins: Int,
    isHost: Boolean,
    betSpinsInput: String,
    onSpinsChange: (String) -> Unit,
    betCoinsInput: String,
    onCoinsChange: (String) -> Unit,
    onStartBattle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isHost) "⚔️ СОЗДАНИЕ БАТЛА" else "🔗 ПОДКЛЮЧЕНИЕ К БАТЛУ",
            fontSize = 22.sp, fontWeight = FontWeight.Black,
            color = MpNeonCyan, letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Обе ставки списываются сразу.\nПобедитель забирает всё.",
            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        BattleInputField("🎁 Число фриспинов", "От 1 до $freeSpins",
            betSpinsInput, onSpinsChange, freeSpins, MpNeonCyan)
        Spacer(modifier = Modifier.height(14.dp))
        BattleInputField("💰 Ставка монет", "От 0 до $balance",
            betCoinsInput, onCoinsChange, balance, MpGoldAccent)
        Spacer(modifier = Modifier.height(14.dp))

        // Кнопки-сумматоры для фриспинов
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 5, 10, 25).forEach { n ->
                QuickBetButton("+$n 🎁", true, MpNeonCyan,
                    {
                        val current = betSpinsInput.toIntOrNull() ?: 0
                        val newValue = (current + n).coerceAtMost(freeSpins)
                        onSpinsChange(newValue.toString())
                    }, Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Кнопки-сумматоры для монет
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(10, 50, 100, 500).forEach { n ->
                QuickBetButton("+$n 💰", true, MpGoldAccent,
                    {
                        val current = betCoinsInput.toIntOrNull() ?: 0
                        val newValue = (current + n).coerceAtMost(balance)
                        onCoinsChange(newValue.toString())
                    }, Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStartBattle,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1730),
                contentColor = MpNeonGreen
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .border(1.5.dp, MpNeonGreen, RoundedCornerShape(14.dp))
        ) {
            Text(
                text = if (isHost) "⚔️ Создать и ждать соперника" else "🔗 Подключиться к сопернику",
                fontWeight = FontWeight.Black, fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun BattleInputField(
    label: String, hint: String, value: String,
    onValueChange: (String) -> Unit, maxAvailable: Int, accent: Color
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("макс: $maxAvailable", fontSize = 11.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, color = Color.DarkGray, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MpDarkBg,
                unfocusedContainerColor = MpDarkBg,
                focusedBorderColor = accent,
                unfocusedBorderColor = Color(0xFF3A3F58),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = accent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuickBetButton(
    text: String, enabled: Boolean, accent: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A1730),
            disabledContainerColor = Color(0xFF1A1730)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(36.dp)
            .border(
                width = 1.dp,
                color = if (enabled) accent.copy(alpha = 0.85f) else Color(0xFF3A3F58),
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (enabled) accent else Color.Gray
        )
    }
}

// ===================== СПИСОК УСТРОЙСТВ =====================
@Composable
private fun DeviceListScreen(
    btManager: BluetoothBattleManager,
    onPick: (BluetoothDevice) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val devices = remember { btManager.bondedDevices() }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔗 ВЫБЕРИ УСТРОЙСТВО", fontSize = 20.sp, fontWeight = FontWeight.Black,
            color = MpNeonCyan, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Показываются только спаренные устройства.\nСначала спарь телефоны в настройках Bluetooth.",
            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))

        if (devices.isEmpty()) {
            Text("😔 Спаренных устройств не найдено.\nЗайди в настройки Bluetooth и спарься с другом.",
                fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    DeviceRow(
                        name = btManager.deviceName(device),
                        address = device.address,
                        onClick = { onPick(device) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("← Назад", color = Color.Gray, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp).clickable { onBack() })
    }
}

@Composable
private fun DeviceRow(name: String, address: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MpNeonGreen.copy(alpha = 0.85f)),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📱", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(address, fontSize = 11.sp, color = Color.Gray)
            }
            Text("▶", fontSize = 18.sp, color = MpNeonGreen)
        }
    }
}

// ===================== ЭКРАН ПОИСКА =====================
@Composable
private fun BattleSearch(
    isHost: Boolean,
    isConnected: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "" -> "."; "." -> ".."; ".." -> "..."; else -> ""
            }
            delay(400)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isHost) "ОЖИДАНИЕ СОПЕРНИКА$dots" else "ПОДКЛЮЧЕНИЕ$dots",
            fontSize = 20.sp, fontWeight = FontWeight.Black,
            color = MpNeonCyan, letterSpacing = 2.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isHost)
                "Убедись, что друг зашёл в «Подключиться к батлу»\nи выбрал этот телефон в списке."
            else "Подключаемся к телефону друга...",
            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onCancel,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = BsNeonRed
            ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 6.dp),
            modifier = Modifier.height(40.dp)
                .border(1.dp, BsNeonRed.copy(alpha = 0.8f), RoundedCornerShape(50))
        ) {
            Text("Отменить", color = BsNeonRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ===================== ЭКРАН БАТЛА =====================
@Composable
private fun InBattleScreen(
    balance: Int,
    freeSpins: Int,
    myEarned: Int,
    opponentEarned: Int,
    mySpinsLeft: Int,
    opponentSpinsLeft: Int,
    betSpins: Int,
    betCoins: Int,
    onSpin: (Int) -> Unit,
    onSpinsChanged: (Int) -> Unit,
    onExit: () -> Unit,
    onBothFinished: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val wheelRotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    fun doSpin() {
        if (isSpinning || mySpinsLeft <= 0) return
        isSpinning = true

        scope.launch {
            val target = wheelRotation.value + 1440f + Random.nextInt(360).toFloat()
            wheelRotation.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )

            val finalNorm = (wheelRotation.value % 360f + 360f) % 360f
            val sectorAngle = 360f / 6f
            val angleUnderPointer = ((-finalNorm) % 360f + 360f) % 360f
            val idx = (angleUnderPointer / sectorAngle).toInt().coerceIn(0, 5)

            // Без 💎. Ряд: 5, 10, 15, 25, 33, 50
            val prize = when (idx) {
                0 -> 5
                1 -> 10
                2 -> 15
                3 -> 25
                4 -> 33
                5 -> 50
                else -> 0
            }

            onSpin(prize)
            onSpinsChanged(mySpinsLeft - 1)
            isSpinning = false

            // Финал срабатывает ТОЛЬКО если у обоих 0 и ставка была больше 0
            if (mySpinsLeft - 1 <= 0 && opponentSpinsLeft <= 0 && betSpins > 0) {
                delay(2000)
                onBothFinished(myEarned + prize, opponentEarned)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BattleCounterCard("🏆 ТЫ", myEarned, mySpinsLeft, MpNeonGreen, Modifier.weight(1f))
            BattleCounterCard("👤 СОПЕРНИК", opponentEarned, opponentSpinsLeft, BsNeonRed, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Text("Банк: ${betCoins * 2} 💰   •   Фриспинов: $betSpins 🎁",
            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.fillMaxSize().rotate(wheelRotation.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2f
                    val sweep = 360f / 6f

                    val sectorColors = listOf(
                        Color(0xFF2A1B54), Color(0xFF3D2C7A),
                        Color(0xFF2A1B54), Color(0xFF3D2C7A),
                        Color(0xFF2A1B54), Color(0xFF3D2C7A)
                    )
                    // Без 💎
                    val prizes = listOf("5", "10", "15", "25", "33", "50")

                    for (i in 0 until 6) {
                        val startAngle = i * sweep - 90f
                        drawArc(
                            color = sectorColors[i],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size.width, size.height),
                            topLeft = Offset.Zero
                        )
                        drawArc(
                            color = MpGoldAccent.copy(alpha = 0.4f),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size.width, size.height),
                            topLeft = Offset.Zero,
                            style = Stroke(width = 1.5f)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            save()
                            val median = ((startAngle + sweep / 2f) * PI / 180f).toFloat()
                            val textDist = radius * 0.65f
                            val x = center.x + textDist * cos(median)
                            val y = center.y + textDist * sin(median)

                            translate(x, y)
                            rotate(startAngle + sweep / 2f + 90f)

                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 34f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText(prizes[i], 0f, 0f, paint)
                            restore()
                        }
                    }

                    drawCircle(
                        color = MpGoldAccent,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
            }

            Text("🔻", fontSize = 32.sp,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-2).dp))
        }

        Spacer(Modifier.height(20.dp))

        // Кнопка активна, пока лично у тебя есть спины — не зависит от соперника
        Button(
            onClick = { doSpin() },
            enabled = !isSpinning && mySpinsLeft > 0,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1730),
                contentColor = MpNeonGreen,
                disabledContainerColor = Color(0xFF2A1B30)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(1.5.dp, MpNeonGreen, RoundedCornerShape(14.dp))
        ) {
            Text(
                text = if (mySpinsLeft > 0) "🎰 КРУТИТЬ ($mySpinsLeft)" else "СПИНЫ ЗАКОНЧИЛИСЬ",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onExit,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = BsNeonRed
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
            modifier = Modifier
                .height(38.dp)
                .border(1.dp, BsNeonRed.copy(alpha = 0.8f), RoundedCornerShape(50))
        ) {
            Text(
                text = "Выйти из батла",
                color = BsNeonRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ===================== КАРТОЧКА СЧЁТЧИКА =====================
@Composable
private fun BattleCounterCard(
    title: String,
    earned: Int,
    spinsLeft: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$earned 💰",
                fontSize = 20.sp,
                color = accent,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "спинов: $spinsLeft",
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}

// ===================== ФИНАЛЬНЫЙ ОВЕРЛЕЙ =====================
@Composable
fun EndGameOverlay(
    myName: String,
    opponentName: String,
    myEarned: Int,
    opponentEarned: Int,
    onDismiss: () -> Unit
) {
    val isWin = myEarned > opponentEarned
    val isDraw = myEarned == opponentEarned

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val bgAlpha by animateFloatAsState(
        targetValue = if (visible) 0.92f else 0f,
        animationSpec = tween(500),
        label = "EndBgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wDp = maxWidth.value
            val hDp = maxHeight.value

            val centerOffset = Offset(wDp / 2f, hDp / 2f)
            val leftAnchor = Offset(wDp * 0.25f, hDp * 0.55f)
            val rightAnchor = Offset(wDp * 0.75f, hDp * 0.55f)

            val targetAnchor = when {
                isWin -> leftAnchor
                isDraw -> centerOffset
                else -> rightAnchor
            }

            var medalPosition by remember { mutableStateOf(centerOffset) }
            val medalOffset by androidx.compose.animation.core.animateOffsetAsState(
                targetValue = medalPosition,
                animationSpec = tween(durationMillis = 800, delayMillis = 400),
                label = "MedalOffset"
            )

            LaunchedEffect(visible) {
                if (visible) {
                    delay(300)
                    medalPosition = targetAnchor
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = when {
                        isWin -> "🏆 ПОБЕДА!"
                        isDraw -> "🤝 НИЧЬЯ"
                        else -> "💀 ПОРАЖЕНИЕ"
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isWin -> MpNeonGreen
                        isDraw -> MpGoldAccent
                        else -> BsNeonRed
                    },
                    letterSpacing = 3.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EndPlayerCard(
                    name = myName,
                    earned = myEarned,
                    accent = MpNeonGreen,
                    isWinner = isWin
                )
                EndPlayerCard(
                    name = opponentName,
                    earned = opponentEarned,
                    accent = BsNeonRed,
                    isWinner = !isWin && !isDraw
                )
            }

            Text(
                text = "🏆",
                fontSize = 64.sp,
                modifier = Modifier
                    .offset(
                        x = (medalOffset.x - 32).dp,
                        y = (medalOffset.y - 32).dp
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MpGoldAccent,
                        contentColor = Color(0xFF1A1208)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("В ЛОББИ", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EndPlayerCard(
    name: String,
    earned: Int,
    accent: Color,
    isWinner: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MpDarkBg),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isWinner) 2.dp else 1.dp,
            color = if (isWinner) accent else Color(0xFF3A3F58)
        ),
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$earned",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )
            Text(
                text = "💰",
                fontSize = 16.sp
            )
        }
    }
}

// ===================== ДИАЛОГ ЗАВЕРШЕНИЯ (ошибки/таймаут) =====================
@Composable
private fun BattleEndDialog(
    reason: String,
    myEarned: Int,
    opponentEarned: Int,
    onDismiss: () -> Unit
) {
    val isWin = myEarned > opponentEarned
    val isLose = myEarned < opponentEarned
    val title = when {
        isWin -> "🏆 ПОБЕДА!"
        isLose -> "💀 ПОРАЖЕНИЕ"
        else -> "⚠️ БАТЛ ЗАВЕРШЁН"
    }
    val accent = when {
        isWin -> MpNeonGreen
        isLose -> BsNeonRed
        else -> MpGoldAccent
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF12101F),
        title = {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent)
        },
        text = {
            Column {
                Text(reason, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Ты выиграл: $myEarned 💰", color = MpNeonGreen, fontSize = 13.sp)
                Text("Соперник: $opponentEarned 💰", color = BsNeonRed, fontSize = 13.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MpGoldAccent,
                    contentColor = Color(0xFF1A1208)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ОК", fontWeight = FontWeight.Black)
            }
        }
    )
}