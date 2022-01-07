import java.text.SimpleDateFormat
import java.util.Date
import java.io.File
import net.mamoe.mirai.console.gradle.BuildMiraiPluginTask

val gitTagName: String? get() = Regex("(?<=refs/tags/).*").find(System.getenv("GITHUB_REF") ?: "")?.value
val gitCommitSha: String? get() = System.getenv("GITHUB_SHA") ?: null
val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(Date()) as String

group = "com.github.asforest"
version = gitTagName ?: "0.0.0"

plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.9.0"
}

repositories {
//    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

dependencies {
//    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.15")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.jetbrains.pty4j:pty4j:0.12.7")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.register("buildWithManifest") {
    dependsOn(tasks.named("buildPlugin"))

    tasks.named<BuildMiraiPluginTask>("buildPlugin").get().apply {
        manifest {
            attributes("Mirai-Plugin-Version" to archiveVersion.get())
            attributes("Git-Commit" to (gitCommitSha ?: ""))
            attributes("Compile-Time" to timestamp)
            attributes("Compile-Time-Ms" to System.currentTimeMillis())
        }
    }
}

tasks.register("developing", Copy::class) {
    dependsOn(tasks.named("buildWithManifest"))

    val archive = project.buildDir.path+File.separator+"mirai"+
            File.separator+project.name+"-"+version+".mirai.jar"

    val env = System.getenv()["PluginDebugDir"]
        ?: throw RuntimeException("The environmental variable 'PluginDebugDir' is not set")
    if(!File(env).run { !exists() || isDirectory })
        throw RuntimeException("The 'PluginDebugDir' $env does not exist or is a file")

    from(archive).into(env).doLast {
        val cps = env+File.separator+"cp.bat"
        if(File(cps).exists())
            Runtime.getRuntime().exec(cps)
    }
}