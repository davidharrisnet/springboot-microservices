# springboot-microservices

A Spring Boot microservice built with Gradle and Java 21.

## How this project was created

1. **`gradle init`**: generated a plain Java application in `my-microservice/`
   (Kotlin DSL, JUnit 4, package `org.example`, Gradle 9.4.1).
2. **Hand-written `App.java`**: the generated `App` was replaced with a Spring Boot
   REST controller (`/hello`). It didn't compile, because the build had no Spring
   dependencies.
3. **Spring Boot conversion**: Claude Code changed the build to add the Spring Boot
   plugin, the Spring Boot BOM and the web starter, so the imports resolve. It also
   fixed the `run(...)` class reference in `App.java` and updated `AppTest`.

[my-microservice/SPRINGBOOT.md](my-microservice/SPRINGBOOT.md) explains each step
and every file that changed.

## Layout

```
my-microservice/
├── settings.gradle.kts          # sets the Spring Boot plugin version
├── gradle/libs.versions.toml    # version catalog
├── SPRINGBOOT.md                # setup history and details
└── app/
    ├── build.gradle.kts         # applies Spring Boot and adds the web starter
    └── src/
        ├── main/java/org/example/App.java      # @SpringBootApplication + /hello endpoint
        └── test/java/org/example/AppTest.java
```

## Quick start

```bash
cd my-microservice
./gradlew build      # compile and run the tests
./gradlew bootRun    # start the app on port 8080
curl "http://localhost:8080/hello?name=You"   # -> Hello You!
```

You need JDK 21. Gradle's toolchain support (the foojay resolver) can download it
automatically.
