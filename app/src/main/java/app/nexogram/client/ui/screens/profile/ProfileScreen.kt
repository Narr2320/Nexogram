package app.nexogram.client.ui.screens.settings.customization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomizationScreen(onBack: () -> Unit) {
    var bubbleRadius by remember { mutableStateOf(16f) }
    var animationSpeed by remember { mutableStateOf(1f) }
    var showIds by remember { mutableStateOf(false) }
    var smoothScroll by remember { mutableStateOf(true) }
    var pressEffects by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кастомизация") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Скругление пузырей: ${bubbleRadius.toInt()}dp")
            Slider(value = bubbleRadius, onValueChange = { bubbleRadius = it }, valueRange = 0f..30f)

            Text("Скорость анимаций: ${String.format("%.1f", animationSpeed)}x")
            Slider(value = animationSpeed, onValueChange = { animationSpeed = it }, valueRange = 0.5f..2f)

            HorizontalDivider()

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = showIds, onCheckedChange = { showIds = it })
                Text("Показывать ID в чатах")
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = smoothScroll, onCheckedChange = { smoothScroll = it })
                Text("Плавная прокрутка")
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = pressEffects, onCheckedChange = { pressEffects = it })
                Text("Эффекты при нажатии")
            }

            Text("Дополнительные плашки появятся здесь позже.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
package