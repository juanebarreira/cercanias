plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.seleniumhq.selenium:selenium-java:4.18.1")
    implementation("org.seleniumhq.selenium:selenium-devtools-v120:4.18.1")
    implementation("io.github.bonigarcia:webdrivermanager:5.7.0")
    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpcore:4.4.16")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.json:json:20231013")
}

application {
    mainClass.set("org.cercanias.crawl.TelegramBot")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.cercanias.crawl.TelegramBot"
    }
    
    // Incluir todas las dependencias en el jar
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // Excluir archivos de firma
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/MANIFEST.MF")
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaExec> {
    environment("BOT_TOKEN", System.getenv("BOT_TOKEN") ?: "")
    environment("CHAT_ID", System.getenv("CHAT_ID") ?: "")
}

tasks.test {
    useJUnitPlatform()
}