# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current state

Commit `f8fcec3` ("empty project") removed the whole build, leaving only `README.md`, `LICENSE`, `.gitignore` and `.gitattributes`. The user is rebuilding it step by step:

1. The user runs `gradle init` to generate a plain Gradle project.
2. The project is then changed into a Spring Boot microservice.

The project lives in `microservice/` (generated there by `gradle init` so nothing at the repo root is overwritten). `README.md` at the repo root is the guide, separate from the project, and documents every step of this process. The user runs all commands themselves; write the step, then read the result. Whenever you help with a step (a command run, a file added or changed), add it to `README.md` in order, with the exact commands and file contents, so the README can be followed from scratch. Don't jump ahead and do several steps at once unless the user asks.

## Earlier setup (for reference)

Commit `7865d50` has a working version that the user built by following a guide. Look at it with `git show 7865d50:<path>` when it helps:

- A Gradle multi-project build (Kotlin DSL, Gradle 9.4.1 wrapper, Java 21, Spring Boot 4.1.2). Each microservice was a top-level subproject in `settings.gradle.kts`. The first one was `greeting-service` on port 8081.
- `SPRINGBOOT.md` was the step-by-step guide (`gradle init`, then converting to Spring Boot, then adding services).
- Spring Boot 4 differences from Boot 3: the web starter is `spring-boot-starter-webmvc`, tested with `spring-boot-starter-webmvc-test`. `@WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`.

## Repo conventions that still apply

- `.gitignore` ignores all `*.jar` files except `gradle/wrapper/gradle-wrapper.jar`, so a Gradle wrapper can be committed. `.gitattributes` forces LF line endings for `gradlew` and CRLF for `*.bat`.
- The user works in Eclipse, which writes build output to `<project>/bin/`. `bin/` is not in `.gitignore`, so don't commit it.
