# 🎰 My App — первое приложение на Kotlin

Это моё первое мобильное приложение, написанное на Kotlin с использованием Jetpack Compose.

Изначально проект задумывался как **тренажёр для прокачки скиллов** в Kotlin, но постепенно вырос в полноценную мини-игру в стиле казино. Сейчас это небольшая игровая платформа со слотами, фриспинами, апгрейдером ставок, банком, кардингом и промокодами.

Проект живой — я продолжаю добавлять новые фичи по мере изучения Kotlin, Jetpack Compose и корутин.

---

## 🛠️ Технологический стек

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material Design 3
- **Navigation:** Jetpack Navigation Component
- **State:** `remember`, `mutableStateOf`, `Animatable`, `AnimatedContent`, `animateColorAsState`
- **Storage:** `SharedPreferences` (единый ключ `casino_prefs` для всех экранов)
- **Async:** Kotlin Coroutines + `LaunchedEffect`, `rememberCoroutineScope`
- **Graphics:** Canvas, Path, Brush, custom drawing

---

## 🏗️ Структура кода

Основная логика разбита по экранам, каждый экран — отдельная `@Composable`-функция в своём файле.

## 🚀 Планы на будущее

- Статистика.
- Тёмная/светлая тема на выбор.
- Звуковые эффекты.

Цель — продолжать прокачивать Kotlin через реальный проект, добавляя новые фичи каждую неделю.

---


## 📌 Теги

`kotlin` `jetpack-compose` `android` `game` `casino` `slots` `coroutines` `material3` `learning-project`
