plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

val javafxVersion = "24.0.2"

dependencies {
    // Your app deps
    implementation("com.google.guava:guava:31.1-jre")

    // JavaFX for your platform — for cross-platform, add :win, :mac, :linux variants
    runtimeOnly("org.openjfx:javafx-base:$javafxVersion:win")
    runtimeOnly("org.openjfx:javafx-controls:$javafxVersion:win")
    runtimeOnly("org.openjfx:javafx-fxml:$javafxVersion:win")
    runtimeOnly("org.openjfx:javafx-graphics:$javafxVersion:win")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.1")
}

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml")
}

// This is your actual JavaFX App class
application {
    mainClass.set("dev.theknife.app.Launcher")
}

// This is the fat JAR task — merges all runtime deps into one jar
tasks.jar {
    archiveBaseName.set("TheKnife")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "dev.theknife.app.Launcher"
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}
