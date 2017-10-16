import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven("http://maven.aliyun.com/nexus/content/groups/public/")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:+")
    }
}

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public/")
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-reflect")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8")

    compile("org.springframework:spring-context:+")
    compile("org.springframework:spring-aspects:+")

    compile("commons-cli:commons-cli:+")
    compile("com.google.code.gson:gson:+")
}

version = "1.0-SNAPSHOT"

plugins {
    idea
    application
    kotlin("jvm")
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.2"
    distributionType = Wrapper.DistributionType.ALL
}

task("beforeJar") {
    rootDir
            .resolve("build").apply { mkdirs() }
            .resolve("1.txt").apply { createNewFile() }
            .bufferedWriter().use { configurations.compile.forEach { s -> it.write("$s\n") } }
}

tasks.withType<Jar> {
    dependsOn("beforeJar")
    version = ""
    manifest.attributes["Main-Class"] = "com.xhstormr.bilibili.MainKt"
    val list1: List<String> = rootDir.resolve("build").resolve("1.txt").bufferedReader().readLines()
    val list2: List<FileTree> = list1.map { zipTree(it) }
    from(list2)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//应用于 run
application {
    mainClassName = "com.xhstormr.bilibili.MainKt"
    applicationDefaultJvmArgs = listOf("-Dname=Tom")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
