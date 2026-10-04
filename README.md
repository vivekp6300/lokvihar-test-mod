# LokVihar Test Mod

A small [Fabric](https://fabricmc.net/) mod for **Minecraft 26.1.2**, used for learning and experimenting. It currently adds one item, the **Ruby** (crafted from 8 iron nuggets around 1 red dye), and keeps the Fabric template's example mixins as reference.

The source is documented heavily on purpose, so it can be read as a tutorial. Good places to start:

- [`LokViharTestMod.java`](src/main/java/com/vivek/minecraft/LokViharTestMod.java): how the mod starts up
- [`ModItems.java`](src/main/java/com/vivek/minecraft/item/ModItems.java): how an item is added
- [`ExampleMixin.java`](src/main/java/com/vivek/minecraft/mixin/ExampleMixin.java): how mixins work
- [`docs/resources.md`](docs/resources.md): what every JSON resource file does

## Requirements

| Tool | Version | Notes |
|---|---|---|
| JDK | **25** | Required by Minecraft 26.x. Any vendor works, e.g. [Eclipse Temurin](https://adoptium.net/). |
| Gradle | 9.7.1 | **Don't install it.** The included wrapper (`gradlew` / `gradlew.bat`) downloads the right version. |
| Git | any | |

All other versions (Minecraft, Fabric Loader, Fabric API, Loom) are pinned in [`gradle.properties`](gradle.properties) and downloaded automatically.

## Getting started

```sh
git clone <this repo>
cd lokvihar-test-mod

./gradlew build        # macOS / Linux / Git Bash
gradlew.bat build      # Windows cmd / PowerShell
```

The first build downloads Minecraft and the dependencies, which takes a few minutes. Later builds are fast.

| Command | What it does |
|---|---|
| `./gradlew build` | Compiles the mod and writes the jar to `build/libs/lokvihar-test-mod-<version>.jar` |
| `./gradlew runClient` | Launches Minecraft with the mod loaded (dev world data lives in `run/`) |
| `./gradlew runServer` | Launches a dedicated server with the mod loaded |
| `./gradlew genSources` | Decompiles Minecraft so you can browse its source in your IDE |

To try the mod in game, run `runClient`, open a creative world and look in the **Ingredients** tab. You can also use `/give @s lokvihar-test-mod:ruby`.

To install it in a normal Minecraft setup, put the built jar in the `mods/` folder of a Fabric 26.1.2 installation, along with the [Fabric API](https://modrinth.com/mod/fabric-api) mod.

## Per-machine settings

> **⚠ Never commit machine-specific values** (JDK paths, memory sizes for a particular PC, credentials) to this repo. The project builds on Windows, macOS and CI, and a path that works on one breaks the others.

The repo is split so that each machine needs **as little local setup as possible, usually none**:

| What | Where | In git? |
|---|---|---|
| Versions, build flags, *which* Java version | [`gradle.properties`](gradle.properties), [`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties) | ✅ yes, shared |
| *Where* things are on this machine | your Gradle user file (see below) | ❌ no, lives outside the repo |

### How Java is picked

[`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties) says `toolchainVersion=25`. When you run `./gradlew`, Gradle searches for an installed JDK 25 and runs the build on it, even if your `JAVA_HOME` points at a different Java. It checks the usual install locations on every OS (Windows registry, `/Library/Java/JavaVirtualMachines`, `/usr/lib/jvm`, SDKMAN, asdf, IntelliJ-downloaded JDKs).

So on most machines, **installing JDK 25 is all the setup there is.**

`gradlew` itself still needs *some* Java to start (any version Gradle 9 supports, i.e. 17 or newer) on your `PATH` or in `JAVA_HOME`. If JDK 25 is your only Java, that's covered too.

### The per-machine Gradle file (only if needed)

Gradle reads a personal `gradle.properties` from your Gradle user home before the project's file, and **values there take priority**. It's outside the repo, so nothing in it can be committed by accident.

| OS | Location |
|---|---|
| Windows | `%USERPROFILE%\.gradle\gradle.properties` (e.g. `C:\Users\you\.gradle\gradle.properties`) |
| macOS / Linux | `~/.gradle/gradle.properties` |

Create it only if one of these applies:

```properties
# Gradle can't find your JDK 25 (installed in an unusual place).
# Comma-separate several paths. On Windows, use forward slashes.
org.gradle.java.installations.paths=C:/tools/jdk-25

# This machine has little or lots of RAM (the repo default is -Xmx1G).
org.gradle.jvmargs=-Xmx3G
```

Settings in this file apply to **every** Gradle project on that machine, so put only machine-wide facts there.

> **Avoid `org.gradle.java.home`.** It hard-wires one JDK path for all your projects and overrides the automatic version selection described above. `org.gradle.java.installations.paths` just tells Gradle where to look.

### Checklist by environment

- **Windows**: install JDK 25 (the Temurin `.msi` registers itself, so Gradle finds it). Use `gradlew.bat` or run `./gradlew` from Git Bash.
- **macOS**: install JDK 25 (Temurin `.pkg`, or `brew install --cask temurin@25`). It goes into `/Library/Java/JavaVirtualMachines`, which Gradle scans.
- **Linux**: install JDK 25 with your package manager or SDKMAN (`sdk install java 25-tem`).
- **GitHub Actions**: install JDK 25 in the workflow. No per-machine file is needed:
  ```yaml
  - uses: actions/setup-java@v4
    with:
      distribution: temurin
      java-version: 25
  - uses: gradle/actions/setup-gradle@v4
  - run: ./gradlew build
  ```
- **GitHub Codespaces / dev containers**: install JDK 25 in the container, e.g. with the `ghcr.io/devcontainers/features/java` feature (`"version": "25"`). Then `./gradlew runServer` works, but `runClient` needs a desktop, so it won't run in a headless container.

## Project layout

```
src/main/      common code: runs on both client and server
src/client/    client-only code (rendering, input); may use src/main, never the reverse
src/main/resources/
  fabric.mod.json                    mod manifest: ID, entrypoints, dependencies
  lokvihar-test-mod.mixins.json      common mixin config
  assets/lokvihar-test-mod/          client resources: textures, models, translations
  data/lokvihar-test-mod/            server data: recipes
src/client/resources/
  lokvihar-test-mod.client.mixins.json   client-only mixin config
docs/          extra documentation
```

Minecraft 26.x ships **unobfuscated**, so this project uses Mojang's official names (`Identifier`, `net.minecraft.client.Minecraft`, ...). Tutorials for older versions that use Yarn names (`MinecraftClient`, `modImplementation`) won't match.

## Upgrading Minecraft

1. Get the matching versions from <https://fabricmc.net/develop>.
2. Update `minecraft_version`, `loader_version`, `loom_version` and `fabric_api_version` in [`gradle.properties`](gradle.properties).
3. Update the `depends` block in [`fabric.mod.json`](src/main/resources/fabric.mod.json) to match.
4. If the required Java version changed, update `toolchainVersion` in [`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties), `options.release` in [`build.gradle`](build.gradle), and `"java"` in `fabric.mod.json`.
5. Run `./gradlew build runClient`. Mixins are set to crash at startup if their target method no longer exists, so broken ones show up right away.

## License

CC0-1.0. See [LICENSE](LICENSE).
