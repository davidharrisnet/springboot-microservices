# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

- `README.md` at the repo root is a step-by-step guide for creating a Spring Boot project from the command line, starting with `gradle init`, for an environment with no access to start.spring.io (or the Spring CLI's `spring init`, which calls it). Maven Central and the Gradle Plugin Portal are reachable.
- `microservice/` is the project the guide produces: a single-project Gradle build (Kotlin DSL, Gradle 9.8.2 wrapper, Java 21, Spring Boot 4.1.2, Spring Web), package `com.fbxmuz.service`. Steps 1–9 make it identical to Spring Initializr output except for the differences listed in step 9 (`../demo` is the Initializr-generated reference to diff against). Later steps add features, starting with `HelloController` (`GET /hello?name=`) in step 10.
- When the project changes, update the README step that produces it, so the guide stays reproducible from scratch. The user runs all commands themselves; write the step, then read the result.

## Commands

Run from `microservice/`:

```bash
./gradlew build                                                          # compile, test, bootJar
./gradlew test --tests com.fbxmuz.service.ServiceApplicationTests        # one test class
./gradlew bootRun                                                        # Tomcat on port 8080
```

## Spring Boot 4 specifics

- Spring Web is `spring-boot-starter-webmvc`, tested with `spring-boot-starter-webmvc-test`. `spring-boot-starter-web` is deprecated.
- `@WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`, not the Boot 3 package.
- Like Initializr output, Gradle and Java files are indented with tabs.

## Notes

- The user works in Eclipse, which writes build output to `bin/`; `microservice/.gitignore` ignores it.
