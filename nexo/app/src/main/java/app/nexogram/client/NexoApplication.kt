package app.nexogram.client

import android.app.Application
import android.content.Intent
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class NexoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val intent = Intent(this, CrashActivity::class.java)
                    .putExtra("trace", sw.toString().take(4000))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } catch (t: Throwable) {
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
