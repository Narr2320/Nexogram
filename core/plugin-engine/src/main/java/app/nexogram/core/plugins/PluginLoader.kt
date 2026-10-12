package app.nexogram.core.plugins

import java.io.File

class PluginLoader {

    fun loadPlugin(file: File): Boolean {
        if (!file.exists() || !file.name.endsWith(".apk") && !file.name.endsWith(".dex")) {
            return false
        }
        // Загрузка и инициализация стороннего плагина
        return true
    }
}
