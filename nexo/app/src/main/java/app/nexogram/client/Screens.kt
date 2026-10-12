package app.nexogram.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatsTab() {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { Tg.start(ctx.applicationContext) }
    var open by remember { mutableStateOf<ChatItem?>(null) }
    LaunchedEffect(open) { Extras.chatOpen = open != null }
    DisposableEffect(Unit) { onDispose { Extras.chatOpen = false } }
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
        Stage.Ready -> {
            val c = open
            if (c == null) ChatList { open = it } else ChatScreen(c) { open = null }
        }
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
fun Avatar(title: String, photoId: Int, photoPath: String, size: Dp) {
    LaunchedEffect(photoId) { Tg.ensureAvatar(photoId, photoPath) }
    val bmp = Tg.avatars[photoId]
    Box(
        Modifier.size(size).clip(CircleShape).background(NexoStyle.dim),
        contentAlignment = Alignment.Center,
    ) {
        if (bmp != null) {
            Image(bmp.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(
                title.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.4f).sp,
            )
        }
    }
}

@Composable
fun Avatar(c: ChatItem, size: Dp) = Avatar(c.title, c.photoId, c.photoPath, size)

@Composable
fun SearchPill(value: String, onChange: (String) -> Unit, hint: String) {
    TextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text(hint) },
        shape = RoundedCornerShape(26.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun ChatList(onOpen: (ChatItem) -> Unit) {
    var q by remember { mutableStateOf("") }
    val all = Tg.chats
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Nexogram", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            if (Tg.ghost) {
                Spacer(Modifier.width(8.dp))
                Text("👻", fontSize = 22.sp)
            }
        }
        SearchPill(q, { q = it }, "Поиск чатов")
        if (all.isEmpty()) {
            Placeholder("Загружаю чаты…", "Подождите несколько секунд")
        } else {
            val list = all.filter { (q.isBlank() || it.title.contains(q, true)) && !Tg.hidden(it.title) }
            LazyColumn(Modifier.fillMaxSize()) {
                items(list) { c -> ChatRow(c, onOpen) }
            }
        }
    }
}

@Composable
fun ChatRow(c: ChatItem, onOpen: (ChatItem) -> Unit) {
    val pv = if (Tg.hidden(c.preview)) "скрыто фильтром" else c.preview
    val media = listOf("📷", "🎤", "🎬", "📎", "🎵", "📞", "⏺", "GIF", "Стикер", "[").any { pv.startsWith(it) }
    Row(
        Modifier.fillMaxWidth().clickable { onOpen(c) }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(c, 56.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (c.muted) Text("  🔇", fontSize = 12.sp)
            }
            if (pv.isNotEmpty()) {
                Text(
                    pv.replace("\n", " "),
                    color = if (media) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                c.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            if (c.unread > 0) {
                Text(
                    c.unread.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(
                            if (c.muted) Color(0xFF4A5560) else MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            } else if (c.pinned) {
                Text("📌", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ContactsTab() {
    LaunchedEffect(Tg.stage) { if (Tg.stage == Stage.Ready) Extras.loadContacts() }
    var q by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Text(
            "Контакты",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp),
        )
        SearchPill(q, { q = it }, "Поиск контактов")
        if (Tg.stage != Stage.Ready) {
            Placeholder("Сначала войдите", "Откройте вкладку «Чаты»")
        } else if (Extras.contacts.isEmpty()) {
            Placeholder("Загружаю контакты…", "")
        } else {
            val list = Extras.contacts.filter { q.isBlank() || it.name.contains(q, true) }
            LazyColumn(Modifier.fillMaxSize()) {
                items(list) { c -> ContactRow(c) }
            }
        }
    }
}

@Composable
fun ContactRow(c: ContactItem) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(c.name, c.photoId, c.photoPath, 52.dp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                c.status,
                fontSize = 14.sp,
                color = if (c.online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun InfoLine(value: String, label: String) {
    Column {
        Text(value, fontSize = 17.sp)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ProfileTab() {
    LaunchedEffect(Tg.stage) { if (Tg.stage == Stage.Ready) Extras.loadMe() }
    val me = Extras.me
    if (me == null) {
        Placeholder("Профиль", "Загружается…")
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        Avatar(me.name, me.photoId, me.photoPath, 120.dp)
        Spacer(Modifier.height(12.dp))
        Text(me.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("в сети", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoLine("+" + me.phone, "Телефон")
                if (me.username.isNotEmpty()) InfoLine("@" + me.username, "Имя пользователя")
            }
        }
    }
}

@Composable
fun ChatScreen(c: ChatItem, onBack: () -> Unit) {
    BackHandler { onBack() }
    LaunchedEffect(c.id) { Tg.openChat(c.id) }
    DisposableEffect(c.id) { onDispose { Tg.closeChat(c.id) } }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(Tg.editing) {
        val e = Tg.editing
        if (e != null) text = e.text
    }
    val visible = Tg.messages.filter { !Tg.hidden(it.text) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            }
            Avatar(c, 40.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                c.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (Tg.ghost) Text("👻", fontSize = 20.sp)
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true,
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(visible) { m -> Bubble(m, c.isGroup) }
        }
        val rp = Tg.replyingTo
        val ed = Tg.editing
        if (rp != null || ed != null) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val label = if (ed != null) "✏ Редактирование" else "↩ " + (rp?.text ?: "").ifEmpty { "медиа" }.take(40)
                Text(
                    label,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "✕",
                    modifier = Modifier.clickable {
                        Tg.replyingTo = null
                        Tg.editing = null
                        text = ""
                    }.padding(8.dp),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    if (it.isNotEmpty()) Tg.typing()
                },
                placeholder = { Text("Сообщение") },
                maxLines = 4,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    Tg.sendText(text.trim())
                    text = ""
                }
            }) { Text("➤") }
        }
    }
}

private fun fmtDur(s: Int): String = (s / 60).toString() + ":" + (s % 60).toString().padStart(2, '0')

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Bubble(m: Msg, group: Boolean) {
    var menu by remember { mutableStateOf(false) }
    val clip = LocalClipboardManager.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (m.out) Arrangement.End else Arrangement.Start,
    ) {
        Box {
            Column(
                Modifier
                    .widthIn(max = 300.dp)
                    .background(
                        if (m.out) NexoStyle.dim else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(14.dp),
                    )
                    .combinedClickable(onClick = {}, onLongClick = { menu = true })
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (group && !m.out && m.senderId != 0L) {
                    LaunchedEffect(m.senderId) { Tg.ensureName(m.senderId) }
                    val n = Tg.names[m.senderId]
                    if (n != null) {
                        Text(
                            n,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (m.reply) {
                    Text("↩ ответ", color = Color(0xAAFFFFFF), style = MaterialTheme.typography.bodySmall)
                }
                if (m.imgId != 0) {
                    LaunchedEffect(m.imgId) { Tg.ensureImage(m.imgId, m.imgPath) }
                    val b = Tg.images[m.imgId]
                    val side = if (m.sticker) 150.dp else 300.dp
                    if (b != null) {
                        Image(
                            b.asImageBitmap(),
                            null,
                            Modifier.widthIn(max = side).heightIn(max = if (m.sticker) 150.dp else 320.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Box(Modifier.size(200.dp, 140.dp).background(Color(0xFF22222A), RoundedCornerShape(10.dp)))
                    }
                    Spacer(Modifier.height(4.dp))
                }
                if (m.voiceId != 0) {
                    val playing = Tg.playingId == m.voiceId
                    Row(
                        Modifier.clickable { Tg.toggleVoice(m.voiceId, m.voicePath) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (playing) "⏸" else "▶", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Голосовое · " + fmtDur(m.voiceDur))
                    }
                }
                if (m.text.isNotEmpty()) Text(m.text)
                Row(Modifier.align(Alignment.End)) {
                    if (m.deleted) {
                        Text("🗑 удалено  ", color = Color(0xFFFF8A80), style = MaterialTheme.typography.bodySmall)
                    }
                    if (m.edited) {
                        Text("изм.  ", color = Color(0xAAFFFFFF), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(m.time, style = MaterialTheme.typography.bodySmall, color = Color(0xAAFFFFFF))
                }
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Ответить") },
                    onClick = {
                        menu = false
                        Tg.editing = null
                        Tg.replyingTo = m
                    },
                )
                if (m.text.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Копировать") },
                        onClick = {
                            menu = false
                            clip.setText(AnnotatedString(m.text))
                        },
                    )
                }
                if (m.out && m.text.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Изменить") },
                        onClick = {
                            menu = false
                            Tg.replyingTo = null
                            Tg.editing = m
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        menu = false
                        Tg.deleteMessage(m)
                    },
                )
            }
        }
    }
}
