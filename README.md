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

The files in steps 4 to 8 are written with `cat > file <<'EOF'`, so each step can be pasted straight into a terminal. Like Spring Initializr's output, the Gradle and Java files are indented with tabs.

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

## Step 3: Upgrade the Gradle wrapper to 9.8.2

```bash
./gradlew wrapper --gradle-version 9.8.2
./gradlew wrapper
```

The first command, run by Gradle 9.4.1, points `gradle/wrapper/gradle-wrapper.properties` at 9.8.2. The second runs with 9.8.2 and regenerates `gradlew`, `gradlew.bat` and `gradle-wrapper.jar` from that version, so they match what Spring Initializr ships.

Check it:

```bash
./gradlew --version
```

## Step 4: Make it a Spring Boot build

Replace `settings.gradle.kts`, removing the generated comment:

```bash
cat > settings.gradle.kts <<'EOF'
rootProject.name = "service"
EOF
```

Replace `build.gradle.kts`:

```bash
cat > build.gradle.kts <<'EOF'
plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.fbxmuz"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
EOF
```

- `org.springframework.boot` adds the `bootRun` and `bootJar` tasks and packages the app as an executable jar.
- `io.spring.dependency-management` imports Spring Boot's BOM, which is why the starters have no versions.
- `spring-boot-starter-webmvc` is Spring Web: Spring MVC plus embedded Tomcat. In Spring Boot 4 it replaces the deprecated `spring-boot-starter-web`.
- `junit-platform-launcher` lets Gradle run JUnit 5 tests.

## Step 5: Add the application class and configuration

```bash
mkdir -p src/main/java/com/fbxmuz/service src/main/resources

cat > src/main/java/com/fbxmuz/service/ServiceApplication.java <<'EOF'
package com.fbxmuz.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}
EOF

cat > src/main/resources/application.properties <<'EOF'
spring.application.name=service
EOF
```

`@SpringBootApplication` turns on auto-configuration and component scanning for `com.fbxmuz.service` and its subpackages, so new classes such as controllers go in that package or below it.

## Step 6: Add the static and templates directories

```bash
mkdir -p src/main/resources/static src/main/resources/templates
touch src/main/resources/static/.gitkeep src/main/resources/templates/.gitkeep
```

Spring Boot serves files in `static/` as web content, and `templates/` holds view templates. Both start empty; Git doesn't track empty directories, so each gets an empty `.gitkeep` file.

## Step 7: Add the test

```bash
mkdir -p src/test/java/com/fbxmuz/service

cat > src/test/java/com/fbxmuz/service/ServiceApplicationTests.java <<'EOF'
package com.fbxmuz.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
EOF
```

The test passes if the Spring application context starts.

## Step 8: Update `.gitignore`

```bash
cat > .gitignore <<'EOF'
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

### Kotlin ###
.kotlin

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

### VS Code ###
.vscode/
EOF
```

This is Spring Initializr's `.gitignore`, plus `.kotlin` from the one `gradle init` generated. It ignores the build output of Gradle (`build/`) and of each IDE, including Eclipse's `bin/` directory and project files (`.project`, `.classpath`, `.settings`). Initializr's `HELP.md` line is left out because this project has no `HELP.md`.

## Step 9: Check the result

Build and run the tests:

```bash
./gradlew build
```

Start the application:

```bash
./gradlew bootRun
```

The log ends with `Tomcat started on port 8080` and the application keeps running. In a second terminal:

```bash
curl -i http://localhost:8080/
```

It returns `404 Not Found`, because no endpoint exists yet; the response shows the web server is up. Stop the application with Ctrl+C.

The project is now a Spring Initializr project with Spring Web, created without start.spring.io. Its files:

```
microservice/
├── .gitattributes
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/fbxmuz/service/ServiceApplication.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/.gitkeep
    │       └── templates/.gitkeep
    └── test/java/com/fbxmuz/service/ServiceApplicationTests.java
```

Compared with a project downloaded from start.spring.io, the differences are the `.gitkeep` files, the extra `.kotlin` line in `.gitignore`, the comments in `.gitattributes`, and no `HELP.md`.

## Step 10: Add a hello world endpoint

Add a controller:

```bash
cat > src/main/java/com/fbxmuz/service/HelloController.java <<'EOF'
package com.fbxmuz.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello(@RequestParam(defaultValue = "World") String name) {
		return "Hello, " + name + "!";
	}

}
EOF
```

- `@RestController` makes Spring create this class at startup (component scanning finds it because it is in `com.fbxmuz.service`) and write each method's return value straight into the response body.
- `@GetMapping("/hello")` maps `GET /hello` to the method.
- `@RequestParam(defaultValue = "World")` reads the optional `name` query parameter. Spring finds the parameter by its Java name, which works because the Spring Boot Gradle plugin compiles with `-parameters`.

Add a test for it:

```bash
cat > src/test/java/com/fbxmuz/service/HelloControllerTests.java <<'EOF'
package com.fbxmuz.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloController.class)
class HelloControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void greetsWorldByDefault() throws Exception {
		mockMvc.perform(get("/hello"))
			.andExpect(status().isOk())
			.andExpect(content().string("Hello, World!"));
	}

	@Test
	void greetsByName() throws Exception {
		mockMvc.perform(get("/hello").param("name", "Ada"))
			.andExpect(status().isOk())
			.andExpect(content().string("Hello, Ada!"));
	}

}
EOF
```

`@WebMvcTest` starts only the web layer for `HelloController`, without Tomcat, and `MockMvc` sends requests to it in memory. It comes from `spring-boot-starter-webmvc-test`; in Spring Boot 4 its package is `org.springframework.boot.webmvc.test.autoconfigure`.

Run the tests:

```bash
./gradlew test
```

Start the application:

```bash
./gradlew bootRun
```

In a second terminal:

```bash
curl http://localhost:8080/hello
curl "http://localhost:8080/hello?name=Ada"
```

They print `Hello, World!` and `Hello, Ada!`. Stop the application with Ctrl+C.
