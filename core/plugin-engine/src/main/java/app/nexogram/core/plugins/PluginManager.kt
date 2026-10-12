package app.nexogram.core.plugins

import java.io.File

interface NexogramPlugin {
    val id: String
    val name: String
    val version: String
    fun onLoad()
    fun onUnload()
}

class PluginManager {

    private val loadedPlugins = mutableMapOf<String, NexogramPlugin>()

    fun loadPluginFromDirectory(pluginDir: File) {
        if (!pluginDir.exists() || !pluginDir.isDirectory) return
        // Логика инициализации и валидации файла плагина
    }

    fun unloadPlugin(pluginId: String) {
        loadedPlugins[pluginId]?.onUnload()
        loadedPlugins.remove(pluginId)
    }

    fun getActivePlugins(): List<NexogramPlugin> {
        return loadedPlugins.values.toList()
    }
}
