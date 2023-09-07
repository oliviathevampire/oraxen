
plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

dependencies {
    val actionsVersion = "1.0.0-SNAPSHOT"
    implementation("dev.triumphteam:triumph-gui:3.1.5")
    implementation("com.github.oraxen:protectionlib:1.3.2")
    implementation("com.jeff_media:CustomBlockData:2.2.0")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")
    implementation("com.jeff_media:PersistentDataSerializer:1.0-SNAPSHOT")
    implementation("gs.mclo:java:2.2.1")
    implementation("org.jetbrains:annotations:24.0.1") { isTransitive = false }

    implementation("me.gabytm.util:actions-spigot:$actionsVersion") { exclude(group = "com.google.guava") }
    implementation("com.tcoded:FoliaLib:0.2.5")

    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}