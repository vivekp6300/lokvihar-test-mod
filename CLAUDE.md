# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A Fabric mod (`lokvihar-test-mod`) for Minecraft 26.1.2, generated from the Fabric example mod template. It's still mostly template code: example entrypoints and no-op example mixins.

Toolchain: Java 25, Gradle 9.7.1 (wrapper), Fabric Loom 1.18-SNAPSHOT, Fabric Loader 0.19.5, Fabric API 0.155.3+26.1.2. All versions live in `gradle.properties`. Keep them in sync with the `depends` block in `src/main/resources/fabric.mod.json`.

The project is built on Windows, macOS and CI. Never commit machine-specific values such as `org.gradle.java.home` or absolute paths. The Java version is pinned in `gradle/gradle-daemon-jvm.properties` (`toolchainVersion`). Per-machine overrides belong in `~/.gradle/gradle.properties`; see README "Per-machine settings".

## Commands

- `./gradlew build`: compile and produce the mod jar plus sources jar in `build/libs/`
- `./gradlew runClient`: launch a dev Minecraft client with the mod loaded
- `./gradlew runServer`: launch a dev dedicated server
- `./gradlew genSources`: decompile Minecraft sources, useful for looking up methods to target with mixins

There are no tests and no linter configured.

## Architecture

- **Split source sets** (`loom.splitEnvironmentSourceSets()`):
  - `src/main` holds common/server-safe code.
  - `src/client` holds client-only code, which can reference `main` but not the other way around.
  - Never import `net.minecraft.client.*` from `src/main`.
- **Entrypoints** are declared in `fabric.mod.json`:
  - `main` → `com.vivek.minecraft.LokViharTestMod` (`ModInitializer`)
  - `client` → `com.vivek.minecraft.client.LokViharTestModClient` (`ClientModInitializer`)
  - `LokViharTestMod` also provides `MOD_ID`, the shared `LOGGER`, and an `id(path)` helper for namespaced `Identifier`s.
- **Mixins** are registered in two configs:
  - `lokvihar-test-mod.mixins.json` covers package `com.vivek.minecraft.mixin`.
  - `lokvihar-test-mod.client.mixins.json` covers package `com.vivek.minecraft.client.mixin` and is client-only.
  - New mixin classes must be added to the matching JSON file's list (`mixins` or `client`), or they won't load.
  - `injectors.defaultRequire: 1` means an injection that fails to match its target crashes at load.
- **Unobfuscated Minecraft**: MC 26.x ships without obfuscation, so this build uses the non-remapping `net.fabricmc.fabric-loom` plugin with plain `implementation` dependencies (not `modImplementation`). Code uses Mojang's official names, e.g. `Identifier.fromNamespaceAndPath`, `net.minecraft.client.Minecraft`. Don't use Yarn mappings or names from older-version tutorials.
- `processResources` substitutes `${version}` in `fabric.mod.json` from `gradle.properties`.
- Assets go under `src/main/resources/assets/lokvihar-test-mod/`.
