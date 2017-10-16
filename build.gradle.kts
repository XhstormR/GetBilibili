import org.gradle.api.tasks.wrapper.Wrapper

version = "1.0-SNAPSHOT"

task("wrapper", Wrapper::class) {
    gradleVersion = "3.5"
    distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
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

configure(JavaPluginConvention::class) {
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
    compile("org.jsoup:jsoup:+")
    compile("commons-cli:commons-cli:+")
}
