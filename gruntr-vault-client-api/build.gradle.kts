plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val junitVersion: String by project
val jacksonDatabindVersion: String by project

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")

    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}