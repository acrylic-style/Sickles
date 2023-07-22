import java.net.URI

plugins {
    java
    kotlin("jvm") version "1.9.0"
}

group = "xyz.acrylicstyle"
version = "1.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.azisaba:kotlin-nms-extension-v1_20_R1:1.0-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.20.1-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "utf-8"
    }

    processResources {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**/plugin.yml")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.rootProject.name
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        from(projectDir) { include("LICENSE") }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    jar {
        from(configurations.getByName("implementation").apply{ isCanBeResolved = true }.map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
