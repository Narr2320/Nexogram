package app.nexogram.client

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val NexoRed = Color(0xFFFF1744)
private val NexoBg = Color(0xFF0B0B0D)
private val NexoCard = Color(0xFF16161A)

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("nexo", Context.MODE_PRIVATE)

    var ghost: Boolean
        get() = sp.getBoolean("ghost", false)
        set(v) = sp.edit().putBoolean("ghost", v).apply()

    var hideTyping: Boolean
        get() = sp.getBoolean("hideTyping", false)
        set(v) = sp.edit().putBoolean("hideTyping", v).apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)
        setContent {
            NexoTheme { NexoApp(prefs) }
        }
    }
}

@Composable
fun NexoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexoRed,
            onPrimary = Color.White,
            background = NexoBg,
            onBackground = Color(0xFFEDEDED),
            surface = NexoCard,
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = Color(0xFF22222A),
            onSurfaceVariant = Color(0xFFB5B5BD),
            secondaryContainer = Color(0xFF3A1016),
            onSecondaryContainer = Color.White,
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoApp(prefs: Prefs) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Чаты", "Плаги

cd /workspaces/Nexogram 2>/dev/null || cd ~/Nexogram 2>/dev/null; pwd
echo "=== ДО ==="; ls -la nexo/app/src/main/java/app/nexogram/client/; wc -l nexo/app/src/main/java/app/nexogram/client/*.kt
mkdir -p nexo/app/src/main/java/app/nexogram/client
cat > nexo/app/src/main/java/app/nexogram/client/MainActivity.kt <<'EOF'
package app.nexogram.client

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val NexoRed = Color(0xFFFF1744)
private val NexoBg = Color(0xFF0B0B0D)
private val NexoCard = Color(0xFF16161A)

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("nexo", Context.MODE_PRIVATE)

    var ghost: Boolean
        get() = sp.getBoolean("ghost", false)
        set(v) = sp.edit().putBoolean("ghost", v).apply()

    var hideTyping: Boolean
        get() = sp.getBoolean("hideTyping", false)
        set(v) = sp.edit().putBoolean("hideTyping", v).apply()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)
        setContent {
            NexoTheme { NexoApp(prefs) }
        }
    }
}

@Composable
fun NexoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexoRed,
            onPrimary = Color.White,
            background = NexoBg,
            onBackground = Color(0xFFEDEDED),
            surface = NexoCard,
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = Color(0xFF22222A),
            onSurfaceVariant = Color(0xFFB5B5BD),
            secondaryContainer = Color(0xFF3A1016),
            onSecondaryContainer = Color.White,
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoApp(prefs: Prefs) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Чаты", "Плагины", "Настройки")
    val icons = listOf(Icons.Filled.Email, Icons.Filled.Build, Icons.Filled.Settings)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nexogram · " + titles[tab], fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                titles.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icons[i], contentDescription = t) },
                        label = { Text(t) },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> Placeholder("Здесь будут ваши чаты", "Вход в Telegram добавим следующим шагом")
                1 -> Placeholder("Плагинов пока нет", "Здесь появятся ваши скрипты")
                else -> SettingsScreen(prefs)
            }
        }
    }
}

@Composable
fun Placeholder(title: String, sub: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun SettingsScreen(prefs: Prefs) {
    var ghost by remember { mutableStateOf(prefs.ghost) }
    var typing by remember { mutableStateOf(prefs.hideTyping) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SwitchRow("Режим призрака", "Не отмечать сообщения прочитанными", ghost) {
            ghost = it
            prefs.ghost = it
        }
        SwitchRow("Скрывать «печатает…»", "Собеседник не увидит, что вы пишете", typing) {
            typing = it
            prefs.hideTyping = it
        }
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
