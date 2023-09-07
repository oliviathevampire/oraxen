import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates plugin.yml
    id("io.papermc.paperweight.userdev") version "1.5.5" apply false
}


class NMSVersion(val nmsVersion: String, val serverVersion: String)
infix fun String.toNms(that: String): NMSVersion = NMSVersion(this, that)
val SUPPORTED_VERSIONS: List<NMSVersion> = listOf(
        "v1_20_R1" toNms "1.20.1-R0.1-SNAPSHOT"
)

SUPPORTED_VERSIONS.forEach {
    project(":${it.nmsVersion}") {

        apply(plugin = "java")
        apply(plugin = "io.papermc.paperweight.userdev")

        repositories {
            maven("https://papermc.io/repo/repository/maven-public/") // Paper
        }

        dependencies {
            implementation(project(":core"))
            paperDevBundle(it.serverVersion)
        }
    }
}

val compiled = (project.findProperty("oraxen_compiled")?.toString() ?: "true").toBoolean()
val pluginPath = project.findProperty("oraxen_folia_plugin_path")
val pluginVersion: String by project
val commandApiVersion = "9.1.0"
val adventureVersion = "4.14.0"
val platformVersion = "4.3.0"
group = "io.th0rgal"
version = pluginVersion

fun property(name: String) = properties[name] as String

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
        maven("https://oss.sonatype.org/content/repositories/snapshots") // Because Spigot depends on Bungeecord ChatComponent-API
        maven("https://jitpack.io") // JitPack
        maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
        maven("https://libraries.minecraft.net/") // Minecraft repo (commodore)
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceHolderAPI
        maven("https://maven.elmakers.com/repository/") // EffectLib
        maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/") // CustomBlockData
        maven("https://repo.triumphteam.dev/snapshots") // actions-code, actions-spigot
        maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } }// MythicMobs
        maven("https://repo.mineinabyss.com/releases") // PlayerAnimator
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // commandAPI snapshots
        maven("https://nexuslite.gcnt.net/repos/other/") // FoliaSchedulers
        maven("https://maven.enginehub.org/repo/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://nexuslite.gcnt.net/repos/other/") // FoliaLib
    }

    dependencies {
        val actionsVersion = "1.0.0-SNAPSHOT"

        compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT") { exclude("org.bukkit")}
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT") { exclude("net.kyori") }
        compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-plain:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
        compileOnly("net.kyori:adventure-platform-bukkit:$platformVersion")
        compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
        compileOnly("me.clip:placeholderapi:2.11.3")
        compileOnly("com.github.BeYkeRYkt:LightAPI:5.3.0-Bukkit")
        compileOnly("me.gabytm.util:actions-core:$actionsVersion")
        compileOnly("io.lumine:Mythic-Dist:5.4.0-SNAPSHOT")
        compileOnly("io.lumine:MythicCrucible:1.7.0-SNAPSHOT")
        compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0")
        compileOnly("commons-io:commons-io:2.11.0")
        compileOnly("com.ticxo.modelengine:api:R3.1.5")
        compileOnly("dev.jorel:commandapi-bukkit-shade:${property("commandApiVersion")}")
        compileOnly("org.apache.httpcomponents:httpclient:${property("httpVersion")}")
        compileOnly("io.javalin:javalin:${property("javalinVersion")}") // Javalin werbserver for LocalHost
        compileOnly("javax.xml.bind:jaxb-api:${property("javaxVersion")}") // Javalin werbserver for LocalHost
        compileOnly("org.springframework:spring-expression:${property("springVersion")}")

        compileOnly("io.lumine:MythicLib-dist:1.6.2-20230827.205210-9")
        compileOnly("net.Indyuce:MMOItems-API:6.9.5-20230827.205716-2")
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
        minecraftVersion("1.20.1")
    }

    shadowJar {
        SUPPORTED_VERSIONS.forEach { dependsOn(":${it.nmsVersion}:reobfJar") }

        //archiveClassifier = null
        relocate("dev.triumphteam.gui", "io.th0rgal.oraxen.shaded.triumphteam.gui")
        relocate("com.jeff_media.customblockdata", "io.th0rgal.oraxen.shaded.customblockdata")
        relocate("com.jeff_media.morepersistentdatatypes", "io.th0rgal.oraxen.shaded.morepersistentdatatypes")
        relocate("com.jeff_media.persistentdataserializer", "io.th0rgal.oraxen.shaded.persistentdataserializer")
        relocate("me.gabytm.util.actions", "io.th0rgal.oraxen.shaded.actions")
        relocate("org.intellij.lang.annotations", "io.th0rgal.oraxen.shaded.intellij.annotations")
        relocate("org.jetbrains.annotations", "io.th0rgal.oraxen.shaded.jetbrains.annotations")
        relocate("com.udojava.evalex", "io.th0rgal.oraxen.shaded.evalex")

        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Version" to pluginVersion,
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ").format(Date.from(Instant.now())),
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}",
                    "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
                    "Compiled" to (project.findProperty("oraxen_compiled")?.toString() ?: "true").toBoolean()
                )
            )
        }
        archiveFileName.set("oraxen-${pluginVersion}.jar")
    }

    compileJava.get().dependsOn(clean)
    build.get().dependsOn(shadowJar)
    build.get().dependsOn(publishToMavenLocal)
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "io.th0rgal.oraxen.OraxenPlugin"
    version = pluginVersion
    name = "Oraxen"
    apiVersion = "1.18"
    foliaSupported = true
    authors = listOf("th0rgal", "boy0000")
    softDepend = listOf("LightAPI", "PlaceholderAPI", "MythicMobs", "MMOItems", "MythicCrucible", "BossShopPro", "CrateReloaded", "ItemBridge", "WorldEdit", "WorldGuard", "Towny", "Factions", "Lands", "PlotSquared", "NBTAPI", "ModelEngine", "CrashClaim", "ViaBackwards")
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
    )
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components.getByName("java"))
        }
    }
}

if (pluginPath != null) {
    tasks {
        register<Copy>("copyJar") {
            this.doNotTrackState("Overwrites the plugin jar to allow for easier reloading")
            dependsOn(shadowJar, jar)
            from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
            into(pluginPath)
            doLast {
                println("Copied to plugin directory $pluginPath")
            }
        }
        named<DefaultTask>("build").get().dependsOn("copyJar")
    }
}
