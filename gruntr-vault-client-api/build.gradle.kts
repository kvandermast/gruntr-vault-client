plugins {
    id("java")
}

repositories {
    mavenCentral()
}

val junitVersion: String by project
val jacksonDatabindVersion: String by project
val mockWebServerVersion: String by project

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")

    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/mockwebserver
    testImplementation("com.squareup.okhttp3:mockwebserver:${mockWebServerVersion}")

}

tasks.test {
    useJUnitPlatform()
}