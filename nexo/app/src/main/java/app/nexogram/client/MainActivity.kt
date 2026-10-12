package app.nexogram.client

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("nexo", Context.MODE_PRIVATE)

    var ghost: Boolean
        get() = sp.getBoolean("ghost", false)
        set(v) = sp.edit().putBoolean("ghost", v).apply()

    var hideTyping: Boolean
        get() = sp.getBoolean("hideTyping", false)
        set(v) = sp.edit().putBoolean("hideTyping", v).apply()

    var spy: Boolean
        get() = sp.getBoolean("spy", false)
        set(v) = sp.edit().putBoolean("spy", v).apply()

    var accent: Int
        get() = sp.getInt("accent", 0xFFFF1744.toInt())
        set(v) = sp.edit().putInt("accent", v).apply()

    var filters: String
        get() = sp.getString("filters", "") ?: ""
        set(v) = sp.edit().putString("filters", v).apply()
}

object Store {
    lateinit var p: Prefs
}

object NexoStyle {
    var accent by mutableStateOf(Color(0xFFFF1744))
    val dim: Color get() = lerp(Color(0xFF0B0B0D), accent, 0.55f)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.p = Prefs(applicationContext)
        NexoStyle.accent = Color(Store.p.accent)
        setContent {
            NexoTheme { NexoApp() }
        }
    }
}

@Composable
fun NexoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexoStyle.accent,
            onPrimary = Color.White,
            background = Color(0xFF0B0B0D),
            onBackground = Color(0xFFEDEDED),
            surface = Color(0xFF16161A),
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = Color(0xFF22222A),
            onSurfaceVariant = Color(0xFFB5B5BD),
            secondaryContainer = NexoStyle.dim,
            onSecondaryContainer = Color.White,
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoApp() {
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
                0 -> ChatsTab()
                1 -> Placeholder("Плагинов пока нет", "Движок плагинов — следующий шаг")
                else -> SettingsRoot()
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
