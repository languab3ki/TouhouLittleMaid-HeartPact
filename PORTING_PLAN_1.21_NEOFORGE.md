# Heart Pact 1.21.1 NeoForge 移植计划

## 来源与目标

- 来源代码：`Asumi-Nishiki/TouhouLittleMaid-HeartPact` 的 `1.20` 分支。远端没有名为 `1.20forge` 的分支，因此按现有 `1.20` 分支作为 Forge 1.20.1 来源。
- 目标环境：Minecraft `1.21.1`，NeoForge `21.1.219`。
- 主模组对照：`TartaricAcid/TouhouLittleMaid` 的 `1.21` 分支，发布版本 `1.5.3-neoforge+mc1.21.1`。

## 最小工作拆分

1. 工程骨架迁移
   - ForgeGradle 改为 `net.neoforged.moddev`。
   - `mods.toml` 改为 `neoforge.mods.toml`。
   - Java 版本统一到 21，编译编码固定为 UTF-8。
   - 依赖切到 1.21.1 NeoForge 版本的车万女仆和 Cloth Config。

2. NeoForge 基础 API 迁移
   - `net.minecraftforge.*` 包迁到 `net.neoforged.*`。
   - `NeoForge.EVENT_BUS` 改为 `NeoForge.EVENT_BUS`。
   - `ModLoadingContext`/配置注册改为 1.21 构造器注入的 `ModContainer`。
   - `FMLJavaModLoadingContext.get()` 改为构造器注入的 `IEventBus`。

3. 注册系统迁移
   - `RegistryObject` 改为 NeoForge `DeferredHolder` 或专用 `DeferredItem`。
   - `ForgeRegistries` 改为 `BuiltInRegistries`/`Registries` 或 NeoForge 专用注册入口。
   - 修正 `ResourceLocation` 创建方式，统一用 `ResourceLocation.fromNamespaceAndPath`。

4. 网络系统迁移
   - `SimpleChannel` 消息改为 `CustomPacketPayload`。
   - 每个 payload 增加 `TYPE` 和 `STREAM_CODEC`。
   - 注册改为监听 `RegisterPayloadHandlersEvent`。
   - 发送改为 `PacketDistributor.sendToServer/sendToPlayer/sendToPlayersTrackingEntityAndSelf`。
   - 客户端处理通过 `IPayloadContext.enqueueWork` 与 Dist 隔离。

5. Minecraft 1.21 游戏 API 修复
   - NBT、数据组件、实体保存加载、物品属性、GUI、渲染事件签名逐个按编译错误修复。
   - 对照主模组 1.21 分支中同类写法，优先跟随主模组模式。

6. 主模组 API 适配
   - 检查 `EntityMaid`、动画、Molang、GUI、女仆饰品接口在 1.21 的包名和方法变化。
   - 对 mixin 的 target 和方法签名逐个核对 1.21 主模组源码。

7. 中文编码和乱码治理
   - 修复源码、构建脚本、配置默认值里的 mojibake。
   - 增加脚本扫描 `�`、连续问号、`锛/銆/鐨/灏/涓` 等常见乱码片段。
   - 构建前后运行编码扫描，避免新写入内容被 PowerShell 编码误导。

8. 验证闭环
   - 先跑 `gradlew compileJava`。
   - 编译通过后跑 `gradlew processResources` 和 `gradlew build`。
   - 最后检查 jar 元数据、语言文件、中文资源文本和 mixin refmap。
