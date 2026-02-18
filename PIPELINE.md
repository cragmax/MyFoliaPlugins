# MyFoliaPlugins — Build & Deploy Pipeline

Folia 1.21 · Java 21 · Gradle 9.2 · Kotlin DSL

---

## Quick Start (New Dev Setup)

1. Clone the repo
2. Copy `gradle.properties.template` to `gradle.properties`
3. Fill in your local values (server path, RCON password, etc.)
4. Run `./gradlew :RegionInfoPlugin:build` to verify everything compiles

---

## Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew :PluginName:build` | Build a single plugin |
| `./gradlew build` | Build all plugins |
| `./gradlew :PluginName:deploy` | Deploy a single plugin |
| `./gradlew deployAll` | Deploy all plugins (single server restart) |

Deploy tasks are only active when `isDev=true` in `gradle.properties`. They show as SKIPPED otherwise.

---

## Project Structure

```
MyFoliaPlugins/
  buildSrc/
    src/main/kotlin/
      DeployProperties.kt           ← property resolution from gradle.properties
      DeployUtils.kt                ← jar copy + start.bat generation
      ServerUtils.kt                ← RCON + process management
      deploy-convention.gradle.kts  ← registers deploy task for each plugin
    build.gradle.kts
  scripts/
    start.bat                       ← server start template (tokens substituted on deploy)
  RegionInfoPlugin/
    src/main/java/com/cragmax/regioninfo/
    src/main/resources/plugin.yml
  build.gradle.kts                  ← shared config + deployAll task
  settings.gradle.kts
  gradle.properties                 ← local dev config (gitignored)
  gradle.properties.template        ← commit this, not gradle.properties
```

---

## gradle.properties

Copy `gradle.properties.template` to `gradle.properties` and fill in your values. This file is gitignored — never commit it.

| Property | Description | Example |
|----------|-------------|---------|
| `isDev` | Enables deploy tasks | `true` |
| `serverDir` | Path to server folder | `C:/MinecraftServers` |
| `serverPluginsPath` | Path to plugins folder | `C:/MinecraftServers/plugins` |
| `serverJar` | Server jar name | `folia.jar` |
| `serverMinMemory` | Min JVM heap | `4G` |
| `serverMaxMemory` | Max JVM heap | `8G` |
| `rconPassword` | RCON password from server.properties | `yourpassword` |
| `mcPort` | Minecraft port | `25565` |
| `rconPort` | RCON port | `25575` |

Use forward slashes in paths to avoid escaping issues:
```
serverDir=C:/MinecraftServers
serverPluginsPath=C:/MinecraftServers/plugins
```

---

## Deploy Flow

### Single Plugin (`./gradlew :PluginName:deploy`)

1. Check `isDev=true` — skip if not
2. Resolve all properties from `gradle.properties`
3. Stop server via RCON (skip gracefully if not running)
4. Poll port 25565 until server is down (3s interval, 60s timeout)
5. Delete old plugin jar from plugins folder (wildcard match handles version changes)
6. Copy new jar to plugins folder
7. Generate `start.bat` in server folder from `scripts/start.bat` template
8. Start server in new terminal window

### Deploy All (`./gradlew deployAll`)

Same as above but server stops once, all jars are copied in one pass, server starts once.

---

## buildSrc Files

### `DeployProperties.kt`
Central property resolution. Calls `DeployProperties.from(project)` to get a typed data class with all values. Fails fast with a clear message if any required property is missing. `isDev` is optional and defaults to false.

### `DeployUtils.kt`
High-level orchestration used by both `deploy` and `deployAll`:
- `deployJar()` — deletes old jar (wildcard), copies new jar, logs the exact filename deployed
- `copyStartBat()` — copies `scripts/start.bat` to server folder with tokens substituted. Warns and continues if template is missing.

### `ServerUtils.kt`
Low-level RCON and process management:
- `isServerRunning()` — socket connection to port 25565
- `stopServer()` — RCON stop + port polling
- `startServer()` — launches server in new terminal window (`cmd /k` keeps window open on stop)

### `deploy-convention.gradle.kts`
Convention plugin applied to every subproject via the root `build.gradle.kts` `subprojects {}` block. Registers the `deploy` task. Properties are resolved lazily inside `doLast` so missing `gradle.properties` entries don't break unrelated tasks like `build`.

---

## start.bat Template

`scripts/start.bat` is a template committed to source. On every deploy, Gradle generates a copy in the server folder with tokens replaced:

| Token | Replaced With |
|-------|---------------|
| `@SERVER_JAR@` | `serverJar` from gradle.properties |
| `@SERVER_MIN_MEMORY@` | `serverMinMemory` from gradle.properties |
| `@SERVER_MAX_MEMORY@` | `serverMaxMemory` from gradle.properties |

The generated `start.bat` in the server folder can be run manually:
```
start.bat           # uses baked-in defaults
start.bat 2G 8G    # overrides memory settings
```

Never edit the generated file directly — it will be overwritten on next deploy.

---

## Adding a New Plugin

1. Create a new folder e.g. `MyNewPlugin/`
2. Add `src/main/java/...` and `src/main/resources/plugin.yml`
3. Add `include("MyNewPlugin")` to `settings.gradle.kts`
4. No `build.gradle.kts` needed — all config is inherited

The new plugin automatically gets Java 21 toolchain, Paper API dependency, versioned jar naming, `:MyNewPlugin:build`, `:MyNewPlugin:deploy`, and inclusion in `deployAll`.

---

## Jar Naming

Jars are versioned with the current git branch name. Forward slashes in branch names are replaced with hyphens to avoid path separator issues:

```
RegionInfoPlugin-1.0-SNAPSHOT-main.jar
RegionInfoPlugin-1.0-SNAPSHOT-feature-my-thing.jar  ← branch: feature/my-thing
```

---

## Technology Stack

| | |
|--|--|
| Java | 21 |
| Gradle | 9.2 with Kotlin DSL |
| Paper API | 1.21.11-R0.1-SNAPSHOT (Folia fork) |
| RCON client | nl.vv32.rcon:rcon:1.2.0 |
| Server | Folia 1.21 |
