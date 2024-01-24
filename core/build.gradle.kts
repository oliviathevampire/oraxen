plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("maven-publish")
    alias(libs.plugins.shadowjar)
    id("org.ajoberstar.grgit.service") version "5.2.0"
}

val pluginVersion = project.property("pluginVersion") as String
tasks {
    publish.get().dependsOn(shadowJar)
    shadowJar.get().archiveFileName.set("oraxen-${pluginVersion}.jar")
    build.get().dependsOn(shadowJar)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    val actionsVersion = "1.0.0-SNAPSHOT"
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("dev.triumphteam:triumph-gui:3.1.7") { exclude("net.kyori") }
    implementation("io.th0rgal:protectionlib:1.3.6")
    implementation("com.jeff-media:custom-block-data:2.2.2")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")
    implementation("com.jeff-media:persistent-data-serializer:1.0")
    implementation("gs.mclo:java:2.2.1")
    implementation("org.jetbrains:annotations:24.0.1") { isTransitive = false }
    implementation("me.gabytm.util:actions-spigot:$actionsVersion") { exclude(group = "com.google.guava") }
    implementation("cloud.commandframework:cloud-paper:1.8.4")

    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    shadowJar {
        relocate("cloud.commandframework", "io.th0rgal.oraxen.shaded.cloud.commandframework")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = pluginVersion

            //from(components["java"])
            artifact(tasks.shadowJar.get().apply { archiveClassifier.set("") })
        }
    }
    repositories {
        maven {
            name = "oraxen"
            this.isAllowInsecureProtocol = true
        }
    }
}
