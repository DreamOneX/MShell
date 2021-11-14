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
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.8.0"
}

repositories {
//    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    // 加载本地依赖
    implementation(fileTree("libs").include("*.jar").exclude("*sources.jar"))
    implementation("com.esotericsoftware.yamlbeans:yamlbeans:1.15")
//    implementation("org.jetbrains.pty4j:pty4j:0.12.5")

    // dependcies from org.jetbrains.pty4j:pty4j:0.12.5
    implementation("org.jetbrains:annotations:20.1.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("log4j:log4j:1.2.17")
    implementation("net.java.dev.jna:jna:5.9.0")
    implementation("net.java.dev.jna:jna-platform:5.9.0")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.register("buildWithManifest") {
    dependsOn(tasks.named("buildPlugin"))

    tasks.named<BuildMiraiPluginTask>("buildPlugin").get().apply {
        manifest {
            attributes("Mirai-Plugin-Id" to "$group.mshell")
            attributes("Mirai-Plugin-Name" to "MShell")
            attributes("Mirai-Plugin-Version" to archiveVersion.get())
            attributes("Mirai-Plugin-Author" to "Asforest")
            attributes("Git-Commit" to (gitCommitSha ?: ""))
            attributes("Compile-Time" to timestamp)
            attributes("Compile-Time-Ms" to System.currentTimeMillis())
        }

        dependencies {
            include(dependency(fileTree("libs").include("*.jar").exclude("*sources.jar")))
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
        Runtime.getRuntime().exec(env+File.separator+"cp.bat")
    }


}