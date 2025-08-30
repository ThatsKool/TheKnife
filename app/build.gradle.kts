
plugins {
    // CLI application support
    application

    // JavaFX plugin
    id("org.openjfx.javafxplugin") version "0.1.0"

    // Shadow plugin for fat JAR
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit Jupiter for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.1")
    testImplementation("org.openjfx:javafx-swing:24.0.2")

    // Application dependencies
    implementation("com.google.guava:guava:31.1-jre")
}

javafx {
    version = "24.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules", "javafx.controls,javafx.fxml",
        "--add-opens", "javafx.graphics/javafx.scene=ALL-UNNAMED"
    )
}

application {
    // Fully qualified main class name
    mainClass.set("dev.theknife.app.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java")
        resources.srcDirs("src/test/resources")
    }
}

// Configure Shadow JAR
tasks.shadowJar {
    archiveBaseName.set("theknife-app")
    archiveClassifier.set("") // no "-all" suffix
    archiveVersion.set("")     // no version in filename
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}
