package app.nexogram.client

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(null)
        val trace = intent.getStringExtra("trace") ?: "нет данных"
        val tv = TextView(this).apply {
            text = "Nexogram вылетел. Сделайте скриншот:\n\n" + trace
            textSize = 11f
            setTextColor(Color.WHITE)
            setTextIsSelectable(true)
            setPadding(24, 64, 24, 64)
        }
        val sv = ScrollView(this)
        sv.setBackgroundColor(Color.BLACK)
        sv.addView(tv)
        setContentView(sv)
    }
}
