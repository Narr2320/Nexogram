package app.nexogram.core.security

import android.view.Window
import android.view.WindowManager

class SecurityManager {
    fun applySecureWindow(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}
