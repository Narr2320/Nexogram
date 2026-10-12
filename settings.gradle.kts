pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexogram"
include(":app")
include(":core:tdlib")
include(":core:database")
include(":core:security")
include(":core:plugin-engine")
include(":feature:chat-list")
include(":feature:chat-screen")
include(":feature:plugins")
include(":feature:settings")
