val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "me.hiencao"
version = "0.0.1"

application {
    mainClass.set("me.hiencao.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.51.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-client-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.3")
    implementation("io.github.pdvrieze.xmlutil:core-jdk:0.90.3")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("org.bspfsystems:yamlconfiguration:2.0.1")
    implementation("org.litote.kmongo:kmongo-coroutine:5.1.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")
}
