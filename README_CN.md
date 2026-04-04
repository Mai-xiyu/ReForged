# ReForged

<p align="center">
  <img src="src/main/resources/logo.png" alt="ReForged Logo" width="256"/>
</p>

🇨🇳 中文版 | [🇬🇧 English](./README.md)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-51.0.33-orange.svg)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/)

一个兼容性桥接项目，让 **NeoForge 模组**能够在 **Minecraft Forge 1.21** 上无缝运行，无需任何修改。

## 📖 概述

ReForged 是一个创新的运行时适配器，它在 NeoForge 和 Forge 模组加载器之间架起了桥梁。它使用先进的字节码转换技术，动态加载 NeoForge 模组并将其 API 调用转换为 Forge 等效调用。

**作者：** Mai_xiyu  
**版本：** 1.0.0  
**许可证：** All Rights Reserved（保留所有权利）

## ✨ 核心特性

- 🔄 **零 JAR 修改** — NeoForge 模组无需重新打包或重新构建即可运行
- 🚀 **动态加载** — 运行时自动发现和加载 NeoForge 模组
- 🔧 **字节码转换** — 使用 ASM 技术将 NeoForge API 调用转换为 Forge
- 🎯 **事件总线桥接** — 透明的事件系统兼容性
- 📦 **资源整合** — NeoForge 模组资源（纹理、模型、配方）自动可用
- ⚙️ **自动配置** — 无缝转换 `neoforge.mods.toml` 为 Forge 格式
- 🎨 **全面补丁** — 91 个 Mixin 补丁处理边缘情况兼容性
- 🛠️ **API 替身** — 为 DeferredRegister、CreativeTabs、Attachments 等提供替代实现

## 🏗️ 技术架构

ReForged 实现了一个复杂的多层兼容性系统：

### 加载流程
```
ReForged 初始化
    ↓
扫描 mods/ 文件夹中的 NeoForge JAR（包含 neoforge.mods.toml）
    ↓
对每个 NeoForge 模组：
    • 将元数据转换为 Forge 格式
    • 创建隔离的类加载器
    • 使用 ASM 转换字节码
    • 将 NeoForge API 引用重映射到替身类
    • 使用桥接的事件总线实例化模组
    ↓
将模组资源注册为 Minecraft 资源包
```

### 核心组件

| 组件 | 作用 |
|------|------|
| **NeoForgeModLoader** | 在运行时发现并实例化 NeoForge 模组 |
| **BytecodeRewriter** | 基于 ASM 的类转换引擎 |
| **ReForgedRemapper** | 将 NeoForge 类引用重写为 Forge 等效类 |
| **NeoForgeEventBusAdapter** | 桥接事件总线系统的动态代理 |
| **Shim 层** | NeoForge 类的替代 API 实现 |
| **Mixin 系统** | 91 个补丁用于 Minecraft/Forge 兼容性 |

## 📦 安装

1. 安装 Minecraft **1.21** 和 **Forge 51.0.33** 或更高版本
2. 下载 ReForged 模组 JAR 文件
3. 将 ReForged 和你的 NeoForge 模组放入 `.minecraft/mods/` 文件夹
4. 启动游戏 — ReForged 将自动检测并加载 NeoForge 模组

**就这么简单！** 无需任何配置。

## 🔨 从源码构建

### 前置要求
- Java 21 或更高版本
- Git

### 构建命令
```bash
# 克隆仓库
git clone https://github.com/Mai-xiyu/ReForged.git
cd ReForged

# 构建模组
./gradlew build

# 编译后的 JAR 文件将位于 build/libs/ 目录
```

### 开发命令
```bash
./gradlew runClient        # 启动游戏客户端
./gradlew runServer        # 启动专用服务器
./gradlew runData          # 生成数据/资源
./gradlew runGameTestServer # 运行游戏测试
```

## 🛠️ 工作原理

### 1. **字节码重映射**
ReForged 使用 ASM（Java 字节码操作框架）重写类引用：
- `net.neoforged.neoforge.common.NeoForge` → `org.xiyu.reforged.shim.NeoForgeShim`
- `net.neoforged.bus.api.IEventBus` → 自定义代理包装器
- 事件注册 → 转发到 Forge 的事件总线

### 2. **事件系统桥接**
当 NeoForge 模组注册事件监听器时：
```java
NeoForge.EVENT_BUS.register(listener);
```
ReForged 会拦截并：
- 分析监听器的 `@SubscribeEvent` 注解
- 在 Forge 的 `MinecraftForge.EVENT_BUS` 上注册处理器
- 根据兼容性需要包装/解包事件对象

### 3. **资源包整合**
NeoForge 模组 JAR 会自动注册为 Minecraft 资源包，使其包含的以下内容立即可用：
- 纹理（`assets/`）
- 模型
- 配方（`data/`）
- 标签
- 其他数据文件

## 📋 系统要求

- **Minecraft：** 1.21
- **Forge：** 51.0.33 或更高版本
- **Java：** 21 或更高版本

## 🤝 兼容性

ReForged 旨在提供广泛的 NeoForge 模组兼容性，但可能存在一些限制：

- ✅ 大多数 NeoForge API 功能受支持
- ✅ 事件系统完全桥接
- ✅ 注册系统（DeferredRegister）兼容
- ✅ 创造模式标签页和物品组正常工作
- ✅ 网络数据包得到处理
- ⚠️ 某些高级 NeoForge 独有功能可能不可用
- ⚠️ 与 NeoForge 深度集成的模组可能需要额外补丁

### 🎯 模组规模与类型适配指南

下表概述了不同类型和规模的 NeoForge 模组在 ReForged 上的预期运行状况。

| 模组类型 | 典型示例 | 预期兼容度 | 说明 |
|----------|----------|------------|------|
| **纯物品/方块模组** | 新矿石、装饰方块、工具武器 | ✅ 优秀 | `DeferredRegister`、CreativeTabs、物品属性、食物组件等均已完整桥接 |
| **世界生成模组** | 自定义矿脉、结构、生物群系修饰器 | ✅ 良好 | `BiomeModifier`/`StructureModifier` 框架已实现；数据包驱动的生成正常 |
| **配方/合成扩展** | 自定义配方类型、条件配方 | ✅ 良好 | `ICondition` 条件系统与自定义 `RecipeSerializer` 可用 |
| **Capability / 附件模组** | 能量、流体、物品存储 | ✅ 良好 | `IEnergyStorage`/`IFluidHandler`/`IItemHandler` 完整实现；`AttachmentType` 桥接至 Forge Capability |
| **网络/数据包模组** | 自定义 Payload 通信 | ✅ 良好 | `PayloadRegistrar` 注册与双向 `reply()` 均已实现 |
| **客户端渲染模组** | 自定义模型、粒子、HUD 覆盖 | ⚠️ 部分 | 基础模型加载（OBJ/JSON）、`RenderType` 注册、GUI 事件可用；深层 BakedModel 变换、自定义 shader 可能需要适配 |
| **信息/工具提示模组** | Jade、WTHIT、JEI 插件 | ⚠️ 部分 | 取决于模组对 NeoForge 接口注入（extension interface）的依赖深度；Jade 已有针对性 Mixin 补丁 |
| **大型内容模组** | Mekanism、Create 等 | ⚠️ 部分 | 已实现 DataMap、BiomeModifier 编解码、属性修改器事件、RenderBuffers 注入、DimensionSpecialEffects 注入、GUI 层级排序、Flywheel GPU 渲染管线等；大部分核心功能可运行，少量边缘路径可能需要额外补丁 |
| **核心/底层模组** | 自定义 ModLoader 扩展、ServiceLoader 覆盖 | ❌ 不支持 | 直接操作 FML 内部或 NeoForge 引导阶段的模组无法通过 shim 兼容 |

**规模参考：**

- **小型模组**（< 50 个类）：仅使用 `DeferredRegister`、事件监听、简单 Capability → **绝大多数可直接运行**。
- **中型模组**（50–300 个类）：包含自定义网络包、客户端渲染、数据生成、条件配方 → **大部分核心功能可运行**，少数高级特性可能需要额外适配。
- **大型模组**（300+ 个类）：深度使用 DataMap、自定义 HolderSet、多方块实体同步、复杂渲染管线 → **需要逐项评估**，可能存在部分功能缺失。

> **经验法则：** 如果一个 NeoForge 模组的核心功能仅依赖注册系统 + 事件总线 + 基础 Capability（物品/能量/流体），那么它大概率可以在 ReForged 上正常工作。模组对 NeoForge 独有的深层 vanilla patch 行为依赖越重，兼容风险越高。

## 📊 当前完成度快照

以下为截至 2026-04-05 的近似工程评估。

| 子系统 | 权重 | 完成度 | 加权分 |
|--------|------|--------|--------|
| Mod 加载管线 | 20% | 85% | 17.0 |
| 事件系统 | 20% | 98% | 19.6 |
| 注册系统 | 15% | 95% | 14.25 |
| 能力系统 | 10% | 95% | 9.5 |
| 网络 / Payload | 8% | 82% | 6.56 |
| 扩展 / 通用 API | 12% | 96% | 11.52 |
| 客户端 | 10% | 97% | 9.7 |
| Mixin 覆盖 | 5% | 95% | 4.75 |
| **总计** | **100%** |  | **~93%** |

### 近期变更（03-09 → 04-05）

#### Phase 1（03-09 → 03-10）：基础事件与 API 框架
- **事件系统（重大更新）**：新增 **60 个 Forge wrapper 构造函数**，实现通过 `NeoForgeEventBusAdapter` 自动桥接事件。覆盖服务器生命周期、实体、生物、玩家、世界/区块、村庄、酿造、附魔、砂轮等事件类别。
- **ClientHooks**：从 18 个方法扩展至 **108 个方法**，完整委托 `ForgeHooksClient`。
- **EventHooks / CommonHooks**：新增 ~27 个缺失方法。
- **客户端+通用事件包装器（~60 个）**：完整覆盖渲染、输入、生命周期、实体、区块等事件。
- **注册系统**：实现 DataMap 系统、HolderSetType 编解码器、DeferredHolder 标签解析。

#### Phase 2–4（03-10 → 03-28）：深层兼容性与 Create/暮色森林支持
- **EntityEvent.Size**：补全 `pose`/`oldSize`/`newSize`/`newEyeHeight` 字段与 getter/setter。
- **GuiGraphics 9-slice 渲染**：新增 `GuiGraphicsExtensionMixin`，实现 `blitWithBorder()`（9 宫格渲染）和 `blitInscribed()`（等比缩放渲染）。
- **CommonHooks.extractLookupProvider**：实现多字段名重试（3 种映射名）+ 服务器回退机制。
- **CommonHooks.tryDispenseShearsHarvestBlock**：通过 Forge `IForgeShearable` 接口实现剪刀收割逻辑。
- **BiomeModifier 编解码器**：全部 4 种类型（AddFeatures/RemoveFeatures/AddSpawns/RemoveSpawns）使用 `RecordCodecBuilder` + `RegistryCodecs.homogeneousList()` 完整实现。
- **StructureModifier**：`NoneStructureModifier` 使用 `MapCodec.unit()` 实现真实编解码器。
- **ClientHooks 事件委托**：`getDetachedCameraDistance()` 触发 `CalculateDetachedCameraDistanceEvent`；`onScreenshot()` 投递至 NeoForge 事件总线。
- **DimensionSpecialEffects**：反射注入模组注册的维度效果到原版静态 `EFFECTS` Map，修复暮色森林天空渲染。
- **RenderBuffers**：反射注入自定义 `RenderType` 缓冲到 `BufferSource.fixedBuffers`，修复 Create 自定义渲染。
- **IEntityWithComplexSpawn**：桥接至 Forge 的 `IEntityAdditionalSpawnData`，实现 `RegistryFriendlyByteBuf ↔ FriendlyByteBuf` 默认方法适配。

#### Phase 5（03-28 → 03-30）：最终 5% 补全
- **ItemStack 属性修改器钩子**：新增 Mixin 注入 `ItemStack.forEachModifier()` 两个重载，路由至 `IItemStackExtension.getAttributeModifiers()` 触发 `ItemAttributeModifierEvent`，修复 Create 动态属性修改。
- **DataMap 基础设施**：`commonSetup` 阶段触发 `RegisterDataMapTypesEvent`；新增 `DataMapInitializer` 从原版 `ComposterBlock.COMPOSTABLES` 和 `ForgeHooks.getBurnTime()` 填充内置 DataMap（堆肥值、燃料燃烧时间）。
- **EventHooks 事件活化**：
  - `canEntityContinueSleeping()` → 触发 `CanContinueSleepingEvent` 并返回事件结果
  - `getEnchantmentLevelSpecific()` / `getAllEnchantmentLevels()` → 触发 `GetEnchantmentLevelEvent` 支持附魔等级修改
  - `getCustomSpawners()` → 触发 `ModifyCustomSpawnersEvent` 支持自定义刷怪器
- **GetEnchantmentLevelEvent**：升级为携带可修改的 `Map<Holder<Enchantment>, Integer>` 附魔映射。
- **ModifyCustomSpawnersEvent**：升级为携带可变 `List<CustomSpawner>` 刷怪器列表。
- **RegisterGuiLayersEvent 排序修复**：`registerAbove()`/`registerBelow()` 现在使用 `ForgeLayeredDraw.addAbove()`/`addBelow()` 实现正确的 Z 轴排序，修复 Create 护目镜/蓝图叠层显示。

#### Phase 6（03-30 → 04-05）：Create Accessor 桥接 与 Flywheel GPU 渲染管线
- **Create Accessor 接口桥接（33 个接口）**：基于 BytecodeRewriter 的完整解决方案，覆盖 Create 全部 33 个 accessor/extension 接口。`CHECKCAST` 从 NeoForge accessor 类型重定向至原版 MC 目标类；`INVOKEINTERFACE` → `INVOKEVIRTUAL` 重写。新增 24 个 Mixin 文件，将 accessor 方法体注入原版类（如 `LevelRendererAccessorMixin`、`ParticleEngineAccessorMixin`、`GameRendererAccessorMixin`）。
- **Flywheel Accessor 接口桥接（10 个接口）**：同样模式应用于 Flywheel 全部 7 个 accessor 接口 + 3 个 extension 接口。覆盖 `LevelRendererAccessor`、`AbstractClientPlayerAccessor`、`LightEngineAccessor`、`LayerLightSectionStorageAccessor`、`SkyDataLayerStorageMapAccessor`、`ModelPartAccessor`、`PoseStackAccessor`，以及 `LevelExtension`、`PoseStackExtension`、`SkyLightSectionStorageExtension`。
- **Flywheel 渲染管线钩子**：`FlywheelLevelRendererMixin` 在 `LevelRenderer.renderLevel()` 的 4 个注入点插入钩子（beginRender、beforeBlockEntities、beforeCrumbling、endRender）。`FlywheelRenderBridge` 通过反射跨 NeoModClassLoader 边界调用 Flywheel 的 `FlwBackend` / `VisualizationManagerImpl`。
- **EventBusHelper Boolean ClassCast 修复**：重写 `postAndReturn()` 完全绕过 IEventBus 代理 — 直接调用 `NeoForgeEventBusAdapter.dispatchFallback()` + `MinecraftForge.EVENT_BUS.post()`，消除 `ReloadLevelRendererEvent cannot be cast to Boolean` 异常。
- **SkyLightSectionStorage 扩展**：复杂 Mixin 实现 Flywheel 的 `flywheel$skyDataLayer(long)` — 通过光照区段向上遍历，使用 `FlywheelSkyStorageMapHelper`（transformer 类加载器接口）桥接 `SkyDataLayerStorageMap` 的包私有字段。
- **AABB.INFINITE CoreMod**：JavaScript 核心模组修补 `AABB` 类，添加 `INFINITE` 静态字段（NeoForge 新增，Forge 中不存在）。
- **独立 ModelResourceLocation**：CoreMod 修复 `ModelResourceLocation.standalone()` 工厂方法，支持无方块状态变体的模型。
- **Verifier 栈帧修复**：BytecodeRewriter 现在修补栈帧类型，将 NeoForge accessor 接口描述符替换为原版 MC 类描述符 — 修复 JVM 字节码验证的 `VerifyError`。

### 说明

- 上述数值属于工程估算，不等同于正式测试通过率。
- 共 861 个 Java 源文件（709 个 shim + 52 个核心 + 100 个 mixin），91 个 Mixin 补丁，7 个 JavaScript CoreMod，60 个事件 wrapper 构造函数。
- 仅剩 4 个 `UnsupportedOperationException` — 全部为设计性保留（如 `PartEntity.getAddEntityPacket()`、`ClientCommandSourceStack.getServer()`）。
- 当前最大缺口：复杂实体同步协议、NeoForge 独有的深层 vanilla patch 行为（如 PistonPushReaction 扩展）、以及大型模组中可能存在的未覆盖边缘路径。

## 📝 项目结构

```
ReForged/
├── src/main/java/org/xiyu/reforged/
│   ├── Reforged.java              # 主模组入口点
│   ├── core/                      # 模组加载和 ASM 转换
│   ├── shim/                      # API 替代层
│   ├── bridge/                    # 事件和系统桥接
│   ├── asm/                       # 高级字节码操作
│   ├── mixin/                     # 用于兼容性的 Mixin 补丁
│   └── util/                      # 工具类
├── src/main/resources/
│   ├── META-INF/
│   │   ├── mods.toml             # Forge 模组元数据
│   │   └── accesstransformer.cfg # 访问权限配置
│   ├── reforged.mixins.json      # Mixin 配置
│   └── coremods/                 # JavaScript CoreMod 补丁
└── build.gradle                   # 构建配置
```

## 🔐 许可证

All Rights Reserved © 2025-2026 Mai_xiyu

## 🙋 支持

如果遇到问题或有疑问：
1. 检查 NeoForge 模组是否与 Forge 1.21 兼容
2. 验证是否安装了 Java 21
3. 检查游戏日志中的错误消息
4. 在 GitHub 仓库上提出 issue

## 🌟 致谢

开发者：**Mai_xiyu**

特别感谢：
- Forge 团队提供 Forge 模组 API
- NeoForge 团队提供 NeoForge 模组 API
- ASM 和 Mixin 社区提供字节码操作工具

---

**注意：** 这是一个社区项目，未经 Forge 或 NeoForge 团队的官方认可或支持。
