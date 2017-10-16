import org.gradle.api.file.FileTree
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.wrapper.Wrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0-SNAPSHOT"

task<Wrapper>("wrapper") {
    gradleVersion = "3.5"
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}

task("beforeJar") {
    rootDir.resolve("build").mkdirs()
    rootDir.resolve("build").resolve("1.txt").createNewFile()
    doLast {
        rootDir.resolve("build").resolve("1.txt").bufferedWriter().use {
            configurations.compile.forEach { s -> it.write("$s\n") }
        }
    }
}

tasks.withType<Jar> {
    archiveName = project.name + archiveName
    manifest.attributes["Main-Class"] = "MainKt"
    val list1: List<String> = rootDir.resolve("build").resolve("1.txt").bufferedReader().readLines()
    val list2: List<FileTree> = list1.map { zipTree(it) }
    from(list2)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.1.1"

    repositories {
        maven { setUrl("http://maven.aliyun.com/nexus/content/groups/public/") }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

apply {
    plugin("java")
    plugin("kotlin")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val kotlin_version: String by extra

repositories {
    maven { setUrl("http://maven.aliyun.com/nexus/content/groups/public/") }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version")
    compile("com.google.code.gson:gson:+")
    compile("commons-cli:commons-cli:+")
    compile("org.springframework:spring-context:+")
    compile("org.springframework:spring-aspects:+")
}
