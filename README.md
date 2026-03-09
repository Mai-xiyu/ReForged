# ReForged

<p align="center">
  <img src="src/main/resources/logo.png" alt="ReForged Logo" width="256"/>
</p>

[🇨🇳 中文版](./README_CN.md) | 🇬🇧 English

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-52.1.10-orange.svg)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/)

A compatibility bridge that enables **NeoForge mods** to run seamlessly on **Minecraft Forge 1.21.1** without any modifications.

## 📖 Overview

ReForged is an innovative runtime adapter that bridges the gap between NeoForge and Forge modloaders. It dynamically loads NeoForge mods and translates their API calls to Forge equivalents using advanced bytecode transformation techniques.

**Author:** Mai_xiyu  
**Version:** 1.0.0  
**License:** All Rights Reserved

## ✨ Key Features

- 🔄 **Zero JAR Modification** — NeoForge mods work without repackaging or rebuilding
- 🚀 **Dynamic Loading** — Runtime discovery and loading of NeoForge mods
- 🔧 **Bytecode Transformation** — ASM-powered translation of NeoForge API calls to Forge
- 🎯 **Event Bus Bridging** — Transparent event system compatibility
- 📦 **Resource Integration** — NeoForge mod assets (textures, models, recipes) automatically available
- ⚙️ **Automatic Configuration** — Seamless conversion of `neoforge.mods.toml` to Forge format
- 🎨 **Comprehensive Patching** — 17+ Mixin patches for edge case compatibility
- 🛠️ **API Shims** — Drop-in replacements for DeferredRegister, CreativeTabs, Attachments, and more

## 🏗️ Technical Architecture

ReForged implements a sophisticated multi-layer compatibility system:

### Loading Pipeline
```
ReForged Initialization
    ↓
Scan mods/ folder for NeoForge JARs (neoforge.mods.toml)
    ↓
For each NeoForge mod:
    • Convert metadata to Forge format
    • Create isolated ClassLoader
    • Transform bytecode with ASM
    • Remap NeoForge API references to shim classes
    • Instantiate mod with bridged event bus
    ↓
Register mod resources as Minecraft resource packs
```

### Core Components

| Component | Purpose |
|-----------|---------|
| **NeoForgeModLoader** | Discovers and instantiates NeoForge mods at runtime |
| **BytecodeRewriter** | ASM-based class transformation engine |
| **ReForgedRemapper** | Rewrites NeoForge class references to Forge equivalents |
| **NeoForgeEventBusAdapter** | Dynamic proxy bridging event bus systems |
| **Shim Layer** | Drop-in API replacements for NeoForge classes |
| **Mixin System** | 17+ patches for Minecraft/Forge compatibility |

## 📦 Installation

1. Install Minecraft **1.21.1** with **Forge 52.1.10** or higher
2. Download ReForged mod JAR
3. Place both ReForged and your NeoForge mods into the `.minecraft/mods/` folder
4. Launch the game — ReForged will automatically detect and load NeoForge mods

**That's it!** No configuration required.

## 🔨 Building from Source

### Prerequisites
- Java 21 or higher
- Git

### Build Commands
```bash
# Clone the repository
git clone https://github.com/Mai-xiyu/ReForged.git
cd ReForged

# Build the mod
./gradlew build

# The compiled JAR will be in build/libs/
```

### Development Commands
```bash
./gradlew runClient        # Launch game client
./gradlew runServer        # Launch dedicated server
./gradlew runData          # Generate data/assets
./gradlew runGameTestServer # Run game tests
```

## 🛠️ How It Works

### 1. **Bytecode Remapping**
ReForged uses ASM (Java bytecode manipulation framework) to rewrite class references:
- `net.neoforged.neoforge.common.NeoForge` → `org.xiyu.reforged.shim.NeoForgeShim`
- `net.neoforged.bus.api.IEventBus` → Custom proxy wrapper
- Event registrations → Forwarded to Forge's event bus

### 2. **Event System Bridge**
When a NeoForge mod registers an event listener:
```java
NeoForge.EVENT_BUS.register(listener);
```
ReForged intercepts this and:
- Analyzes the listener for `@SubscribeEvent` annotations
- Registers the handler on Forge's `MinecraftForge.EVENT_BUS`
- Wraps/unwraps event objects as needed for compatibility

### 3. **Resource Pack Integration**
NeoForge mod JARs are automatically registered as Minecraft resource packs, making their:
- Textures (`assets/`)
- Models
- Recipes (`data/`)
- Tags
- Other data files

...immediately available to the game.

## 📋 System Requirements

- **Minecraft:** 1.21.1
- **Forge:** 52.1.10 or higher
- **Java:** 21 or higher

## 🤝 Compatibility

ReForged aims to provide broad compatibility with NeoForge mods, but some limitations may apply:

- ✅ Most NeoForge API features supported
- ✅ Event systems fully bridged
- ✅ Registry systems (DeferredRegister) compatible
- ✅ Creative tabs and item groups work
- ✅ Network packets handled
- ⚠️ Some advanced NeoForge-exclusive features may not be available
- ⚠️ Mods with deep NeoForge integration may require additional patches

## 📊 Current Progress Snapshot

Latest implementation snapshot, approximate as of 2026-03-09.

| Subsystem | Weight | Completion | Weighted Score |
|-----------|--------|------------|----------------|
| Mod loading pipeline | 20% | 75% | 15.0 |
| Event system | 20% | 72% | 14.4 |
| Registry system | 15% | 80% | 12.0 |
| Capability system | 10% | 85% | 8.5 |
| Network / Payload | 8% | 70% | 5.6 |
| Extension / Common API | 12% | 70% | 8.4 |
| Client side | 10% | 60% | 6.0 |
| Mixin coverage | 5% | 52% | 2.6 |
| **Total** | **100%** |  | **72.5%** |

### Notes

- The percentages above are engineering estimates, not formal test pass rates.
- Recent work mainly improved `Block` / `BlockState` / `Item` / `ItemStack` / `Entity` extension bridges and expanded `EventHooks` coverage.
- The biggest remaining gaps are still `CommonHooks`, client rendering hooks, advanced entity sync, and deeper NeoForge-only vanilla patch behavior.

## 📝 Project Structure

```
ReForged/
├── src/main/java/org/xiyu/reforged/
│   ├── Reforged.java              # Main mod entry point
│   ├── core/                      # Mod loading and ASM transformation
│   ├── shim/                      # API replacement layer
│   ├── bridge/                    # Event and system bridges
│   ├── asm/                       # Advanced bytecode manipulation
│   ├── mixin/                     # Mixin patches for compatibility
│   └── util/                      # Utility classes
├── src/main/resources/
│   ├── META-INF/
│   │   ├── mods.toml             # Forge mod metadata
│   │   └── accesstransformer.cfg # Access widening config
│   ├── reforged.mixins.json      # Mixin configuration
│   └── coremods/                 # JavaScript CoreMod patches
└── build.gradle                   # Build configuration
```

## 🔐 License

All Rights Reserved © 2024 Mai_xiyu

## 🙋 Support

If you encounter issues or have questions:
1. Check if the NeoForge mod is compatible with Forge 1.21.1
2. Verify Java 21 is installed
3. Check the game logs for error messages
4. Open an issue on the GitHub repository

## 🌟 Credits

Developed by **Mai_xiyu**

Special thanks to:
- The Forge team for the Forge modding API
- The NeoForge team for the NeoForge modding API
- The ASM and Mixin communities for bytecode manipulation tools

---

**Note:** This is a community project and is not officially affiliated with or endorsed by the Forge or NeoForge teams.
