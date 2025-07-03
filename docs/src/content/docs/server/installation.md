---
title: Installation
description: Learn how to install and setup Bestium on your server.
---

## Install Bestium

To install Bestium on your PaperMC server:
1. Download the plugin JAR from the [latest release](https://github.com/huzvanec/Bestium/releases/latest) on GitHub.
2. Make sure your server version matches the one listed in the release title.
   For example, a release titled `Bestium 1.0.0 for Paper 1.21.6` will only work on PaperMC 1.21.6.
3. Drop the downloaded JAR into your server's `plugins/` folder.
4. Restart your server.

Done!

## Install Bestium Example

Bestium is a library plugin, which means it doesn't add anything to the game by itself.
It provides tools that other plugins can use to register and spawn custom entities.

If you don't have a plugin using Bestium yet, you can try the [Bestium Example plugin](https://github.com/huzvanec/BestiumExample):
1. Download the plugin JAR from the [latest release](https://github.com/huzvanec/BestiumExample/releases/latest).
2. Drop the downloaded JAR into your server's `plugin/` folder, right next to the Bestium JAR.
3. Restart your server again.

## Verifying

If you installed Bestium with a plugin that depends on it, you should see Bestium doing some work in your console:
```
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Starting entity injection (bootstrap phase)
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Fetching DataFixer entity types
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Retrieving default attribute suppliers
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Resetting unregistered intrusive entity type holders
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Unfreezing entity type registry
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Injecting 1 entity
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Overwriting default attribute suppliers
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Refreezing entity type registry
[ServerMain/INFO]: [BestiumInjectionUnit] [Phase 1/2] Injection complete
(...)
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Starting injection (load phase)
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Fetching Bukkit entity type registry internals
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Injecting 1 entity into Bukkit entity registry
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Retrieving Bukkit entity type data
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Injecting 1 entity into Bukkit entity type data
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Overwriting Bukkit entity type data
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Overwriting Bukkit entity type registry internals
[Server thread/INFO]: [BestiumInjectionUnit] [Phase 2/2] Injection complete
```
If no errors appear, that means that the injection was successful and you can start summonning custom entities.