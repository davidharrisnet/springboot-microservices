# springboot-microservices

How to create a Spring Boot web project from the command line, without access to start.spring.io (or the Spring CLI's `spring init`, which calls it).

The result is the same as a Spring Initializr project with these settings:

| Setting      | Value             |
|--------------|-------------------|
| Project      | Gradle - Kotlin   |
| Spring Boot  | 4.1.2             |
| Java         | 21                |
| Group        | `com.fbxmuz`      |
| Artifact     | `service`         |
| Package      | `com.fbxmuz.service` |
| Dependencies | Spring Web        |

Prerequisites: Java 21 and any recent Gradle (these steps used Gradle 9.4.1). Gradle is only used once, in step 1, to generate the wrapper; every later command uses `./gradlew`. The build downloads its plugins and libraries from the Gradle Plugin Portal and Maven Central.

The project lives in the `microservice/` directory. Step 1 creates it; every later command runs from inside it.

## Step 1: Generate a basic Gradle build

```bash
mkdir microservice
cd microservice
gradle init --type basic --dsl kotlin --project-name service --no-incubating
```

`--type basic` creates an empty build with a Gradle wrapper, without the sample `app` subproject that `--type java-application` adds. Running it in a new, empty directory means `gradle init` has nothing to overwrite and doesn't ask.

It generates:

| File | Contents |
|------|----------|
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` | The Gradle 9.4.1 wrapper |
| `settings.gradle.kts` | `rootProject.name = "service"` |
| `build.gradle.kts` | A comment only |
| `gradle.properties` | `org.gradle.configuration-cache=true` |
| `gradle/libs.versions.toml` | An empty version catalog |
| `.gitignore` | `.gradle`, `build` and `.kotlin` |
| `.gitattributes` | Line endings for `gradlew` and `*.bat` |

## Step 2: Remove files a Spring Boot project doesn't use

```bash
rm gradle.properties gradle/libs.versions.toml
```

A Spring Initializr project has neither file. Library versions come from Spring Boot's dependency management instead of a version catalog, and the configuration cache is left at Gradle's default (off).

`settings.gradle.kts` and `build.gradle.kts` stay; step 4 replaces their contents.
