val javafxVersion = "21.0.2"

plugins {
    id("java")
    id("application") // Apply the application plugin
    id("org.openjfx.javafxplugin") version "0.1.0"
}

// Specify the main class for your application
application {
    mainClass.set("com.zhaw.it.pm3.tournamentgenerator.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Add Mockito as a test dependency
    testImplementation("org.mockito:mockito-core:3.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.10.0")

    // Add JavaFX dependencies
    implementation("org.openjfx:javafx-base:$javafxVersion")
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-graphics:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")


    // Add the Javax mail dependency
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("org.openjfx:javafx-swing:21")

    implementation ("com.google.code.gson:gson:2.11.0")

}

javafx {
    version = javafxVersion
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}
