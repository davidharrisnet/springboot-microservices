# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current state

This repository is a fresh scaffold: it contains only `README.md`, an Apache 2.0 `LICENSE`, and GitHub's standard Java `.gitignore`. No source code, build files (Maven/Gradle), or services exist yet. The name indicates the intended direction: a set of Spring Boot microservices.

Do not assume a build tool, Java version, service layout, or inter-service infrastructure (service discovery, config server, gateway, messaging). Confirm these with the user before scaffolding, then update this file with the actual build/test commands and architecture once they exist.

## Notes

- The `.gitignore` ignores `*.jar`, so if a Maven or Gradle wrapper is added, `maven-wrapper.jar` / `gradle-wrapper.jar` will be ignored unless an exception (e.g. `!gradle/wrapper/gradle-wrapper.jar`) is added.
