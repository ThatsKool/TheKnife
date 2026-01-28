plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

// Configurazione Java - richiede Java 17 o superiore per l'esecuzione
// Il JAR sarà compatibile con Java 17+ anche se compilato con versioni più recenti
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Configura encoding UTF-8 per evitare errori con caratteri Unicode (emoji, simboli)
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-encoding")
    options.compilerArgs.add("UTF-8")
    // Forza il target bytecode a Java 17 per garantire compatibilità
    options.release.set(17)
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

// Configura encoding per i source set
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        resources {
            // Risorse principali dell'applicazione
            srcDirs("src/main/resources", "../data")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
    }
}

val javafxVersion = "21.0.4"

dependencies {
    // Your app deps
    // CSV parsing
    implementation("org.apache.commons:commons-csv:1.11.0")

    // JavaFX for your platform — auto-detects OS or uses targetOs property
    // Usa implementation invece di runtimeOnly per includere nel fat JAR
    val targetOs = project.findProperty("targetOs") as? String ?: run {
        val osName = System.getProperty("os.name", "").lowercase()
        when {
            osName.contains("win") -> "win"
            osName.contains("mac") -> "mac"
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> "linux"
            else -> "linux" // default to linux for unknown systems
        }
    }
    implementation("org.openjfx:javafx-base:$javafxVersion:$targetOs")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$targetOs")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$targetOs")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$targetOs")

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
    applicationDefaultJvmArgs = listOf("-Xmx4g")
}

tasks.named<JavaExec>("run") {
    jvmArgs = listOf("-Xmx4g")
}

// This is the fat JAR task — merges all runtime deps into one jar
tasks.jar {
    archiveBaseName.set("TheKnife")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "dev.theknife.app.Launcher"
        // Aggiungi i moduli JavaFX al manifest per Java 17+
        attributes["Add-Opens"] = "javafx.base/javafx.beans.value javafx.base/javafx.collections javafx.graphics/javafx.scene javafx.graphics/javafx.stage"
        // Specifica che il JAR richiede Java 17 o superiore
        attributes["Build-Jdk-Spec"] = "17"
        attributes["Created-By"] = "Gradle ${project.gradle.gradleVersion} with Java ${java.sourceCompatibility}"
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // Assicura che il JAR sia compatibile con Java 17+
    doFirst {
        logger.lifecycle("Compilazione JAR compatibile con Java 17+")
        logger.lifecycle("Source compatibility: ${java.sourceCompatibility}")
        logger.lifecycle("Target compatibility: ${java.targetCompatibility}")
        logger.lifecycle("Release target: 17 (compatibile con Java 17, 18, 19, 20, 21, 22, 23, 24, ...)")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("migrateTestCSV") {
    group = "util"
    description = "Add progressive IDs to test CSV and pad missing fields"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("dev.theknife.app.tools.TestCSVIdAdder")
    args(project.file("src/test/resources/data/michelin_my_maps.csv").absolutePath)
}

tasks.register<JavaExec>("migrateMainCSV") {
    group = "util"
    description = "Add progressive IDs and update header in main CSV"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("dev.theknife.app.tools.TestCSVIdAdder")
    args(project.file("../data/data/michelin_my_maps.csv").absolutePath, "sequential")
}

tasks.javadoc {
    destinationDir = file("$rootDir/doc/javadoc")
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

// Task per verificare che il JAR includa JavaFX
tasks.register("verifyJar") {
    group = "verification"
    description = "Verifica che il JAR includa tutte le dipendenze JavaFX"
    dependsOn("jar")
    
    doLast {
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        if (!jarFile.exists()) {
            throw GradleException("JAR non trovato: ${jarFile.absolutePath}")
        }
        
        logger.lifecycle("Verifica JAR: ${jarFile.name}")
        logger.lifecycle("Dimensione: ${jarFile.length() / 1024 / 1024} MB")
        
        // Usa zipTree di Gradle per ispezionare il JAR
        val jarContents = zipTree(jarFile)
        val javafxClasses = mutableListOf<String>()
        val nativeLibs = mutableListOf<String>()
        
        jarContents.files.forEach { file ->
            val relativePath = file.relativeTo(jarFile.parentFile).path.replace('\\', '/')
            
            // Verifica classi JavaFX
            if (relativePath.startsWith("javafx/") || relativePath.startsWith("com/sun/javafx/")) {
                javafxClasses.add(relativePath)
            }
            
            // Verifica librerie native
            if (relativePath.endsWith(".dll") || 
                relativePath.endsWith(".so") || 
                relativePath.endsWith(".dylib") ||
                relativePath.contains("/native/")) {
                nativeLibs.add(relativePath)
            }
        }
        
        if (javafxClasses.isEmpty()) {
            logger.warn("ATTENZIONE: Nessuna classe JavaFX trovata nel JAR!")
            logger.warn("Il JAR potrebbe non funzionare correttamente.")
        } else {
            logger.lifecycle("Classi JavaFX trovate: ${javafxClasses.size}")
            logger.info("Prime classi JavaFX: ${javafxClasses.take(5).joinToString(", ")}")
        }
        
        if (nativeLibs.isEmpty()) {
            logger.warn("ATTENZIONE: Nessuna libreria nativa trovata nel JAR!")
        } else {
            logger.lifecycle("Librerie native trovate: ${nativeLibs.size}")
            logger.info("Librerie native: ${nativeLibs.take(10).joinToString(", ")}")
        }
    }
}

// Task per compilare JAR per Windows
tasks.register("buildJarWindows") {
    group = "distribution"
    description = "Compila e copia il JAR per Windows nella cartella bin/windows"
    
    doFirst {
        val targetDir = file("${project.rootDir}/bin/windows")
        targetDir.mkdirs()
        val oldJar = file("${targetDir}/TheKnife.jar")
        if (oldJar.exists()) {
            logger.lifecycle("Eliminazione vecchio JAR Windows: ${oldJar.absolutePath}")
            oldJar.delete()
        }
        logger.lifecycle("Compilazione JAR per Windows...")
    }
    
    doLast {
        // Esegui gradlew con targetOs=win
        project.exec {
            workingDir = project.rootDir
            if (System.getProperty("os.name").lowercase().contains("win")) {
                commandLine("${project.rootDir}/gradlew.bat", "clean", "jar", "-PtargetOs=win")
            } else {
                commandLine("${project.rootDir}/gradlew", "clean", "jar", "-PtargetOs=win")
            }
        }
        
        // Copia il JAR
        val sourceJar = file("${project.layout.buildDirectory.get()}/libs/TheKnife.jar")
        val targetJar = file("${project.rootDir}/bin/windows/TheKnife.jar")
        if (sourceJar.exists()) {
            copy {
                from(sourceJar)
                into(file("${project.rootDir}/bin/windows"))
            }
            logger.lifecycle("✓ JAR Windows creato: ${targetJar.absolutePath}")
            logger.lifecycle("  Dimensione: ${targetJar.length() / 1024 / 1024} MB")
        } else {
            throw GradleException("Errore: JAR compilato non trovato")
        }
    }
}

// Task per compilare JAR per Linux
tasks.register("buildJarLinux") {
    group = "distribution"
    description = "Compila e copia il JAR per Linux nella cartella bin/linux"
    
    doFirst {
        val targetDir = file("${project.rootDir}/bin/linux")
        targetDir.mkdirs()
        val oldJar = file("${targetDir}/TheKnife.jar")
        if (oldJar.exists()) {
            logger.lifecycle("Eliminazione vecchio JAR Linux: ${oldJar.absolutePath}")
            oldJar.delete()
        }
        logger.lifecycle("Compilazione JAR per Linux...")
    }
    
    doLast {
        // Esegui gradlew con targetOs=linux
        project.exec {
            workingDir = project.rootDir
            if (System.getProperty("os.name").lowercase().contains("win")) {
                commandLine("${project.rootDir}/gradlew.bat", "clean", "jar", "-PtargetOs=linux")
            } else {
                commandLine("${project.rootDir}/gradlew", "clean", "jar", "-PtargetOs=linux")
            }
        }
        
        // Copia il JAR
        val sourceJar = file("${project.layout.buildDirectory.get()}/libs/TheKnife.jar")
        val targetJar = file("${project.rootDir}/bin/linux/TheKnife.jar")
        if (sourceJar.exists()) {
            copy {
                from(sourceJar)
                into(file("${project.rootDir}/bin/linux"))
            }
            logger.lifecycle("✓ JAR Linux creato: ${targetJar.absolutePath}")
            logger.lifecycle("  Dimensione: ${targetJar.length() / 1024 / 1024} MB")
        } else {
            throw GradleException("Errore: JAR compilato non trovato")
        }
    }
}

// Task per compilare JAR per Mac
tasks.register("buildJarMac") {
    group = "distribution"
    description = "Compila e copia il JAR per Mac nella cartella bin/mac"
    
    doFirst {
        val targetDir = file("${project.rootDir}/bin/mac")
        targetDir.mkdirs()
        val oldJar = file("${targetDir}/TheKnife.jar")
        if (oldJar.exists()) {
            logger.lifecycle("Eliminazione vecchio JAR Mac: ${oldJar.absolutePath}")
            oldJar.delete()
        }
        logger.lifecycle("Compilazione JAR per Mac...")
    }
    
    doLast {
        // Esegui gradlew con targetOs=mac
        project.exec {
            workingDir = project.rootDir
            if (System.getProperty("os.name").lowercase().contains("win")) {
                commandLine("${project.rootDir}/gradlew.bat", "clean", "jar", "-PtargetOs=mac")
            } else {
                commandLine("${project.rootDir}/gradlew", "clean", "jar", "-PtargetOs=mac")
            }
        }
        
        // Copia il JAR
        val sourceJar = file("${project.layout.buildDirectory.get()}/libs/TheKnife.jar")
        val targetJar = file("${project.rootDir}/bin/mac/TheKnife.jar")
        if (sourceJar.exists()) {
            copy {
                from(sourceJar)
                into(file("${project.rootDir}/bin/mac"))
            }
            logger.lifecycle("✓ JAR Mac creato: ${targetJar.absolutePath}")
            logger.lifecycle("  Dimensione: ${targetJar.length() / 1024 / 1024} MB")
        } else {
            throw GradleException("Errore: JAR compilato non trovato")
        }
    }
}

// Task master per compilare tutti i JAR per tutte le piattaforme
tasks.register("buildAllJars") {
    group = "distribution"
    description = "Compila i JAR per tutte le piattaforme (Windows, Linux, Mac) e li copia in bin/"
    
    dependsOn("buildJarWindows", "buildJarLinux", "buildJarMac")
    
    doLast {
        logger.lifecycle("")
        logger.lifecycle("═══════════════════════════════════════════════════════")
        logger.lifecycle("✓ Tutti i JAR sono stati compilati e copiati in bin/")
        logger.lifecycle("═══════════════════════════════════════════════════════")
        logger.lifecycle("  • bin/windows/TheKnife.jar")
        logger.lifecycle("  • bin/linux/TheKnife.jar")
        logger.lifecycle("  • bin/mac/TheKnife.jar")
        logger.lifecycle("")
    }
}
