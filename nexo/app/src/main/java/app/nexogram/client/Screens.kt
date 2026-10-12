package app.nexogram.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChatsTab() {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { Tg.start(ctx.applicationContext) }
    when (Tg.stage) {
        Stage.Starting -> Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(Tg.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Stage.Phone -> LoginForm("Ваш номер телефона", "+374XXXXXXXX", KeyboardType.Phone, false) { Tg.submitPhone(it) }
        Stage.Code -> LoginForm("Код из Telegram", "12345", KeyboardType.Number, false) { Tg.submitCode(it) }
        Stage.Password -> LoginForm("Облачный пароль", "Пароль", KeyboardType.Password, true) { Tg.submitPassword(it) }
        Stage.Ready -> ChatList()
        Stage.Error -> Placeholder("Ошибка", Tg.message)
    }
}

@Composable
fun LoginForm(title: String, hint: String, kind: KeyboardType, secret: Boolean, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            placeholder = { Text(hint) },
            keyboardOptions = KeyboardOptions(keyboardType = kind),
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
        )
        if (Tg.message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(Tg.message, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSubmit(text.trim()) },
            enabled = !Tg.busy && text.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (Tg.busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("Далее")
        }
    }
}

@Composable
fun ChatList() {
    val list = Tg.chats
    if (list.isEmpty()) {
        Placeholder("Загружаю чаты…", "Подождите несколько секунд")
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(list) { c -> ChatRow(c) }
    }
}

@Composable
fun ChatRow(c: ChatItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (c.preview.isNotEmpty()) {
                    Text(
                        c.preview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (c.unread > 0) {
                Spacer(Modifier.width(8.dp))
                Text(
                    c.unread.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}
