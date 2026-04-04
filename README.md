# ReForged

<p align="center">
  <img src="src/main/resources/logo.png" alt="ReForged Logo" width="256"/>
</p>

[🇨🇳 中文版](./README_CN.md) | 🇬🇧 English

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-51.0.33-orange.svg)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/)

A compatibility bridge that enables **NeoForge mods** to run seamlessly on **Minecraft Forge 1.21** without any modifications.

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
- 🎨 **Comprehensive Patching** — 91 Mixin patches for edge case compatibility
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
| **Mixin System** | 91 patches for Minecraft/Forge compatibility |

## 📦 Installation

1. Install Minecraft **1.21** with **Forge 51.0.33** or higher
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

- **Minecraft:** 1.21
- **Forge:** 51.0.33 or higher
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

### 🎯 Mod Scale & Type Compatibility Guide

The table below outlines the expected compatibility for different types and scales of NeoForge mods on ReForged.

| Mod Type | Typical Examples | Expected Compat | Notes |
|----------|-----------------|-----------------|-------|
| **Item / Block mods** | New ores, decorations, tools & weapons | ✅ Excellent | `DeferredRegister`, CreativeTabs, item properties, food components are fully bridged |
| **Worldgen mods** | Custom ore veins, structures, biome modifiers | ✅ Good | `BiomeModifier`/`StructureModifier` framework implemented; datapack-driven generation works |
| **Recipe / Crafting extensions** | Custom recipe types, conditional recipes | ✅ Good | `ICondition` system and custom `RecipeSerializer` available |
| **Capability / Attachment mods** | Energy, fluid, item storage | ✅ Good | `IEnergyStorage`/`IFluidHandler`/`IItemHandler` fully implemented; `AttachmentType` bridges to Forge Capability |
| **Network / Payload mods** | Custom payload communication | ✅ Good | `PayloadRegistrar` registration and bidirectional `reply()` implemented |
| **Client rendering mods** | Custom models, particles, HUD overlays | ⚠️ Partial | Basic model loading (OBJ/JSON), `RenderType` registration, GUI events available; deep BakedModel transforms and custom shaders may need adaptation |
| **Info / Tooltip mods** | Jade, WTHIT, JEI plugins | ⚠️ Partial | Depends on how deeply the mod relies on NeoForge extension interfaces; Jade has a dedicated Mixin patch |
| **Large content mods** | Mekanism, Create, etc. | ⚠️ Partial | DataMap, BiomeModifier codecs, attribute modifier events, RenderBuffers injection, DimensionSpecialEffects injection, GUI layer ordering, Flywheel GPU rendering pipeline all implemented; most core features run, some edge paths may need extra patches |
| **Core / Low-level mods** | Custom ModLoader extensions, ServiceLoader overrides | ❌ Unsupported | Mods that manipulate FML internals or NeoForge bootstrap stages cannot be shimmed |

**Scale Reference:**

- **Small mods** (< 50 classes): Only use `DeferredRegister`, event listeners, simple Capabilities → **most will run out of the box**.
- **Medium mods** (50–300 classes): Include custom networking, client rendering, datagen, conditional recipes → **core features mostly work**, some advanced features may need adaptation.
- **Large mods** (300+ classes): Deep use of DataMaps, custom HolderSets, multiblock entity sync, complex render pipelines → **case-by-case evaluation needed**, some functionality may be missing.

> **Rule of thumb:** If a NeoForge mod's core functionality only relies on the registry system + event bus + basic Capabilities (item/energy/fluid), it will most likely work on ReForged. The heavier a mod's reliance on NeoForge-exclusive deep vanilla patches, the higher the compatibility risk.

## 📊 Current Progress Snapshot

Latest implementation snapshot, approximate as of 2026-04-05.

| Subsystem | Weight | Completion | Weighted Score |
|-----------|--------|------------|----------------|
| Mod loading pipeline | 20% | 85% | 17.0 |
| Event system | 20% | 98% | 19.6 |
| Registry system | 15% | 95% | 14.25 |
| Capability system | 10% | 95% | 9.5 |
| Network / Payload | 8% | 82% | 6.56 |
| Extension / Common API | 12% | 96% | 11.52 |
| Client side | 10% | 97% | 9.7 |
| Mixin coverage | 5% | 95% | 4.75 |
| **Total** | **100%** |  | **~93%** |

### Recent Changes (03-09 → 04-05)

#### Phase 1 (03-09 → 03-10): Core Event & API Framework
- **Event system (major)**: Added **60 Forge wrapper constructors** enabling automatic event bridging via `NeoForgeEventBusAdapter`. Covers server lifecycle, entity, living, player, level, village, brewing, enchanting, and grindstone events.
- **ClientHooks**: Expanded from 18 → **108 methods** with full `ForgeHooksClient` delegation.
- **EventHooks / CommonHooks**: Added ~27 missing methods.
- **Client + Common event wrappers (~60)**: Full coverage for rendering, input, lifecycle, entity, chunk events.
- **Registry system**: Implemented DataMap system, HolderSetType codecs, DeferredHolder tag resolution.

#### Phase 2–4 (03-10 → 03-28): Deep Compatibility & Create/Twilight Forest Support
- **EntityEvent.Size**: Added `pose`/`oldSize`/`newSize`/`newEyeHeight` fields with getters/setters.
- **GuiGraphics 9-slice rendering**: New `GuiGraphicsExtensionMixin` implementing `blitWithBorder()` (9-slice) and `blitInscribed()` (aspect-ratio preserving).
- **CommonHooks.extractLookupProvider**: Multi-field-name retry (3 mapping names) + server fallback.
- **CommonHooks.tryDispenseShearsHarvestBlock**: Full harvest logic via Forge's `IForgeShearable`.
- **BiomeModifier codecs**: All 4 types (AddFeatures/RemoveFeatures/AddSpawns/RemoveSpawns) fully implemented with `RecordCodecBuilder` + `RegistryCodecs.homogeneousList()`.
- **StructureModifier**: `NoneStructureModifier` with real `MapCodec.unit()` codec.
- **ClientHooks event delegation**: `getDetachedCameraDistance()` fires `CalculateDetachedCameraDistanceEvent`; `onScreenshot()` posts to NeoForge event bus.
- **DimensionSpecialEffects**: Reflective injection of mod-registered effects into vanilla's static `EFFECTS` map — fixes Twilight Forest sky rendering.
- **RenderBuffers**: Reflective injection of custom `RenderType` buffers into `BufferSource.fixedBuffers` — fixes Create's custom rendering.
- **IEntityWithComplexSpawn**: Bridged to Forge's `IEntityAdditionalSpawnData` with `RegistryFriendlyByteBuf ↔ FriendlyByteBuf` default method adapters.

#### Phase 5 (03-28 → 03-30): Final 5% Completion Push
- **ItemStack attribute modifier hook**: New Mixin injects into both `ItemStack.forEachModifier()` overloads, routing through `IItemStackExtension.getAttributeModifiers()` to fire `ItemAttributeModifierEvent` — fixes Create's dynamic attribute modifications.
- **DataMap infrastructure**: `RegisterDataMapTypesEvent` fired during `commonSetup`; new `DataMapInitializer` populates built-in DataMaps from vanilla `ComposterBlock.COMPOSTABLES` and `ForgeHooks.getBurnTime()` (compostable values, fuel burn times).
- **EventHooks event activation**:
  - `canEntityContinueSleeping()` → fires `CanContinueSleepingEvent` returning event result
  - `getEnchantmentLevelSpecific()` / `getAllEnchantmentLevels()` → fires `GetEnchantmentLevelEvent` with modifiable enchantment map
  - `getCustomSpawners()` → fires `ModifyCustomSpawnersEvent` with mutable spawner list
- **GetEnchantmentLevelEvent**: Upgraded to carry a modifiable `Map<Holder<Enchantment>, Integer>` enchantment map.
- **ModifyCustomSpawnersEvent**: Upgraded to carry a mutable `List<CustomSpawner>` spawner list.
- **RegisterGuiLayersEvent ordering fix**: `registerAbove()`/`registerBelow()` now use `ForgeLayeredDraw.addAbove()`/`addBelow()` for correct Z-ordering — fixes Create goggle/schematic overlay display.

#### Phase 6 (03-30 → 04-05): Create Accessor Bridge & Flywheel GPU Rendering Pipeline
- **Create Accessor Interface Bridge (33 interfaces)**: Complete BytecodeRewriter-based solution for all 33 Create accessor/extension interfaces. `CHECKCAST` redirects from NeoForge accessor types to vanilla MC target classes; `INVOKEINTERFACE` → `INVOKEVIRTUAL` rewrites. 24 new Mixin files inject accessor method bodies into vanilla classes (e.g., `LevelRendererAccessorMixin`, `ParticleEngineAccessorMixin`, `GameRendererAccessorMixin`).
- **Flywheel Accessor Interface Bridge (10 interfaces)**: Same pattern applied to all 7 Flywheel accessor interfaces + 3 extension interfaces. Covers `LevelRendererAccessor`, `AbstractClientPlayerAccessor`, `LightEngineAccessor`, `LayerLightSectionStorageAccessor`, `SkyDataLayerStorageMapAccessor`, `ModelPartAccessor`, `PoseStackAccessor`, plus `LevelExtension`, `PoseStackExtension`, `SkyLightSectionStorageExtension`.
- **Flywheel Render Pipeline Hooks**: `FlywheelLevelRendererMixin` injects into `LevelRenderer.renderLevel()` at 4 injection points (beginRender, beforeBlockEntities, beforeCrumbling, endRender). `FlywheelRenderBridge` uses reflection to invoke Flywheel's `FlwBackend` / `VisualizationManagerImpl` through the NeoModClassLoader boundary.
- **EventBusHelper Boolean ClassCast Fix**: Rewrote `postAndReturn()` to bypass the IEventBus proxy entirely — calls `NeoForgeEventBusAdapter.dispatchFallback()` + `MinecraftForge.EVENT_BUS.post()` directly, eliminating `ReloadLevelRendererEvent cannot be cast to Boolean`.
- **SkyLightSectionStorage Extension**: Complex mixin implementing Flywheel's `flywheel$skyDataLayer(long)` — traverses upward through light sections using `FlywheelSkyStorageMapHelper` (transformer-classloader interface) to bridge `SkyDataLayerStorageMap`'s package-private fields.
- **AABB.INFINITE CoreMod**: JavaScript coremod patches `AABB` class to add the `INFINITE` static field (NeoForge addition not present in Forge).
- **Standalone ModelResourceLocation**: CoreMod fixes `ModelResourceLocation.standalone()` factory method for models without block state variants.
- **Verifier Stack Frame Fix**: BytecodeRewriter now patches stack frame types to replace NeoForge accessor interface descriptors with vanilla MC class descriptors — fixes `VerifyError` from JVM bytecode verification.

### Notes

- The percentages above are engineering estimates, not formal test pass rates.
- 861 Java source files total (709 shim + 52 core + 100 mixin), 91 Mixin patches, 7 JavaScript CoreMods, 60 event wrapper constructors.
- Only 4 `UnsupportedOperationException` remain — all intentional by design (e.g. `PartEntity.getAddEntityPacket()`, `ClientCommandSourceStack.getServer()`).
- The biggest remaining gaps are advanced entity sync protocols, NeoForge-exclusive deep vanilla patch behavior (e.g. PistonPushReaction extension), and potentially uncovered edge paths in large mods.

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

All Rights Reserved © 2025-2026 Mai_xiyu

## 🙋 Support

If you encounter issues or have questions:
1. Check if the NeoForge mod is compatible with Forge 1.21
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
