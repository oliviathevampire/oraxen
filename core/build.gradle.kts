plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("maven-publish")
    alias(libs.plugins.shadowjar)
    id("org.ajoberstar.grgit.service") version "5.2.0"
}

val pluginVersion = project.property("pluginVersion") as String
tasks {
    shadowJar.get().archiveFileName.set("oraxen-${pluginVersion}.jar")
    build.get().dependsOn(shadowJar)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("gs.mclo:java:2.2.1")

    val actionsVersion = "1.0.0-SNAPSHOT"
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
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    val publishData = PublishData(project)
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = publishData.getVersion()

            from(components["java"])
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("MAVEN_USERNAME") ?: project.findProperty("oraxenUsername") as? String ?: ""
                    password = System.getenv("MAVEN_PASSWORD") ?: project.findProperty("oraxenPassword") as? String ?: ""
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }

            url = uri(publishData.getRepository())
            name = "oraxen"
        }
    }
}

class PublishData(private val project: Project) {
    var type: Type = getReleaseType()
    var hashLength: Int = 7

    private fun getReleaseType(): Type {
        val branch = getCheckedOutBranch()
        println("Branch: $branch")
        return when {
            branch.contentEquals("master") -> Type.RELEASE
            branch.contentEquals("develop") -> Type.SNAPSHOT
            else -> Type.DEV
        }
    }

    private fun getCheckedOutGitCommitHash(): String =
        System.getenv("GITHUB_SHA")?.substring(0, hashLength) ?: "local"

    private fun getCheckedOutBranch(): String =
        System.getenv("GITHUB_REF")?.replace("refs/heads/", "") ?: grgitService.service.get().grgit.branch.current().name

    fun getVersion(): String = getVersion(false)

    fun getVersion(appendCommit: Boolean): String =
        type.append(getVersionString(), appendCommit, getCheckedOutGitCommitHash())

    private fun getVersionString(): String =
        (rootProject.version as String).removeSuffix("-SNAPSHOT").removeSuffix("-DEV")

    fun getRepository(): String = type.repo

    enum class Type(private val append: String, val repo: String, private val addCommit: Boolean) {
        RELEASE("", "https://repo.oraxen.com/releases/", false),
        DEV("-DEV", "https://repo.oraxen.com/development/", true),
        SNAPSHOT("-SNAPSHOT", "https://repo.oraxen.com/snapshots/", true);

        fun append(name: String, appendCommit: Boolean, commitHash: String): String =
            name.plus(append).plus(if (appendCommit && addCommit) "-".plus(commitHash) else "")
    }
}
