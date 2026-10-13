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
