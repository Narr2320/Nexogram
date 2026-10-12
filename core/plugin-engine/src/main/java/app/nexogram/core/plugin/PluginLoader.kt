package app.nexogram.core.plugin

import java.io.File

class PluginLoader {
    fun loadPlugin(pluginFile: File): Boolean {
        if (!pluginFile.exists()) return false
        return pluginFile.extension == "apk" || pluginFile.extension == "dex"
    }
}
