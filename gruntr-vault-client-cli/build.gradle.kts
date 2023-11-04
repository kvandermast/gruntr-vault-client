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
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.acuz.gruntr.cli.Main"
    }

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}