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
