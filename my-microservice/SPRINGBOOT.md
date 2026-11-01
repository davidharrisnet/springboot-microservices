# Spring Boot setup

This project was set up in two steps:

1. **`gradle init`**, run by hand, generated a plain Java application. `App.java`
   was then replaced by hand with a Spring Boot REST controller, which didn't compile.
2. **Claude Code** changed the generated build to make it a Spring Boot
   microservice.

## Step 1: `gradle init`

`gradle init` generated an application project with these settings:

| Setting         | Value                     |
|-----------------|---------------------------|
| Project type    | Application (Java)        |
| Build DSL       | Kotlin (`*.gradle.kts`)   |
| Root project    | `my-microservice`         |
| Subproject      | `app`                     |
| Package         | `org.example`             |
| Java toolchain  | 21                        |
| Test framework  | JUnit 4                   |
| Gradle version  | 9.4.1 (wrapper)           |

It produced the wrapper (`gradlew`, `gradle/wrapper/`) and a version catalog
(`gradle/libs.versions.toml`) with guava and junit. It also produced a sample
`App` with a `getGreeting()` method and a matching `AppTest`.

After init, `App.java` was replaced by hand with a Spring Boot `@RestController`
that has a `/hello` endpoint. That version didn't compile, because the generated
build had no Spring dependencies, so none of the `org.springframework.*` imports
resolved. Fixing that is the reason for step 2.

## Step 2: Spring Boot conversion (Claude Code)

### `settings.gradle.kts` — plugin version

A `pluginManagement` block declares the Spring Boot plugin version in one place:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.springframework.boot") version "4.0.0"
    }
}
```

Spring Boot is a *project* plugin, so it can't go in the settings file's own
`plugins {}` block (that block is only for settings plugins such as foojay).
Declaring the plugin here does nothing by itself; it only sets the version that subprojects
get when they apply it. To upgrade Spring Boot, change the version here.

### `app/build.gradle.kts` — apply the plugin and add dependencies

```kotlin
plugins {
    application
    id("org.springframework.boot")   // version comes from settings.gradle.kts
}

dependencies {
    // ...existing junit / guava...

    // Spring Boot BOM manages versions for the starters below.
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.web)
}
```

- The plugin adds the `bootRun` and `bootJar` tasks and others.
- The BOM is a list of compatible versions for every Spring Boot library, and it always
  matches the plugin version. That's why starters need no version of their own. A
  `platform(...)` import is used here instead of the separate
  `io.spring.dependency-management` plugin.

### `gradle/libs.versions.toml` — catalog entry

```toml
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
```

This entry has no version because the BOM supplies it. In build files, refer to it as
`libs.spring.boot.starter.web`.

### `app/src/main/java/org/example/App.java`

The hand-written controller called `SpringApplication.run(DemoApplication.class, args)`,
but `DemoApplication` doesn't exist. It now passes `App.class`. The rest of the file
is unchanged. Its Spring imports resolve now because of the build changes above.

### `app/src/test/java/org/example/AppTest.java`

The generated test called `App.getGreeting()`, which `App` no longer has, so the
test code didn't compile. The new test checks the controller method directly:

```java
assertEquals("Hello Spring!", classUnderTest.hello("Spring"));
```

## Build and run

```bash
./gradlew build      # compile and run the tests
./gradlew bootRun    # start the app on port 8080
```

Then open <http://localhost:8080/hello?name=You>. The response is `Hello You!`.

## Notes

- If the IDE still shows unresolved imports, reload the Java project. In VS Code, use
  "Java: Clean Java Language Server Workspace".
- The tests still use JUnit 4 from the version catalog. To use Spring's test support
  (`@SpringBootTest`, MockMvc), add `testImplementation("org.springframework.boot:spring-boot-starter-test")`
  and move to JUnit Jupiter.
