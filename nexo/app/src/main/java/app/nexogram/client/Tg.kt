package app.nexogram.client

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.atomic.AtomicInteger
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

enum class Stage { Starting, Phone, Code, Password, Ready, Error }

class ChatItem(val id: Long, val title: String, val unread: Int, val preview: String)

object Tg {
    private val ui = Handler(Looper.getMainLooper())
    private var client: Client? = null

    var stage by mutableStateOf(Stage.Starting)
    var message by mutableStateOf("")
    var busy by mutableStateOf(false)
    var chats by mutableStateOf(listOf<ChatItem>())

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
        if (obj is TdApi.UpdateAuthorizationState) {
            val s = obj.authorizationState
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
                    p.applicationVersion = "0.2"
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

    private fun loadChats() {
        val lc = TdApi.LoadChats()
        lc.chatList = TdApi.ChatListMain()
        lc.limit = 50
        send(lc) { _ ->
            val g = TdApi.GetChats()
            g.chatList = TdApi.ChatListMain()
            g.limit = 50
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
                    arr[i] = ChatItem(r.id, r.title, r.unreadCount, previewOf(r))
                }
                if (left.decrementAndGet() == 0) {
                    val list = arr.filterNotNull()
                    ui.post { chats = list }
                }
            })
        }
    }

    private fun previewOf(c: TdApi.Chat): String {
        val m = c.lastMessage ?: return ""
        val t = m.content
        return if (t is TdApi.MessageText) t.text.text
        else "[" + t.javaClass.simpleName.removePrefix("Message") + "]"
    }
}
