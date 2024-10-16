plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"

}

group = "com.goodasssub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation("org.projectlombok:lombok:1.18.34")
    implementation("net.minestom:minestom-snapshots:1c47dd613f")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("org.mongodb:mongodb-driver-sync:5.1.3")
    implementation("net.mangolise:mango-anti-cheat:latest")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.discord4j:discord4j-core:3.3.0-RC1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "com.goodasssub.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}

tasks.test {
    useJUnitPlatform()
}