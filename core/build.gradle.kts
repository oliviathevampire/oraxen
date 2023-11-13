plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.6"
    id("maven-publish")
    id("org.ajoberstar.grgit.service") version "5.2.0"
    alias(libs.plugins.shadowjar)
}

val pluginVersion = project.property("pluginVersion") as String
tasks {
    shadowJar.get().archiveFileName.set("oraxen-${pluginVersion}.jar")
    build.get().dependsOn(shadowJar)
}

dependencies {
    val actionsVersion = "1.0.0-SNAPSHOT"
    implementation("dev.triumphteam:triumph-gui:3.1.5") { exclude("net.kyori") }
    implementation("io.th0rgal:protectionlib:1.3.6")
    implementation("com.jeff-media:custom-block-data:2.2.2")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")
    implementation("com.jeff_media:PersistentDataSerializer:1.0-SNAPSHOT")
    implementation("gs.mclo:java:2.2.1")
    implementation("org.jetbrains:annotations:24.0.1") { isTransitive = false }

    implementation("me.gabytm.util:actions-spigot:$actionsVersion") { exclude(group = "com.google.guava") }
    implementation("com.tcoded:FoliaLib:0.2.5")

    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = pluginVersion

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "oraxen"
            this.isAllowInsecureProtocol = true
        }
    }
}