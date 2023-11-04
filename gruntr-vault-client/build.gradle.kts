plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val junitVersion: String by project

dependencies {
    implementation(project(":gruntr-vault-client-api"))
    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}