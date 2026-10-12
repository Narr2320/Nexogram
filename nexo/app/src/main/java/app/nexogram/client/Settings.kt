package app.nexogram.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        "devices" -> DevicesPage { page = "" }
        else -> SettingsMain { page = it }
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
fun IconRow(emoji: String, tint: Color, title: String, sub: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(tint),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 16.sp)
            Text(
                sub,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun SettingsMain(go: (String) -> Unit) {
    LaunchedEffect(Tg.stage) { if (Tg.stage == Stage.Ready) Extras.loadMe() }
    var confirm by remember { mutableStateOf(false) }
    val me = Extras.me
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (me != null) {
            Avatar(me.name, me.photoId, me.photoPath, 96.dp)
            Spacer(Modifier.height(10.dp))
            Text(me.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            val line = "+" + me.phone + (if (me.username.isNotEmpty()) "  •  @" + me.username else "")
            Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
        } else {
            Text("Настройки", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }
        SectionCard {
            IconRow("👻", Color(0xFF7E57C2), "Режим призрака", "Не читать, не печатать") { go("ghost") }
            IconRow("🕵", Color(0xFF5C6BC0), "Шпион", "Сохранять удалённые сообщения") { go("spy") }
            IconRow("🧹", Color(0xFF26A69A), "Фильтры", "Скрывать чаты и сообщения по словам") { go("filters") }
            IconRow("🎨", Color(0xFFEF6C00), "Кастомизация", "Тема и цвет акцента") { go("custom") }
        }
        Spacer(Modifier.height(12.dp))
        SectionCard {
            IconRow("💻", Color(0xFF00ACC1), "Устройства", "Активные сеансы") { go("devices") }
            IconRow("🚪", Color(0xFFE53935), "Выйти из аккаунта", "Потребуется войти заново") { confirm = true }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Сессия на этом устройстве будет завершена, потребуется войти заново.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    Tg.logout()
                }) { Text("Выйти") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Отмена") } },
        )
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
fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) NexoStyle.dim else MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun CustomPage(back: () -> Unit) {
    val presets = listOf(0xFFFF1744, 0xFF3390EC, 0xFFFF6D00, 0xFF00C853, 0xFF2979FF, 0xFFAA00FF, 0xFFF50057)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeader("Кастомизация", back)
        Text("Тема", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeChip("Чёрная", !NexoStyle.navy) {
                NexoStyle.navy = false
                Store.p.navy = false
            }
            ThemeChip("Тёмно-синяя", NexoStyle.navy) {
                NexoStyle.navy = true
                Store.p.navy = true
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Цвет акцента", fontWeight = FontWeight.SemiBold)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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

@Composable
fun DevicesPage(back: () -> Unit) {
    LaunchedEffect(Unit) { Extras.loadSessions() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageHeader("Устройства", back)
        if (Extras.sessions.isEmpty()) {
            Text("Загрузка…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Extras.sessions.forEach { s ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(s.title + if (s.current) "  (это устройство)" else "", fontWeight = FontWeight.SemiBold)
                    Text(
                        s.sub,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
