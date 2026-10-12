package app.nexogram.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexogramApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexogramApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var ghostModeEnabled by remember { mutableStateOf(true) }
    var antiDeleteEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = when(selectedTab) {
                    0 -> "Nexogram • Чаты"
                    1 -> "Nexogram • Плагины"
                    else -> "Nexogram • Настройки"
                }) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("💬") },
                    label = { Text("Чаты") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🔌") },
                    label = { Text("Плагины") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("⚙️") },
                    label = { Text("Настройки") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> ChatListScreen()
                1 -> PluginsScreen()
                2 -> SettingsScreen(
                    ghostMode = ghostModeEnabled,
                    onGhostModeChanged = { ghostModeEnabled = it },
                    antiDelete = antiDeleteEnabled,
                    onAntiDeleteChanged = { antiDeleteEnabled = it }
                )
            }
        }
    }
}

@Composable
fun ChatListScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Подключение к TDLib и загрузка чатов...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PluginsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Менеджер плагинов (.apk / .dex)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { }) {
            Text("Загрузить плагин")
        }
    }
}

@Composable
fun SettingsScreen(
    ghostMode: Boolean,
    onGhostModeChanged: (Boolean) -> Unit,
    antiDelete: Boolean,
    onAntiDeleteChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Модификации Nexogram", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ghost Mode (скрыть read receipts)")
            Switch(checked = ghostMode, onCheckedChange = onGhostModeChanged)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Anti-Delete (сохранять удаленные)")
            Switch(checked = antiDelete, onCheckedChange = onAntiDeleteChanged)
        }
    }
}
