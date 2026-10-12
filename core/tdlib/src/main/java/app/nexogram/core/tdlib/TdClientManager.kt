package app.nexogram.core.tdlib

import android.content.Context
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class TdClientManager(private val context: Context) {

    private val activeAccounts = ConcurrentHashMap<Long, String>()

    fun initSession(accountId: Long, apiId: Int, apiHash: String) {
        val sessionDir = File(context.filesDir, "tdlib_session_$accountId")
        if (!sessionDir.exists()) {
            sessionDir.mkdirs()
        }
        activeAccounts[accountId] = sessionDir.absolutePath
    }

    fun getActiveAccounts(): List<Long> {
        return activeAccounts.keys().toList()
    }

    fun closeSession(accountId: Long) {
        activeAccounts.remove(accountId)
    }
}
