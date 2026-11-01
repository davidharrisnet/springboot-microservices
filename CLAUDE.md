# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and test

Gradle multi-project build (Kotlin DSL, Gradle 9.4.1 wrapper, Java 21 toolchain). Always use `./gradlew`, not a system `gradle`.

```bash
./gradlew build                                    # compile, test, package all services
./gradlew :greeting-service:test                   # one service's tests
./gradlew :greeting-service:test --tests '*GreetingControllerTest.greetsByName'  # one test
./gradlew :greeting-service:bootRun                # run a service
```

## Layout

- Each microservice is a top-level subproject registered in `settings.gradle.kts`. Currently only `greeting-service` (port 8081).
- Each service has its own package `com.example.microservices.<service>` and its own `server.port` and `spring.application.name` in `src/main/resources/application.yml`, so several services can run at once.
- Plugin versions live in `gradle/libs.versions.toml`. Spring Boot's BOM (through `io.spring.dependency-management`) manages library versions, so starters are declared without versions.
- No shared build logic yet: each service's `build.gradle.kts` is self-contained. No inter-service infrastructure (gateway, discovery, config server, messaging) exists yet; confirm with the user before adding any.
- `SPRINGBOOT.md` is the step-by-step setup guide, including how to add a new service.

## Spring Boot 4 specifics

- The web starter is `spring-boot-starter-webmvc`, tested with `spring-boot-starter-webmvc-test`. `spring-boot-starter-web` is deprecated.
- `@WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`, not the Boot 3 package.
- Test dependencies are `testImplementation`, so test classes must be under `src/test/java`.

## Notes

- The user works in Eclipse, which writes build output to `<service>/bin/`. Gradle's output goes to `build/`. `bin/` is not currently in `.gitignore`, so don't commit it.
