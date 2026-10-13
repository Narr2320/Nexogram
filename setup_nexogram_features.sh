#!/bin/bash

echo "Создаю структуру папок для новых фич..."
mkdir -p app/src/main/java/app/nexogram/client/ui/screens/profile
mkdir -p app/src/main/java/app/nexogram/client/ui/screens/chat/media
mkdir -p app/src/main/java/app/nexogram/client/ui/screens/settings/customization

echo "Создаю экран профиля (Карточка контакта)..."
cat << 'KOTLIN' > app/src/main/java/app/nexogram/client/ui/screens/profile/ProfileScreen.kt
package app.nexogram.client.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(userId: Long) {
    // TODO: Здесь нужно вызвать TDLib getUserFullInfo(userId)
    // TODO: Добавить отображение подарков, музыки, историй
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Профиль пользователя: $userId", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Музыка: (нужен запрос к TDLib)", style = MaterialTheme.typography.bodyMedium)
        Text("Подарки: (нужен запрос к TDLib)", style = MaterialTheme.typography.bodyMedium)
        Text("Истории: (нужен запрос к TDLib)", style = MaterialTheme.typography.bodyMedium)
    }
}
KOTLIN

echo "Создаю компоненты для Голосовых и Видео-сообщений..."
cat << 'KOTLIN' > app/src/main/java/app/nexogram/client/ui/screens/chat/media/VoiceRecorder.kt
package app.nexogram.client.ui.screens.chat.media

import android.media.MediaRecorder
import androidx.compose.runtime.*
import java.io.File

@Composable
fun VoiceRecorderButton(
    onSendVoice: (File, Int) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var file: File? by remember { mutableStateOf(null) }

    // TODO: Реализовать логику onLongPress -> startRecording
    // TODO: Реализовать onRelease -> stopRecording и отправку через TDLib InputMessageVoiceNote
}
KOTLIN

cat << 'KOTLIN' > app/src/main/java/app/nexogram/client/ui/screens/chat/media/VideoNoteRecorder.kt
package app.nexogram.client.ui.screens.chat.media

// TODO: Аналогично VoiceRecorder, но с использованием Camera API и InputMessageVideoNote
KOTLIN

echo "Создаю расширенные настройки кастомизации..."
cat << 'KOTLIN' > app/src/main/java/app/nexogram/client/ui/screens/settings/customization/CustomizationScreen.kt
package app.nexogram.client.ui.screens.settings.customization

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomizationScreen() {
    var bubbleRadius by remember { mutableStateOf(16f) }
    var animationSpeed by remember { mutableStateOf(1f) }
    var showIds by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Кастомизация интерфейса", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Скругление пузырей: ${bubbleRadius.toInt()}dp")
        Slider(value = bubbleRadius, onValueChange = { bubbleRadius = it }, valueRange = 0f..30f)
        
        Text("Скорость анимаций: ${animationSpeed}x")
        Slider(value = animationSpeed, onValueChange = { animationSpeed = it }, valueRange = 0.5f..2f)
        
        Row {
            Checkbox(checked = showIds, onCheckedChange = { showIds = it })
            Text("Показывать ID в чатах")
        }
        
        // TODO: Добавить переключатели для "Плавной прокрутки", "Эффектов нажатия" и т.д.
    }
}
KOTLIN

echo "Готово! Все каркасы созданы."
echo "Не забудь добавить эти экраны в свой Navigation (MainActivity.kt или Navigator.kt)"
