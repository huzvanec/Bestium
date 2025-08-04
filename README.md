<div align="center">
  <img src="branding/capybara.png" alt="Capybara" width="40%" />
  <h1>Bestium</h1>
  A powerful plugin library for creating custom Minecraft entities with unique behaviors.
  <br/>
  <br/>
  <a href="https://docs.bestium.jeme.cz">
    <img src="branding/docs.png" alt="Docs" width="15%" />
  </a>
</div>
<br/>

Unlike traditional methods, Bestium injects your entity code directly into the Minecraft and Bukkit runtimes, it allows
you to create custom entities without extending non-abstract vanilla entities, meaning you're not bound by their
existing implementations.

- [x] Inject custom entity code into the Minecraft and Bukkit runtimes
- [x] Preserve entities over server restarts and chunk unloads
- [x] Craft a nice API with full javadoc
- [x] Integrate [BetterModel](https://github.com/toxicity188/BetterModel) (❤️
  to [toxicity188](https://github.com/toxicity188)) to add custom models to entities
- [x] Datapack biome support (entities can be added to biomes naturally)
- [x] In-game summon command
- [x] Mob spawn egg support
- [ ] Mob spawner support
- [ ] In-game GUI with spawn eggs
- [ ] Config

> [!WARNING]
> Depending on what you desire, Bestium may be unstable.

> [!CAUTION]
> Due to frequent changes in Minecraft's internal codebase, Bestium only supports the latest Minecraft release.

## Usage

Check out the [Documentation](https://docs.bestium.jeme.cz) and [Javadoc](https://jd.bestium.jeme.cz)
for more information.

## The Patching System

The [patching system](core/src/main/kotlin/cz/jeme/bestium/inject/patch) is heavily inspired by and partially copied
from the patching system of the [Nova plugin](https://github.com/xenondevs/Nova) (licensed under [LGPL](https://github.com/xenondevs/Nova/blob/db8a2fef8e0b1258ad0a0df940238a092614ecca/LICENSE)).

This work would not have been possible without the incredible effort [xenondevs](https://github.com/xenondevs) put into
the Nova framework. Please check
out:

- [Nova Plugin](https://github.com/xenondevs/Nova)
- [ByteBase (ASM library)](https://github.com/ByteZ1337/ByteBase)
- [ByteZ's blog post on runtime patching](https://blog.xenondevs.xyz/2023/03/03/runtime-patching-in-nova/)
- [Xenondevs GitHub profile](https://github.com/xenondevs)

Bestium is built with full Nova compatibility in mind.

## Building

Requirements:

- [Git](https://git-scm.com/downloads)
- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)

```bash
git clone https://github.com/huzvanec/Bestium.git
cd Bestium/
./gradlew build
```

Complete JAR is now located in `./build/libs/`.  
API JAR is now located in `./api/build/libs/`.
