package com.example.myfirstapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

// ==========================================================
// МЕНЕДЖЕР BLUETOOTH-БАТЛА
// Отвечает за сервер, клиент, обмен сообщениями, таймаут.
// UI (BattleScreen) подписывается на колбэки.
// ==========================================================
class BluetoothBattleManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        // Уникальный UUID нашего приложения. Должен быть одинаковый на обоих телефонах.
        private val APP_UUID: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
        private const val SERVICE_NAME = "MysorBattle"
        private const val TIMEOUT_MS = 120_000L // 2 минуты бездействия
    }

    // Колбэки, которые UI устанавливает
    var onConnected: (() -> Unit)? = null
    var onMessage: ((BattleMessage) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onTimeout: (() -> Unit)? = null
    var onOpponentLeft: (() -> Unit)? = null

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var output: OutputStream? = null
    private var readerJob: Job? = null
    private var timeoutJob: Job? = null

    private var lastMessageTime: Long = System.currentTimeMillis()

    // ===== РАЗРЕШЕНИЯ =====
    fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    // ===== СПАРЕННЫЕ УСТРОЙСТВА (для клиента) =====
    @SuppressLint("MissingPermission")
    fun bondedDevices(): List<BluetoothDevice> {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = manager?.adapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return adapter.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun deviceName(device: BluetoothDevice): String {
        return try {
            device.name ?: device.address
        } catch (e: SecurityException) {
            device.address
        }
    }

    // ===== СЕРВЕР (ХОСТ) =====
    fun startServer() {
        stop()
        scope.launch(Dispatchers.IO) {
            try {
                val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val adapter = manager?.adapter
                if (adapter == null) {
                    withContext(Dispatchers.Main) { onError?.invoke("Bluetooth недоступен") }
                    return@launch
                }
                @Suppress("MissingPermission")
                if (!adapter.isEnabled) {
                    withContext(Dispatchers.Main) { onError?.invoke("Включи Bluetooth") }
                    return@launch
                }

                @Suppress("MissingPermission")
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)

                @Suppress("MissingPermission")
                val socket = serverSocket?.accept() // блокирует до подключения

                if (socket == null) {
                    withContext(Dispatchers.Main) { onError?.invoke("Не удалось принять подключение") }
                    return@launch
                }
                clientSocket = socket
                output = socket.outputStream

                withContext(Dispatchers.Main) { onConnected?.invoke() }

                startReading()
                startTimeoutWatcher()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError?.invoke("Ошибка сервера: ${e.message}") }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) { onError?.invoke("Нет разрешения Bluetooth") }
            }
        }
    }

    // ===== КЛИЕНТ =====
    fun connectTo(device: BluetoothDevice) {
        stop()
        scope.launch(Dispatchers.IO) {
            try {
                @Suppress("MissingPermission")
                val socket = device.createRfcommSocketToServiceRecord(APP_UUID)

                @Suppress("MissingPermission")
                try {
                    // Останавливаем обнаружение, если идёт — оно тормозит connect
                    (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
                        ?.adapter?.cancelDiscovery()
                } catch (_: SecurityException) {}

                socket.connect() // блокирует
                clientSocket = socket
                output = socket.outputStream

                withContext(Dispatchers.Main) { onConnected?.invoke() }

                startReading()
                startTimeoutWatcher()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onError?.invoke("Не удалось подключиться: ${e.message}") }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) { onError?.invoke("Нет разрешения Bluetooth") }
            }
        }
    }

    // ===== ОТПРАВКА СООБЩЕНИЯ =====
    fun send(msg: BattleMessage) {
        scope.launch(Dispatchers.IO) {
            try {
                val bytes = (msg.serialize() + "\n").toByteArray()
                output?.write(bytes)
                output?.flush()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onError?.invoke("Соединение потеряно: ${e.message}")
                }
            }
        }
    }

    // ===== ЧТЕНИЕ СООБЩЕНИЙ =====
    private fun startReading() {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket?.inputStream))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    lastMessageTime = System.currentTimeMillis()
                    val parsed = BattleMessage.parse(line) ?: continue
                    withContext(Dispatchers.Main) { onMessage?.invoke(parsed) }
                }
                // Сокет закрылся — значит, соперник отключился
                withContext(Dispatchers.Main) { onOpponentLeft?.invoke() }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { onOpponentLeft?.invoke() }
            }
        }
    }

    // ===== ТАЙМАУТ 2 МИНУТЫ =====
    private fun startTimeoutWatcher() {
        timeoutJob?.cancel()
        lastMessageTime = System.currentTimeMillis()
        timeoutJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5_000L)
                val elapsed = System.currentTimeMillis() - lastMessageTime
                if (elapsed >= TIMEOUT_MS) {
                    withContext(Dispatchers.Main) { onTimeout?.invoke() }
                    break
                }
            }
        }
    }

    // Пинговать при каждом своём действии — обнуляет таймер
    fun heartbeat() {
        lastMessageTime = System.currentTimeMillis()
    }

    // ===== ЗАКРЫТИЕ =====
    fun stop() {
        try { readerJob?.cancel() } catch (_: Exception) {}
        try { timeoutJob?.cancel() } catch (_: Exception) {}
        try { output?.close() } catch (_: Exception) {}
        try { clientSocket?.close() } catch (_: Exception) {}
        try { serverSocket?.close() } catch (_: Exception) {}
        readerJob = null
        timeoutJob = null
        output = null
        clientSocket = null
        serverSocket = null
    }
}

// ==========================================================
// СООБЩЕНИЯ ПРОТОКОЛА
// Формат: TYPE|field1|field2\n
// ==========================================================
data class BattleMessage(
    val type: String,
    val fields: List<String> = emptyList()
) {
    fun serialize(): String = (listOf(type) + fields).joinToString("|")

    companion object {
        fun parse(line: String): BattleMessage? {
            val parts = line.split("|")
            if (parts.isEmpty()) return null
            return BattleMessage(parts[0], parts.drop(1))
        }
    }
}