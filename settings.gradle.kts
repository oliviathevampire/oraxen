rootProject.name = "oraxen"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(
        "core",
        "v1_20_R1",
)
