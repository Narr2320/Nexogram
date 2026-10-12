package app.nexogram.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import org.drinkless.tdlib.TdApi

class ContactItem(
    val id: Long,
    val name: String,
    val status: String,
    val online: Boolean,
    val photoId: Int,
    val photoPath: String,
    val sortKey: Long,
)

class Me(val name: String, val phone: String, val username: String, val photoId: Int, val photoPath: String)

class SessionItem(val title: String, val sub: String, val current: Boolean)

object Extras {
    var contacts by mutableStateOf(listOf<ContactItem>())
    var me by mutableStateOf<Me?>(null)
    var sessions by mutableStateOf(listOf<SessionItem>())
    var chatOpen by mutableStateOf(false)

    private var contactsLoaded = false
    private val tFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dFmt = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val fFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val kFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    fun loadContacts() {
        if (contactsLoaded) return
        contactsLoaded = true
        Tg.send(TdApi.GetContacts()) { r ->
            if (r is TdApi.Users) fetchUsers(r.userIds)
        }
    }

    private fun fetchUsers(ids: LongArray) {
        if (ids.isEmpty()) {
            Tg.ui.post { contacts = emptyList() }
            return
        }
        val arr = arrayOfNulls<ContactItem>(ids.size)
        val left = AtomicInteger(ids.size)
        ids.forEachIndexed { i, id ->
            val q = TdApi.GetUser()
            q.userId = id
            Tg.send(q) { r ->
                if (r is TdApi.User) arr[i] = toContact(r)
                if (left.decrementAndGet() == 0) {
                    val list = arr.filterNotNull().sortedByDescending { it.sortKey }
                    Tg.ui.post { contacts = list }
                }
            }
        }
    }

    private fun stamp(sec: Int): String {
        val d = Date(sec * 1000L)
        return if (kFmt.format(d) == kFmt.format(Date())) "в " + tFmt.format(d) else dFmt.format(d)
    }

    private fun toContact(u: TdApi.User): ContactItem {
        val now = System.currentTimeMillis() / 1000
        val st = u.status
        var text = "был(а) давно"
        var online = false
        var key = 0L
        when (st) {
            is TdApi.UserStatusOnline -> {
                text = "в сети"
                online = true
                key = Long.MAX_VALUE
            }
            is TdApi.UserStatusOffline -> {
                text = "был(а) " + stamp(st.wasOnline)
                key = st.wasOnline.toLong()
            }
            is TdApi.UserStatusRecently -> {
                text = "был(а) недавно"
                key = now - 3600
            }
            is TdApi.UserStatusLastWeek -> {
                text = "был(а) на этой неделе"
                key = now - 259200
            }
            is TdApi.UserStatusLastMonth -> {
                text = "был(а) в этом месяце"
                key = now - 1296000
            }
            else -> {}
        }
        val ph: TdApi.ProfilePhoto? = u.profilePhoto
        val small: TdApi.File? = ph?.small
        val path = if (small != null && small.local.isDownloadingCompleted) small.local.path else ""
        return ContactItem(
            u.id,
            (u.firstName + " " + u.lastName).trim(),
            text,
            online,
            small?.id ?: 0,
            path,
            key,
        )
    }

    fun loadMe() {
        if (me != null) return
        Tg.send(TdApi.GetMe()) { r ->
            if (r is TdApi.User) {
                val ph: TdApi.ProfilePhoto? = r.profilePhoto
                val big: TdApi.File? = ph?.big
                val uname = r.usernames?.activeUsernames?.firstOrNull() ?: ""
                val path = if (big != null && big.local.isDownloadingCompleted) big.local.path else ""
                val m = Me((r.firstName + " " + r.lastName).trim(), r.phoneNumber, uname, big?.id ?: 0, path)
                Tg.ui.post { me = m }
            }
        }
    }

    fun loadSessions() {
        Tg.send(TdApi.GetActiveSessions()) { r ->
            if (r is TdApi.Sessions) {
                val list = r.sessions.map { s ->
                    val info = listOf(s.deviceModel, s.platform, s.country).filter { it.isNotEmpty() }.joinToString(" · ")
                    SessionItem(
                        (s.applicationName + " " + s.applicationVersion).trim(),
                        info + " · " + fFmt.format(Date(s.lastActiveDate * 1000L)),
                        s.isCurrent,
                    )
                }
                Tg.ui.post { sessions = list }
            }
        }
    }
}
