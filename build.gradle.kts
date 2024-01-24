import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

plugins {
    id("java")
    //id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates plugin.yml
    id("io.papermc.paperweight.userdev") version "1.5.11" apply false
    alias(libs.plugins.shadowjar)
    alias(libs.plugins.mia.publication)
//    alias(libs.plugins.mia.copyjar)
}

class NMSVersion(val nmsVersion: String, val serverVersion: String)
infix fun String.toNms(that: String): NMSVersion = NMSVersion(this, that)
val SUPPORTED_VERSIONS: List<NMSVersion> = listOf(
    "v1_20_R1" toNms "1.20.1-R0.1-SNAPSHOT",
    "v1_20_R2" toNms "1.20.2-R0.1-SNAPSHOT",
    "v1_20_R3" toNms "1.20.4-R0.1-SNAPSHOT"
)

SUPPORTED_VERSIONS.forEach {
    project(":${it.nmsVersion}") {

        apply(plugin = "java")
        apply(plugin = "io.papermc.paperweight.userdev")

        repositories {
            maven("https://papermc.io/repo/repository/maven-public/") // Paper
            maven("https://repo.mineinabyss.com/releases")
        }

        dependencies {
            compileOnly("io.papermc.paper:paper-api:" + it.serverVersion)
            implementation(project(":core"))
            paperDevBundle(it.serverVersion)
        }
    }
}

val compiled = (project.findProperty("oraxen_compiled")?.toString() ?: "true").toBoolean()
val pluginPath = project.findProperty("oraxen_plugin_path")?.toString()
val devPluginPath = project.findProperty("oraxen_dev_plugin_path")?.toString()
val foliaPluginPath = project.findProperty("oraxen_folia_plugin_path")?.toString()
val spigotPluginPath = project.findProperty("oraxen_spigot_plugin_path")?.toString()
val pluginVersion: String by project
val commandApiVersion = "9.3.0"
val adventureVersion = "4.15.0"
val platformVersion = "4.3.2"
val googleGsonVersion = "2.10.1"
group = "io.th0rgal"
version = pluginVersion

allprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()

        maven("https://papermc.io/repo/repository/maven-public/") // Paper
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
        maven("https://oss.sonatype.org/content/repositories/snapshots") // Because Spigot depends on Bungeecord ChatComponent-API
        maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
        maven("https://libraries.minecraft.net/") // Minecraft repo (commodore)
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceHolderAPI
        maven("https://maven.elmakers.com/repository/") // EffectLib
        maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") // CustomBlockData
        maven("https://repo.triumphteam.dev/snapshots") // actions-code, actions-spigot
        maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } }// MythicMobs
        maven("https://repo.mineinabyss.com/releases") // PlayerAnimator
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // commandAPI snapshots
        maven("https://repo.auxilor.io/repository/maven-public/") // EcoItems
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.oraxen.com/snapshots")
        maven("https://jitpack.io") // JitPack
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/") // MMOItems
        maven("https://repo.codemc.org/repository/maven-public/") // BlockLocker

        mavenLocal()
    }

    dependencies {
        val actionsVersion = "1.0.0-SNAPSHOT"
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
        compileOnly("gs.mclo:java:2.2.1")

        compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-plain:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
        compileOnly("net.kyori:adventure-platform-bukkit:$platformVersion")
        compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0-SNAPSHOT")
        compileOnly("me.clip:placeholderapi:2.11.5")
        compileOnly("com.github.LinsMinecraftStudio.LighterAPI:lightapi-bukkit-common:5.4.0-SNAPSHOT")
        compileOnly("me.gabytm.util:actions-core:$actionsVersion")
        compileOnly("org.springframework:spring-expression:6.0.10")
        compileOnly("io.lumine:Mythic-Dist:5.3.5")
        compileOnly("io.lumine:MythicCrucible:2.0.0-SNAPSHOT")
        compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
        compileOnly("commons-io:commons-io:2.11.0")
        compileOnly("com.google.code.gson:gson:$googleGsonVersion")
        compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.4")
        compileOnly("com.ticxo.modelengine:api:R3.1.9")
        compileOnly(files("../libs/compile/BSP.jar"))
        compileOnly("dev.jorel:commandapi-bukkit-shade:$commandApiVersion")
        compileOnly("io.lumine:MythicLib:1.1.6") // Remove and add deps needed for Polymath
        compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
        compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
        compileOnly("com.willfp:EcoItems:5.36.0")
        compileOnly("com.willfp:eco:6.67.2")
        compileOnly("com.willfp:libreforge:4.49.2")
        compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.1")
        compileOnly("nl.rutgerkok:blocklocker:1.10.4-SNAPSHOT")

        implementation("org.bstats:bstats-bukkit:3.0.0")
        implementation("io.th0rgal:protectionlib:1.4.0")
        implementation("com.github.stefvanschie.inventoryframework:IF:0.10.12")
        implementation("com.jeff-media:custom-block-data:2.2.2")
        implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")
        implementation("com.jeff-media:persistent-data-serializer:1.0")
        implementation("org.jetbrains:annotations:24.1.0") { isTransitive = false }
        implementation("dev.triumphteam:triumph-gui:3.1.7") { exclude("net.kyori") }
        implementation("com.ticxo:PlayerAnimator:R1.2.8") { isChanging = true }

        implementation("me.gabytm.util:actions-spigot:$actionsVersion") { exclude(group = "com.google.guava") }
    }
}

dependencies {
    implementation(project(path = ":core"))
    SUPPORTED_VERSIONS.forEach { implementation(project(path = ":${it.nmsVersion}", configuration = "reobf")) }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filesNotMatching(listOf("**/*.png", "**/*.ogg", "**/models/**", "**/textures/**", "**/font/**.json", "**/plugin.yml")) {
            expand(mapOf(project.version.toString() to pluginVersion))
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.18.2")
    }

    shadowJar {
        SUPPORTED_VERSIONS.forEach { dependsOn(":${it.nmsVersion}:reobfJar") }

        //archiveClassifier = null
        relocate("org.bstats", "io.th0rgal.oraxen.shaded.bstats")
        relocate("dev.triumphteam.gui", "io.th0rgal.oraxen.shaded.triumphteam.gui")
        relocate("com.jeff_media", "io.th0rgal.oraxen.shaded.jeff_media")
        relocate("com.github.stefvanschie.inventoryframework", "io.th0rgal.oraxen.shaded.inventoryframework")
        relocate("me.gabytm.util.actions", "io.th0rgal.oraxen.shaded.actions")
        relocate("org.intellij.lang.annotations", "io.th0rgal.oraxen.shaded.intellij.annotations")
        relocate("org.jetbrains.annotations", "io.th0rgal.oraxen.shaded.jetbrains.annotations")
        relocate("com.udojava.evalex", "io.th0rgal.oraxen.shaded.evalex")
        relocate("com.ticxo.playeranimator", "io.th0rgal.oraxen.shaded.playeranimator")
        relocate("com.tcoded.folialib", "io.th0rgal.oraxen.shaded.folialib")

        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Version" to pluginVersion,
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ").format(Date.from(Instant.now())),
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}",
                    "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
                    "Compiled" to (project.findProperty("oraxen_compiled")?.toString() ?: "true").toBoolean(),
                    "authUsr" to (project.findProperty("oraxenUsername")?.toString() ?: ""),
                    "authPw" to (project.findProperty("oraxenPassword")?.toString() ?: "")
                )
            )
        }
        archiveFileName.set("oraxen-${pluginVersion}.jar")
        archiveClassifier.set("")
    }

    compileJava.get().dependsOn(clean)
//    copyJar.get().dependsOn(jar)
    build.get().dependsOn(shadowJar)
    build.get().dependsOn(publishToMavenLocal)
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "io.th0rgal.oraxen.OraxenPlugin"
    version = pluginVersion
    name = "Oraxen"
    apiVersion = "1.18"
    authors = listOf("th0rgal", "boy0000")
    softDepend = listOf("LightAPI", "PlaceholderAPI", "MythicMobs", "MMOItems", "MythicCrucible", "MythicMobs", "BossShopPro", "CrateReloaded", "ItemBridge", "WorldEdit", "WorldGuard", "Towny", "Factions", "Lands", "PlotSquared", "NBTAPI", "ModelEngine", "CrashClaim", "ViaBackwards", "HuskClaims")
    depend = listOf("ProtocolLib")
    loadBefore = listOf("Realistic_World")
    permissions.create("oraxen.command") {
        description = "Allows the player to use the /oraxen command"
        default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
    }
    libraries = listOf(
        "org.springframework:spring-expression:6.0.6",
        "org.apache.httpcomponents:httpmime:4.5.13",
        "dev.jorel:commandapi-bukkit-shade:$commandApiVersion",
        "org.joml:joml:1.10.5",
        "net.kyori:adventure-text-minimessage:$adventureVersion",
        "net.kyori:adventure-text-serializer-plain:$adventureVersion",
        "net.kyori:adventure-text-serializer-ansi:$adventureVersion",
        "net.kyori:adventure-platform-bukkit:$platformVersion",
        "com.google.code.gson:gson:$googleGsonVersion",
        "gs.mclo:java:2.2.1",
    )
}

if (pluginPath != null) {
    tasks {
        val defaultPath = findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar")
        // Define the main copy task
        val copyJarTask = register<Copy>("copyJar") {
            this.doNotTrackState("Overwrites the plugin jar to allow for easier reloading")
            dependsOn(shadowJar, jar)
            from(defaultPath)
            into(pluginPath)
            doLast { println("Copied to plugin directory $pluginPath") }
        }

        // Create individual copy tasks for each destination
//        val copyToDevPluginPathTask = register<Copy>("copyToDevPluginPath") {
//            dependsOn(shadowJar, jar)
//            from(defaultPath)
//            devPluginPath?.let { into(it) }
//            doLast { println("Copied to plugin directory $devPluginPath") }
//        }
//
//        val copyToFoliaPluginPathTask = register<Copy>("copyToFoliaPluginPath") {
//            dependsOn(shadowJar, jar)
//            from(defaultPath)
//            foliaPluginPath?.let { into(it) }
//            doLast { println("Copied to plugin directory $foliaPluginPath") }
//        }
//
//        val copyToSpigotPluginPathTask = register<Copy>("copyToSpigotPluginPath") {
//            dependsOn(shadowJar, jar)
//            from(defaultPath)
//            spigotPluginPath?.let { into(it) }
//            doLast { println("Copied to plugin directory $spigotPluginPath") }
//        }

        // Make the build task depend on all individual copy tasks
        named<DefaultTask>("build").get().dependsOn(
            copyJarTask/*,
            copyToDevPluginPathTask,
            copyToFoliaPluginPathTask,
            copyToSpigotPluginPathTask*/
        )
    }
}

