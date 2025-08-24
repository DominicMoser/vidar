plugins {
    id("java")
    // Plugin to generate a class containing constants defined in the build system (e.g., from gradle.properties)
    id("com.github.gmazzo.buildconfig") version "5.6.5"
    id("maven-publish")
}

// Retrieve project properties from gradle.properties
val projectVersion: String by project
val groupName: String by project

// Extra property used by this project (minimum version of Docker Compose)
val composeVersion: String by project
group = groupName
version = projectVersion

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = project.name
            from(components["java"])
        }
    }
}

// Configure the constants that should be created
buildConfig {
    packageName(project.group.toString().lowercase() + "." + project.name.lowercase())
    className("Constants")
    documentation.set("Automatically generated class with runtime constants defined in the gradle.properties file.")
    buildConfigField("String", "VERSION", projectVersion as? String)
    buildConfigField("String", "COMPOSE_VERSION", composeVersion as? String)
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    // First, generate some comments for the generated Constants class
    dependsOn("addJavadocToBuildConfig")
    // Fail if Javadoc is incomplete
    isFailOnError = true
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xwerror", true)
        addStringOption("Xdoclint:all")
    }
}

tasks.publish {
    // Only publish when tests run without errors and Javadocs are generated
    dependsOn("test", "javadoc")
}

tasks.register("addJavadocToBuildConfig") {
    // Groups the task under the 'documentation' section
    group = "documentation"
    dependsOn("generateBuildConfig")
    doLast {
        val constantsFile = file(
            "build/generated/sources/buildConfig/main/" +
                    project.group.toString().lowercase().replace(".", "/") +
                    "/" +
                    project.name.lowercase() +
                    "/" +
                    "Constants.java"
        )
        println(constantsFile.path)
        if (constantsFile.exists()) {
            // Read the file
            var content = constantsFile.readText()

            // Add Javadoc comments to fields
            content = content.replace(
                "public static",
                "/** Automatically generated value. */\n  public static"
            )
            // Write the modified content back to the file
            constantsFile.writeText(content)
        }
    }
}