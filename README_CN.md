# ReForged

<p align="center">
  <img src="src/main/resources/logo.png" alt="ReForged Logo" width="256"/>
</p>

🇨🇳 中文版 | [🇬🇧 English](./README.md)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-52.1.10-orange.svg)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/)

一个兼容性桥接项目，让 **NeoForge 模组**能够在 **Minecraft Forge 1.21.1** 上无缝运行，无需任何修改。

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
- 🎨 **全面补丁** — 40 个 Mixin 补丁处理边缘情况兼容性
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
| **Mixin 系统** | 40 个补丁用于 Minecraft/Forge 兼容性 |

## 📦 安装

1. 安装 Minecraft **1.21.1** 和 **Forge 52.1.10** 或更高版本
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

- **Minecraft：** 1.21.1
- **Forge：** 52.1.10 或更高版本
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

## 📊 当前完成度快照

以下为截至 2026-03-10 的近似工程评估。

| 子系统 | 权重 | 完成度 | 加权分 |
|--------|------|--------|--------|
| Mod 加载管线 | 20% | 78% | 15.6 |
| 事件系统 | 20% | 80% | 16.0 |
| 注册系统 | 15% | 88% | 13.2 |
| 能力系统 | 10% | 88% | 8.8 |
| 网络 / Payload | 8% | 80% | 6.4 |
| 扩展 / 通用 API | 12% | 85% | 10.2 |
| 客户端 | 10% | 75% | 7.5 |
| Mixin 覆盖 | 5% | 55% | 2.75 |
| **总计** | **100%** |  | **80.5%** |

### 近期变更（03-09 以来）

- **注册系统**：实现完整 DataMap 系统（`DataMapStorage`、`IRegistryExtension.getDataMap()`、`IWithData.getData()`）；`DeferredHolder.is(TagKey)` / `tags()` 现在通过 `BuiltInRegistries` 解析；全部 4 种 HolderSetType 编解码器（ANY/AND/OR/NOT）已完整实现 `makeCodec()` / `makeStreamCodec()`。
- **网络 / Payload**：`ClientPayloadContext.reply()` 和 `ServerPayloadContext.reply()` 已正确委托至 `PayloadChannelRegistry.sendViaConnection()`。
- **扩展 / 通用 API**：`CommonHooks.getTagFromVanillaTier()` 映射全部 6 种原版 Tier 到 `BlockTags.INCORRECT_FOR_*`；`FarmlandWaterManager` 委托至 Forge；`IBlockEntityExtension.getPersistentData()` 提供逐实例缓存；`IHolderLookupProviderExtension.lookup()` 委托至 provider；`CompositeHolderSet` 现在实现 `ICustomHolderSet` 并支持 `homogenize()`。
- **客户端**：`CompositeRenderable.render()` 和 `BakedModelRenderable.render()` 完整实现，包含变换矩阵和 `putBulkData()` 渲染。
- **能力系统**：改进 `AttachmentBridge` 数据委托路径。
- **权限系统**：`PermissionAPI.getRegisteredNodes()` 现在由真实注册表支持，通过 `PermissionGatherEvent` 填充。

### 说明

- 上述数值属于工程估算，不等同于正式测试通过率。
- 共 765 个 Java 源文件（685 个 shim + 80 个核心），40 个 Mixin 补丁。
- 仅剩 4 个 `UnsupportedOperationException` — 全部为设计性保留（如 `PartEntity.getAddEntityPacket()`、`ClientCommandSourceStack.getServer()`）。
- 当前最大缺口仍然是更深层的客户端渲染 / 模型烘焙钩子、复杂实体同步链，以及 NeoForge 对原版类的深层补丁行为。

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
1. 检查 NeoForge 模组是否与 Forge 1.21.1 兼容
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
