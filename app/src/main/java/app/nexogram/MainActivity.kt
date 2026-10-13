package app.nexogram

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.nexogram.core.tdlib.TdClientManager
import app.nexogram.core.security.SecurityManager

class MainActivity : AppCompatActivity() {

    private lateinit var tdClientManager: TdClientManager
    private val securityManager = SecurityManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tdClientManager = TdClientManager(this)
        
        // Защита экрана при старте (если включена)
        securityManager.setScreenProtection(this, enabled = false)
    }
}
// Пример для Jetpack Compose Navigation
composable("profile/{userId}") { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
        ProfileScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable("customization") {
                CustomizationScreen(onBack = { navController.popBackStack() })
}
