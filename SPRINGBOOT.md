# Creating a Spring Boot Microservice with `gradle init`

This guide sets up this repository as a Gradle multi-project build where each microservice is a subproject. It starts with one service, `greeting-service`. Further services are added the same way (see step 9).

Every command and file below was tested with:

| Tool        | Version |
|-------------|---------|
| Java (JDK)  | 21      |
| Gradle      | 9.4.1   |
| Spring Boot | 4.1.2   |

Run all commands from the repository root:

```bash
cd ~/dev/javawork/springboot-microservices
```

---

## Step 1: Check the prerequisites

```bash
java -version      # needs 17 or newer for Spring Boot 4; this guide uses 21
gradle --version   # any recent Gradle; it is only used once, to generate the wrapper
```

After step 2 you'll use the generated `./gradlew` wrapper, not the `gradle` you have installed.

## Step 2: Generate the build with `gradle init`

```bash
gradle init \
  --type java-application \
  --dsl kotlin \
  --test-framework junit-jupiter \
  --project-name springboot-microservices \
  --package com.example.microservices \
  --java-version 21 \
  --no-split-project \
  --no-incubating \
  --overwrite \
  --use-defaults
```

What the options do:

| Option | Purpose |
|--------|---------|
| `--type java-application` | Creates a root build plus one runnable Java subproject called `app` |
| `--dsl kotlin` | Build scripts are written as `*.gradle.kts` (use `groovy` if you prefer `*.gradle`) |
| `--test-framework junit-jupiter` | JUnit 5+, which Spring Boot's test starters use |
| `--package` | Base Java package for the generated sources |
| `--java-version 21` | Sets the Java toolchain version |
| `--no-split-project` | One application subproject, without the extra `buildSrc`/library modules |
| `--overwrite` | Required because the directory already has `README.md`, `LICENSE` and `.gitignore` |
| `--use-defaults` | Uses defaults for anything not set, so `init` asks no questions |

You can leave out the options and run plain `gradle init` to answer the same questions interactively.

Generated files:

```
settings.gradle.kts            # root settings: project name + include("app")
gradle.properties              # enables the configuration cache
gradle/libs.versions.toml      # version catalog (dependency versions)
gradle/wrapper/                # Gradle wrapper jar + properties
gradlew, gradlew.bat           # wrapper scripts; use these from now on
app/build.gradle.kts           # the subproject's build script
app/src/main/java/...App.java
app/src/test/java/...AppTest.java
```

`gradle init` also adds `.gradle`, `build` and `.kotlin` to `.gitignore`.

## Step 3: Stop git from ignoring the wrapper jar

This repo's `.gitignore` has a `*.jar` rule, which also ignores `gradle/wrapper/gradle-wrapper.jar`. The wrapper doesn't work without that file, so add an exception:

```bash
echo '!gradle/wrapper/gradle-wrapper.jar' >> .gitignore
git check-ignore -v gradle/wrapper/gradle-wrapper.jar   # should print nothing now
```

## Step 4: Rename `app` to the service name

```bash
mv app greeting-service
sed -i 's/include("app")/include("greeting-service")/' settings.gradle.kts
rm greeting-service/src/main/java/com/example/microservices/App.java \
   greeting-service/src/test/java/com/example/microservices/AppTest.java
mkdir -p greeting-service/src/main/java/com/example/microservices/greeting \
         greeting-service/src/test/java/com/example/microservices/greeting \
         greeting-service/src/main/resources
```

Each service gets its own sub-package (`com.example.microservices.greeting`), so the services' classes don't clash.

## Step 5: Add Spring Boot to the version catalog

Replace the contents of `gradle/libs.versions.toml` (the generated Guava and JUnit entries aren't needed, because Spring Boot manages those versions):

```toml
[versions]
spring-boot = "4.1.2"
spring-dependency-management = "1.1.7"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "spring-dependency-management" }
```

To upgrade Spring Boot for every service later, change this one version.

## Step 6: Change the service's build script

Replace `greeting-service/build.gradle.kts`:

```kotlin
plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example.microservices"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

Notes:
- The Spring Boot plugin adds the `bootRun` and `bootJar` tasks, so the generated `application` plugin isn't needed.
- The dependency-management plugin imports Spring Boot's BOM, so starters are declared without versions.
- In Spring Boot 4 the web starter is `spring-boot-starter-webmvc`, and each starter has a matching `-test` starter. The old `spring-boot-starter-web` still works but is deprecated.

## Step 7: Add the application code

`greeting-service/src/main/java/com/example/microservices/greeting/GreetingServiceApplication.java`

```java
package com.example.microservices.greeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GreetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingServiceApplication.class, args);
    }
}
```

`greeting-service/src/main/java/com/example/microservices/greeting/GreetingController.java`

```java
package com.example.microservices.greeting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(defaultValue = "World") String name) {
        return new Greeting("Hello, " + name + "!");
    }

    public record Greeting(String message) {}
}
```

`greeting-service/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: greeting-service

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Give each service its own port (8081, 8082, ...) so you can run several at the same time.

`greeting-service/src/test/java/com/example/microservices/greeting/GreetingControllerTest.java`

```java
package com.example.microservices.greeting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GreetingController.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsDefaultGreeting() throws Exception {
        mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"));
    }

    @Test
    void greetsByName() throws Exception {
        mockMvc.perform(get("/greeting").param("name", "Gradle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Gradle!"));
    }
}
```

In Spring Boot 4, `@WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`. Older tutorials use a different package that no longer exists.

## Step 8: Build, test and run

```bash
./gradlew build                                   # compile, test and package every service
./gradlew :greeting-service:test                  # test one service
./gradlew :greeting-service:test --tests '*GreetingControllerTest'              # one test class
./gradlew :greeting-service:test --tests '*GreetingControllerTest.greetsByName' # one test method
./gradlew :greeting-service:bootRun               # run the service (Ctrl+C to stop)
```

With the service running, from another terminal:

```bash
curl localhost:8081/greeting?name=David
# {"message":"Hello, David!"}

curl localhost:8081/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Run the packaged jar directly:

```bash
java -jar greeting-service/build/libs/greeting-service-0.0.1-SNAPSHOT.jar
```

(The `-plain.jar` next to it has only the service's own classes, without dependencies, and can't be run on its own.)

## Step 9: Add another microservice

Repeat steps 4–7 for the new service:

1. Create `<name>-service/` with the same `src/main/java`, `src/test/java` and `src/main/resources` layout, in its own package (for example `com.example.microservices.<name>`).
2. Add it to `settings.gradle.kts`: `include("greeting-service", "<name>-service")`.
3. Copy `greeting-service/build.gradle.kts` and change the dependencies as needed.
4. Give it its own `spring.application.name` and `server.port` in `application.yml`.

Once there are several services, consider moving the shared parts of `build.gradle.kts` (plugins, toolchain, repositories, test setup) into a convention plugin under `buildSrc/` so each service's build file only lists its own dependencies.

## Step 10: Commit

```bash
git add .
git status          # check that gradle/wrapper/gradle-wrapper.jar is listed and build/ and .gradle/ are not
git commit -m "Set up Gradle multi-project build with greeting-service"
```
