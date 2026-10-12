package app.nexogram.client

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background

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

    var navy: Boolean
        get() = sp.getBoolean("navy", false)
        set(v) = sp.edit().putBoolean("navy", v).apply()

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
    var navy by mutableStateOf(false)
    val bg: Color get() = if (navy) Color(0xFF17212B) else Color(0xFF0B0B0D)
    val surface: Color get() = if (navy) Color(0xFF1F2C3A) else Color(0xFF16161A)
    val dim: Color get() = lerp(bg, accent, 0.55f)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.p = Prefs(applicationContext)
        NexoStyle.accent = Color(Store.p.accent)
        NexoStyle.navy = Store.p.navy
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
            background = NexoStyle.bg,
            onBackground = Color(0xFFEDEDED),
            surface = NexoStyle.surface,
            onSurface = Color(0xFFEDEDED),
            surfaceVariant = if (NexoStyle.navy) Color(0xFF2A3A4B) else Color(0xFF22222A),
            onSurfaceVariant = Color(0xFFB5B5BD),
            secondaryContainer = NexoStyle.dim,
            onSecondaryContainer = Color.White,
        ),
        content = content,
    )
}

@Composable
fun NexoApp() {
    var tab by remember { mutableIntStateOf(0) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    0 -> ChatsTab()
                    1 -> ContactsTab()
                    2 -> SettingsRoot()
                    else -> ProfileTab()
                }
            }
            if (!Extras.chatOpen) FloatingBar(tab) { tab = it }
        }
    }
}

@Composable
fun FloatingBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf(
        Icons.Filled.Email to "Чаты",
        Icons.Filled.Person to "Контакты",
        Icons.Filled.Settings to "Настройки",
        Icons.Filled.AccountCircle to "Профиль",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEachIndexed { i, it ->
            val sel = tab == i
            val tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (sel) NexoStyle.dim else Color.Transparent)
                    .clickable { onTab(i) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(it.first, contentDescription = it.second, tint = tint)
                Text(it.second, fontSize = 11.sp, color = tint, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
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
