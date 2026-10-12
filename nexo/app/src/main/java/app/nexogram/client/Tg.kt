package app.nexogram.client

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

enum class Stage { Starting, Phone, Code, Password, Ready, Error }

class ChatItem(
    val id: Long,
    val title: String,
    val unread: Int,
    val preview: String,
    val photoId: Int,
    val photoPath: String,
    val time: String,
    val muted: Boolean,
    val pinned: Boolean,
    val isGroup: Boolean,
)

data class Msg(
    val id: Long,
    val out: Boolean,
    val text: String,
    val time: String,
    val senderId: Long,
    val imgId: Int = 0,
    val imgPath: String = "",
    val sticker: Boolean = false,
    val voiceId: Int = 0,
    val voicePath: String = "",
    val voiceDur: Int = 0,
    val reply: Boolean = false,
    val edited: Boolean = false,
    val deleted: Boolean = false,
)

object Tg {
    private val ui = Handler(Looper.getMainLooper())
    private var client: Client? = null
    private var current = 0L
    private var lastTyping = 0L
    private var player: MediaPlayer? = null
    private val pending = HashSet<Int>()
    private val namePending = HashSet<Long>()
    private val selfDel: MutableSet<Long> = java.util.Collections.synchronizedSet(HashSet<Long>())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dayFmt = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val keyFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    var stage by mutableStateOf(Stage.Starting)
    var message by mutableStateOf("")
    var busy by mutableStateOf(false)
    var chats by mutableStateOf(listOf<ChatItem>())
    var messages by mutableStateOf(listOf<Msg>())
    var replyingTo by mutableStateOf<Msg?>(null)
    var editing by mutableStateOf<Msg?>(null)
    var playingId by mutableStateOf(0)
    val avatars = mutableStateMapOf<Int, Bitmap>()
    val images = mutableStateMapOf<Int, Bitmap>()
    val names = mutableStateMapOf<Long, String>()

    val ghost: Boolean get() = Store.p.ghost
    private val hideTyping: Boolean get() = Store.p.hideTyping

    private val refresh = Runnable { loadChats() }

    fun hidden(text: String): Boolean {
        val f = Store.p.filters
        if (f.isBlank() || text.isEmpty()) return false
        val low = text.lowercase()
        return f.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.any { low.contains(it) }
    }

    fun start(ctx: Context) {
        if (client != null) return
        if (BuildConfig.TG_API_ID == 0 || BuildConfig.TG_API_HASH.isEmpty()) {
            stage = Stage.Error
            message = "API_ID / API_HASH не переданы в сборку (секреты GitHub)"
            return
        }
        try {
            val dir = ctx.filesDir.absolutePath + "/td"
            client = Client.create(
                Client.ResultHandler { obj -> onUpdate(obj, dir) },
                null,
                null,
            )
        } catch (t: Throwable) {
            stage = Stage.Error
            message = "TDLib: " + t.toString()
        }
    }

    fun logout() {
        send(TdApi.LogOut())
        stage = Stage.Error
        message = "Вы вышли. Закройте и снова откройте приложение."
    }

    private fun set(s: Stage, msg: String = "") {
        ui.post {
            stage = s
            message = msg
            busy = false
        }
    }

    private fun send(f: TdApi.Function<*>, cb: ((TdApi.Object) -> Unit)? = null) {
        client?.send(f, Client.ResultHandler { r ->
            if (r is TdApi.Error) {
                val m = r.message
                ui.post {
                    message = m
                    busy = false
                }
            } else {
                ui.post { busy = false }
            }
            cb?.invoke(r)
        })
    }

    private fun onUpdate(obj: TdApi.Object, dir: String) {
        when (obj) {
            is TdApi.UpdateAuthorizationState -> onAuth(obj.authorizationState, dir)
            is TdApi.UpdateNewMessage -> onNewMessage(obj.message)
            is TdApi.UpdateMessageSendSucceeded -> {
                val old = obj.oldMessageId
                val nm = toMsg(obj.message)
                ui.post { messages = messages.map { if (it.id == old) nm else it } }
            }
            is TdApi.UpdateMessageContent -> {
                if (obj.chatId == current) {
                    val mid = obj.messageId
                    val t = bodyOf(obj.newContent)
                    ui.post { messages = messages.map { if (it.id == mid) it.copy(text = t, edited = true) else it } }
                }
            }
            is TdApi.UpdateDeleteMessages -> onDeleted(obj.chatId, obj.messageIds, obj.isPermanent)
            is TdApi.UpdateChatReadInbox -> if (stage == Stage.Ready) scheduleRefresh()
            is TdApi.UpdateChatLastMessage -> if (stage == Stage.Ready) scheduleRefresh()
            else -> {}
        }
    }

    private fun onAuth(s: TdApi.AuthorizationState, dir: String) {
        when (s) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val p = TdApi.SetTdlibParameters()
                p.databaseDirectory = dir
                p.filesDirectory = dir
                p.databaseEncryptionKey = ByteArray(0)
                p.useFileDatabase = true
                p.useChatInfoDatabase = true
                p.useMessageDatabase = true
                p.useSecretChats = false
                p.apiId = BuildConfig.TG_API_ID
                p.apiHash = BuildConfig.TG_API_HASH
                p.systemLanguageCode = "ru"
                p.deviceModel = "Android"
                p.systemVersion = "Android"
                p.applicationVersion = "0.5"
                send(p)
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> set(Stage.Phone)
            is TdApi.AuthorizationStateWaitCode -> set(Stage.Code)
            is TdApi.AuthorizationStateWaitPassword -> set(Stage.Password)
            is TdApi.AuthorizationStateReady -> {
                set(Stage.Ready)
                loadChats()
            }
            else -> {
                val name = s.javaClass.simpleName
                ui.post { message = "Состояние: " + name }
            }
        }
    }

    fun submitPhone(phone: String) {
        busy = true
        message = ""
        val q = TdApi.SetAuthenticationPhoneNumber()
        q.phoneNumber = phone
        send(q)
    }

    fun submitCode(code: String) {
        busy = true
        message = ""
        val q = TdApi.CheckAuthenticationCode()
        q.code = code
        send(q)
    }

    fun submitPassword(pw: String) {
        busy = true
        message = ""
        val q = TdApi.CheckAuthenticationPassword()
        q.password = pw
        send(q)
    }

    private fun scheduleRefresh() {
        ui.removeCallbacks(refresh)
        ui.postDelayed(refresh, 700)
    }

    private fun loadChats() {
        val lc = TdApi.LoadChats()
        lc.chatList = TdApi.ChatListMain()
        lc.limit = 60
        send(lc) { _ ->
            val g = TdApi.GetChats()
            g.chatList = TdApi.ChatListMain()
            g.limit = 60
            send(g) { r ->
                if (r is TdApi.Chats) fetchChats(r.chatIds)
            }
        }
    }

    private fun fetchChats(ids: LongArray) {
        if (ids.isEmpty()) {
            ui.post { chats = emptyList() }
            return
        }
        val arr = arrayOfNulls<ChatItem>(ids.size)
        val left = AtomicInteger(ids.size)
        ids.forEachIndexed { i, id ->
            val q = TdApi.GetChat()
            q.chatId = id
            client?.send(q, Client.ResultHandler { r ->
                if (r is TdApi.Chat) {
                    val ph: TdApi.ChatPhotoInfo? = r.photo
                    val small: TdApi.File? = ph?.small
                    val pid = small?.id ?: 0
                    val ppath = if (small != null && small.local.isDownloadingCompleted) small.local.path else ""
                    val lm: TdApi.Message? = r.lastMessage
                    val tm = if (lm != null) fmtDate(lm.date) else ""
                    val muted = r.notificationSettings.muteFor > 0
                    val pinned = r.positions.firstOrNull { it.list is TdApi.ChatListMain }?.isPinned ?: false
                    val t = r.type
                    val group = t is TdApi.ChatTypeBasicGroup || (t is TdApi.ChatTypeSupergroup && !t.isChannel)
                    arr[i] = ChatItem(r.id, r.title, r.unreadCount, previewOf(r), pid, ppath, tm, muted, pinned, group)
                }
                if (left.decrementAndGet() == 0) {
                    val list = arr.filterNotNull()
                    ui.post { chats = list }
                }
            })
        }
    }

    private fun fmtDate(sec: Int): String {
        val d = Date(sec * 1000L)
        return if (keyFmt.format(d) == keyFmt.format(Date())) timeFmt.format(d) else dayFmt.format(d)
    }

    // ---------- картинки и аватарки ----------
    fun ensureAvatar(fileId: Int, path: String) = ensureBitmap(fileId, path, avatars)

    fun ensureImage(fileId: Int, path: String) = ensureBitmap(fileId, path, images)

    private fun ensureBitmap(fileId: Int, path: String, target: MutableMap<Int, Bitmap>) {
        if (fileId == 0 || target.containsKey(fileId)) return
        if (path.isNotEmpty()) {
            decodeInto(fileId, path, target)
            return
        }
        synchronized(pending) { if (!pending.add(fileId)) return }
        val q = TdApi.DownloadFile()
        q.fileId = fileId
        q.priority = 1
        q.synchronous = true
        send(q) { r ->
            if (r is TdApi.File && r.local.isDownloadingCompleted) decodeInto(fileId, r.local.path, target)
        }
    }

    private fun decodeInto(fileId: Int, path: String, target: MutableMap<Int, Bitmap>) {
        val b = BitmapFactory.decodeFile(path) ?: return
        ui.post { target[fileId] = b }
    }

    fun ensureName(uid: Long) {
        if (uid == 0L || names.containsKey(uid)) return
        synchronized(namePending) { if (!namePending.add(uid)) return }
        val q = TdApi.GetUser()
        q.userId = uid
        send(q) { r ->
            if (r is TdApi.User) {
                val n = (r.firstName + " " + r.lastName).trim()
                ui.post { names[uid] = n }
            }
        }
    }

    // ---------- голосовые ----------
    fun toggleVoice(fileId: Int, path: String) {
        if (playingId == fileId) {
            stopVoice()
            return
        }
        stopVoice()
        if (path.isNotEmpty()) {
            startVoice(fileId, path)
            return
        }
        val q = TdApi.DownloadFile()
        q.fileId = fileId
        q.priority = 1
        q.synchronous = true
        send(q) { r ->
            if (r is TdApi.File && r.local.isDownloadingCompleted) {
                val p = r.local.path
                ui.post { startVoice(fileId, p) }
            }
        }
    }

    private fun startVoice(id: Int, path: String) {
        try {
            val mp = MediaPlayer()
            mp.setDataSource(path)
            mp.setOnCompletionListener { stopVoice() }
            mp.prepare()
            mp.start()
            player = mp
            playingId = id
        } catch (t: Throwable) {
            message = "Не удалось воспроизвести"
        }
    }

    fun stopVoice() {
        try {
            player?.release()
        } catch (t: Throwable) {
        }
        player = null
        playingId = 0
    }

    // ---------- сообщения ----------
    fun openChat(id: Long) {
        current = id
        messages = emptyList()
        replyingTo = null
        editing = null
        val o = TdApi.OpenChat()
        o.chatId = id
        send(o)
        loadHistory(id, 0)
    }

    fun closeChat(id: Long) {
        if (current == id) current = 0L
        stopVoice()
        val c = TdApi.CloseChat()
        c.chatId = id
        send(c)
    }

    private fun loadHistory(id: Long, tries: Int) {
        val q = TdApi.GetChatHistory()
        q.chatId = id
        q.fromMessageId = 0L
        q.offset = 0
        q.limit = 40
        q.onlyLocal = false
        send(q) { r ->
            if (r is TdApi.Messages) {
                val list = r.messages.map { toMsg(it) }
                ui.post { if (current == id) messages = list }
                if (list.size < 5 && tries < 2) {
                    ui.postDelayed({ if (current == id) loadHistory(id, tries + 1) }, 700)
                }
                val ids = r.messages.filter { !it.isOutgoing }.map { it.id }.toLongArray()
                markRead(id, ids)
            }
        }
    }

    private fun onNewMessage(m: TdApi.Message) {
        if (stage == Stage.Ready) scheduleRefresh()
        val cid = m.chatId
        if (cid == current) {
            val item = toMsg(m)
            ui.post {
                if (current == cid && messages.none { it.id == item.id }) messages = listOf(item) + messages
            }
            if (!m.isOutgoing) markRead(cid, longArrayOf(m.id))
        }
    }

    // ШПИОН: при включённом режиме удалённые собеседником сообщения остаются в чате
    private fun onDeleted(chatId: Long, ids: LongArray, permanent: Boolean) {
        if (!permanent) return
        scheduleRefresh()
        if (chatId != current) return
        val set = ids.toSet()
        val keep = Store.p.spy
        ui.post {
            messages = messages.mapNotNull {
                if (it.id !in set) it
                else if (keep && !selfDel.contains(it.id)) it.copy(deleted = true)
                else null
            }
        }
    }

    // РЕЖИМ ПРИЗРАКА: при включённом режиме «прочитано» не отправляется вообще
    private fun markRead(chatId: Long, ids: LongArray) {
        if (ghost || ids.isEmpty()) return
        val v = TdApi.ViewMessages()
        v.chatId = chatId
        v.messageIds = ids
        v.source = TdApi.MessageSourceChatHistory()
        v.forceRead = true
        send(v)
    }

    fun sendText(text: String) {
        val id = current
        if (id == 0L) return
        val c = TdApi.InputMessageText()
        c.text = TdApi.FormattedText(text, emptyArray<TdApi.TextEntity>())
        val ed = editing
        if (ed != null) {
            val e = TdApi.EditMessageText()
            e.chatId = id
            e.messageId = ed.id
            e.inputMessageContent = c
            send(e)
            editing = null
            return
        }
        val q = TdApi.SendMessage()
        q.chatId = id
        q.inputMessageContent = c
        val rt = replyingTo
        if (rt != null) {
            val r = TdApi.InputMessageReplyToMessage()
            r.messageId = rt.id
            q.replyTo = r
        }
        send(q)
        replyingTo = null
    }

    fun deleteMessage(m: Msg) {
        val id = current
        if (id == 0L) return
        selfDel.add(m.id)
        val q = TdApi.DeleteMessages()
        q.chatId = id
        q.messageIds = longArrayOf(m.id)
        q.revoke = m.out
        send(q)
    }

    fun typing() {
        if (hideTyping || current == 0L) return
        val now = System.currentTimeMillis()
        if (now - lastTyping < 4000) return
        lastTyping = now
        val q = TdApi.SendChatAction()
        q.chatId = current
        q.action = TdApi.ChatActionTyping()
        send(q)
    }

    private fun toMsg(m: TdApi.Message): Msg {
        val c = m.content
        var imgId = 0
        var imgPath = ""
        var isSticker = false
        var vId = 0
        var vPath = ""
        var vDur = 0
        when (c) {
            is TdApi.MessagePhoto -> {
                val sizes = c.photo.sizes
                val pick = sizes.filter { it.width <= 800 }.maxByOrNull { it.width } ?: sizes.lastOrNull()
                if (pick != null) {
                    imgId = pick.photo.id
                    if (pick.photo.local.isDownloadingCompleted) imgPath = pick.photo.local.path
                }
            }
            is TdApi.MessageSticker -> {
                val st = c.sticker
                if (st.format is TdApi.StickerFormatWebp) {
                    isSticker = true
                    imgId = st.sticker.id
                    if (st.sticker.local.isDownloadingCompleted) imgPath = st.sticker.local.path
                }
            }
            is TdApi.MessageVoiceNote -> {
                val f = c.voiceNote.voice
                vId = f.id
                vDur = c.voiceNote.duration
                if (f.local.isDownloadingCompleted) vPath = f.local.path
            }
            else -> {}
        }
        val s = m.senderId
        val uid = if (s is TdApi.MessageSenderUser) s.userId else 0L
        return Msg(
            id = m.id,
            out = m.isOutgoing,
            text = bodyOf(c),
            time = timeFmt.format(Date(m.date * 1000L)),
            senderId = uid,
            imgId = imgId,
            imgPath = imgPath,
            sticker = isSticker,
            voiceId = vId,
            voicePath = vPath,
            voiceDur = vDur,
            reply = m.replyTo is TdApi.MessageReplyToMessage,
            edited = m.editDate > 0,
        )
    }

    private fun cap(s: String): String = if (s.isEmpty()) "" else "\n" + s

    // текст внутри пузыря
    private fun bodyOf(t: TdApi.MessageContent): String = when (t) {
        is TdApi.MessageText -> t.text.text
        is TdApi.MessagePhoto -> t.caption.text
        is TdApi.MessageVideo -> "🎬 Видео" + cap(t.caption.text)
        is TdApi.MessageDocument -> "📎 Документ" + cap(t.caption.text)
        is TdApi.MessageVoiceNote -> ""
        is TdApi.MessageVideoNote -> "⏺ Кружок"
        is TdApi.MessageSticker -> if (t.sticker.format is TdApi.StickerFormatWebp) "" else "Стикер (анимированный)"
        is TdApi.MessageAnimation -> "GIF"
        is TdApi.MessageAudio -> "🎵 Аудио"
        is TdApi.MessageCall -> "📞 Звонок"
        else -> "[" + t.javaClass.simpleName.removePrefix("Message") + "]"
    }

    // короткий текст для списка чатов
    private fun contentText(t: TdApi.MessageContent): String = when (t) {
        is TdApi.MessageText -> t.text.text
        is TdApi.MessagePhoto -> "📷 Фото" + cap(t.caption.text)
        is TdApi.MessageVoiceNote -> "🎤 Голосовое"
        is TdApi.MessageSticker -> "Стикер"
        else -> bodyOf(t)
    }

    private fun previewOf(c: TdApi.Chat): String {
        val m = c.lastMessage ?: return ""
        return contentText(m.content)
    }
}
