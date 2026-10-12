package app.nexogram.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsRoot() {
    var page by remember { mutableStateOf("") }
    BackHandler(enabled = page != "") { page = "" }
    when (page) {
        "ghost" -> GhostPage { page = "" }
        "spy" -> SpyPage { page = "" }
        "filters" -> FiltersPage { page = "" }
        "custom" -> CustomPage { page = "" }
        else -> SettingsMain { page = it }
    }
}

@Composable
fun SettingsMain(go: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NavRow("👻  Режим призрака", "Не читать, не печатать") { go("ghost") }
        NavRow("🕵  Шпион", "Сохранять удалённые сообщения") { go("spy") }
        NavRow("🧹  Фильтры", "Скрывать чаты и сообщения по словам") { go("filters") }
        NavRow("🎨  Кастомизация", "Цвет акцента") { go("custom") }
        NavRow("🚪  Выйти из аккаунта", "Потребуется перезапуск приложения") { Tg.logout() }
    }
}

@Composable
fun NavRow(title: String, sub: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                sub,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun PageHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Text("←", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    sub,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White,
                ),
            )
        }
    }
}

@Composable
fun GhostPage(back: () -> Unit) {
    var ghost by remember { mutableStateOf(Store.p.ghost) }
    var typing by remember { mutableStateOf(Store.p.hideTyping) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeader("Режим призрака", back)
        SwitchRow("Не отмечать прочитанным", "Собеседник не увидит, что вы прочитали", ghost) {
            ghost = it
            Store.p.ghost = it
        }
        SwitchRow("Скрывать «печатает…»", "Собеседник не увидит, что вы пишете", typing) {
            typing = it
            Store.p.hideTyping = it
        }
    }
}

@Composable
fun SpyPage(back: () -> Unit) {
    var spy by remember { mutableStateOf(Store.p.spy) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeader("Шпион", back)
        SwitchRow("Сохранять удалённые", "Удалённое собеседником остаётся в чате с пометкой 🗑", spy) {
            spy = it
            Store.p.spy = it
        }
        Text(
            "Работает, пока приложение открыто. Сохранение между запусками — позже.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun FiltersPage(back: () -> Unit) {
    var f by remember { mutableStateOf(Store.p.filters) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeader("Фильтры", back)
        Text(
            "Слова через запятую. Чаты с такими словами в названии и сообщения с ними будут скрыты.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = f,
            onValueChange = {
                f = it
                Store.p.filters = it
            },
            placeholder = { Text("слово1, слово2") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CustomPage(back: () -> Unit) {
    val presets = listOf(0xFFFF1744, 0xFFFF6D00, 0xFF00C853, 0xFF2979FF, 0xFFAA00FF, 0xFFF50057)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeader("Кастомизация", back)
        Text("Цвет акцента", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            presets.forEach { p ->
                val c = Color(p)
                val sel = NexoStyle.accent == c
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(if (sel) 3.dp else 0.dp, Color.White, CircleShape)
                        .clickable {
                            NexoStyle.accent = c
                            Store.p.accent = c.toArgb()
                        },
                )
            }
        }
    }
}
