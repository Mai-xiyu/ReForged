package org.xiyu.reforged;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.xiyu.reforged.core.DataMapInitializer;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.*;

/**
 * ReForged — NeoForge Compatibility Bridge for Forge 1.21.1
 *
 * <p>Drop this mod + NeoForge mods into .minecraft/mods/. ReForged will:
 * <ol>
 *     <li>Scan mods/ for NeoForge JARs (those with neoforge.mods.toml)</li>
 *     <li>Load their classes via a custom ClassLoader</li>
 *     <li>Instantiate their @Mod classes with a bridged event bus</li>
 *     <li>Register their resources (models, textures, sounds, data) as resource packs</li>
 * </ol>
 * No JAR modification needed — everything happens at runtime.</p>
 */
@Mod(Reforged.MODID)
public class Reforged {

    public static final String MODID = "reforged";
    public static final String VERSION = "1.0.0";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Cached NIO FileSystems for NeoForge JARs — kept alive for the game session. */
    private static final List<FileSystem> jarFileSystems = new ArrayList<>();

    // Forge 51 constructs @Mod classes via no-arg constructor.
    public Reforged() {
        init(FMLJavaModLoadingContext.get());
    }

    public Reforged(FMLJavaModLoadingContext context) {
        init(context);
    }

    private void init(FMLJavaModLoadingContext context) {
        LOGGER.info("========================================");
        LOGGER.info(" ReForged v{} — NeoForge Compatibility Bridge", VERSION);
        LOGGER.info("========================================");

        // ── 1. Discover the mods directory ───────────────────
        Path modsDir = FMLPaths.MODSDIR.get();
        LOGGER.info("[ReForged] Mods directory: {}", modsDir);

        // ── 2. Load NeoForge mods at runtime ─────────────────
        NeoForgeModLoader.loadAll(modsDir, context.getModEventBus());

        // ── 3. Register event listeners ──────────────────────
        context.getModEventBus().addListener(this::commonSetup);
        context.getModEventBus().addListener(this::clientSetup);
        context.getModEventBus().addListener(this::onForgeLoadComplete);
        context.getModEventBus().addListener(this::addPackFinders);

        LOGGER.info("[ReForged] Bootstrap complete");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        net.neoforged.neoforge.attachment.AttachmentInternals.init();
        net.neoforged.neoforge.capabilities.CapabilityHooks.init();
        net.neoforged.neoforge.network.registration.NetworkRegistry.setup();
        // Fire ModifyDefaultComponentsEvent to NeoForge mods
        NeoForgeModLoader.dispatchNeoForgeModEvent(new net.neoforged.neoforge.event.ModifyDefaultComponentsEvent());
        // Fire RegisterDataMapTypesEvent so NeoForge mods can register custom DataMap types
        NeoForgeModLoader.dispatchNeoForgeModEvent(new net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent());
        // Populate built-in DataMaps from vanilla/Forge data
        DataMapInitializer.populateBuiltinDataMaps();
        // Initialize cauldron fluid content and let NeoForge mods register custom cauldrons
        net.neoforged.neoforge.fluids.CauldronFluidContent.init();
        NeoForgeModLoader.dispatchNeoForgeModEvent(new net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent());
        LOGGER.info("[ReForged] Common setup phase — NeoForge bridge active");
    }

    @SuppressWarnings("removal")
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[ReForged] Client setup phase — firing NeoForge client events");
        // Fire NeoForge-only client registration events that have no Forge equivalent

        // 1. RegisterDimensionSpecialEffectsEvent (Twilight Forest needs this)
        net.neoforged.neoforge.client.DimensionSpecialEffectsManager.init();

        // 1.5. RegisterGuiLayersEvent (Create goggle overlay, etc.)
        // Layers are collected here and deferred; GuiRenderMixin applies them on first Gui.render().
        try {
            var guiLayersEvent = new net.neoforged.neoforge.client.event.RegisterGuiLayersEvent();
            NeoForgeModLoader.dispatchNeoForgeModEvent(guiLayersEvent);
            int layerCount = guiLayersEvent.getOrderedLayers().size();
            if (layerCount > 0) {
                LOGGER.info("[ReForged] Collected {} NeoForge GUI layer(s) (deferred apply on first render)", layerCount);
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterGuiLayersEvent dispatch failed: {}", t.getMessage());
        }

        // 2. RegisterClientExtensionsEvent (Create/TF need this for custom rendering)
        NeoForgeModLoader.dispatchNeoForgeModEvent(
                new net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent());

        // 3. RegisterMenuScreensEvent (Create needs this for 20+ menu screens)
        try {
            var screens = new java.util.HashMap<net.minecraft.world.inventory.MenuType<?>,
                    net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor<?, ?>>();
            var menuEvent = new net.neoforged.neoforge.client.event.RegisterMenuScreensEvent(screens);
            NeoForgeModLoader.dispatchNeoForgeModEvent(menuEvent);
            // Apply the registered screen constructors to Forge's MenuScreens
            screens.forEach((type, ctor) -> {
                try {
                    registerMenuScreenUnchecked(type, ctor);
                } catch (Throwable t) {
                    LOGGER.warn("[ReForged] Failed to register menu screen for {}: {}", type, t.getMessage());
                }
            });
            LOGGER.info("[ReForged] Registered {} NeoForge menu screen(s)", screens.size());
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterMenuScreensEvent dispatch failed: {}", t.getMessage());
        }

        // 4. RegisterNamedRenderTypesEvent (Create uses custom render types)
        try {
            var renderTypes = new java.util.HashMap<net.minecraft.resources.ResourceLocation,
                    net.neoforged.neoforge.client.RenderTypeGroup>();
            var rtEvent = new net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent(renderTypes);
            NeoForgeModLoader.dispatchNeoForgeModEvent(rtEvent);
            // Register named render types into Forge's NamedRenderTypeManager via reflection
            if (!renderTypes.isEmpty()) {
                try {
                    var forgeManager = net.minecraftforge.client.NamedRenderTypeManager.class;
                    java.lang.reflect.Field mapField = null;
                    for (java.lang.reflect.Field f : forgeManager.getDeclaredFields()) {
                        if (java.util.Map.class.isAssignableFrom(f.getType())) {
                            mapField = f;
                            break;
                        }
                    }
                    if (mapField != null) {
                        mapField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        var forgeMap = (java.util.Map<net.minecraft.resources.ResourceLocation, Object>) mapField.get(null);
                        if (forgeMap != null) {
                            renderTypes.forEach((key, group) -> forgeMap.put(key, group.toForge()));
                        }
                    }
                } catch (Throwable ignored) {}
                LOGGER.info("[ReForged] Registered {} NeoForge named render type(s)", renderTypes.size());
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterNamedRenderTypesEvent dispatch failed: {}", t.getMessage());
        }

        // 5. RegisterRenderBuffersEvent (Create uses custom render buffers)
        try {
            var buffers = new java.util.LinkedHashMap<net.minecraft.client.renderer.RenderType,
                    com.mojang.blaze3d.vertex.ByteBufferBuilder>();
            var rbEvent = new net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent(buffers);
            NeoForgeModLoader.dispatchNeoForgeModEvent(rbEvent);
            if (!buffers.isEmpty()) {
                // Inject into Minecraft's RenderBuffers.bufferSource().fixedBuffers map
                try {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    var renderBuffers = mc.renderBuffers();
                    var bufferSource = renderBuffers.bufferSource();
                    java.lang.reflect.Field fixedField = net.minecraft.client.renderer.MultiBufferSource.BufferSource.class.getDeclaredField("fixedBuffers");
                    fixedField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    var fixedMap = (java.util.SequencedMap<net.minecraft.client.renderer.RenderType, com.mojang.blaze3d.vertex.ByteBufferBuilder>) fixedField.get(bufferSource);
                    fixedMap.putAll(buffers);
                    LOGGER.info("[ReForged] Injected {} NeoForge render buffer(s) into BufferSource", buffers.size());
                } catch (Throwable reflectErr) {
                    LOGGER.warn("[ReForged] Failed to inject render buffers into BufferSource: {}", reflectErr.getMessage());
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterRenderBuffersEvent dispatch failed: {}", t.getMessage());
        }

        // 6. RegisterItemDecorationsEvent (Create uses item decorators)
        try {
            var decorators = new java.util.HashMap<net.minecraft.world.item.Item,
                    java.util.List<net.neoforged.neoforge.client.IItemDecorator>>();
            var idEvent = new net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent(decorators);
            NeoForgeModLoader.dispatchNeoForgeModEvent(idEvent);
            // Apply to ItemDecoratorHandler
            decorators.forEach((item, decoList) -> {
                for (var deco : decoList) {
                    net.neoforged.neoforge.client.ItemDecoratorHandler.register(item, deco);
                }
            });
            if (!decorators.isEmpty()) {
                LOGGER.info("[ReForged] Registered {} NeoForge item decoration(s)", decorators.size());
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterItemDecorationsEvent dispatch failed: {}", t.getMessage());
        }

        // 7. RegisterDimensionTransitionScreenEvent (TF uses custom transition screens)
        try {
            var dtEvent = new net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent(
                    net.neoforged.neoforge.client.DimensionTransitionScreenManager.conditionalEffects(),
                    net.neoforged.neoforge.client.DimensionTransitionScreenManager.incomingEffects(),
                    net.neoforged.neoforge.client.DimensionTransitionScreenManager.outgoingEffects());
            NeoForgeModLoader.dispatchNeoForgeModEvent(dtEvent);
            int total = net.neoforged.neoforge.client.DimensionTransitionScreenManager.conditionalEffects().size()
                    + net.neoforged.neoforge.client.DimensionTransitionScreenManager.incomingEffects().size()
                    + net.neoforged.neoforge.client.DimensionTransitionScreenManager.outgoingEffects().size();
            if (total > 0) {
                LOGGER.info("[ReForged] Registered {} NeoForge dimension transition screen(s)", total);
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] RegisterDimensionTransitionScreenEvent dispatch failed: {}", t.getMessage());
        }

        LOGGER.info("[ReForged] Client setup phase complete");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerMenuScreenUnchecked(net.minecraft.world.inventory.MenuType<?> type,
                                                     net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor<?, ?> ctor) {
        net.minecraft.client.gui.screens.MenuScreens.register((net.minecraft.world.inventory.MenuType) type,
                (net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor) ctor);
    }

    private void onForgeLoadComplete(final FMLLoadCompleteEvent event) {
        NeoForgeModLoader.dispatchNeoForgeModEvent(new net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent(event));
        LOGGER.info("[ReForged] Forwarded Forge FMLLoadCompleteEvent to NeoForge mod bus");
    }

    /**
     * Register NeoForge mod JARs as resource packs so their assets/ and data/
     * are available to Forge's resource system (models, textures, sounds, recipes, etc.).
     *
     * <p>This event fires during {@link net.minecraft.server.packs.repository.PackRepository}
     * construction, which happens AFTER mod construction, so the NeoForge JARs have
     * already been discovered by {@link NeoForgeModLoader#loadAll}.</p>
     */
    private void addPackFinders(final AddPackFindersEvent event) {
        List<Path> neoJars = NeoForgeModLoader.getLoadedNeoJarPaths();
        if (neoJars.isEmpty()) return;

        LOGGER.info("[ReForged] Registering {} NeoForge JAR(s) as {} packs",
                neoJars.size(), event.getPackType());

        for (Path jarPath : neoJars) {
            try {
                // Open the JAR as a NIO FileSystem so PathPackResources can read from it.
                // We cache the FileSystem to keep it alive for the game session.
                FileSystem jarFs;
                try {
                    jarFs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
                } catch (FileSystemAlreadyExistsException e) {
                    // Already opened (e.g. CLIENT_RESOURCES event fired before SERVER_DATA)
                    jarFs = FileSystems.getFileSystem(jarPath.toUri());
                }
                jarFileSystems.add(jarFs);

                Path root = jarFs.getPath("/");
                var supplier = new PathPackResources.PathResourcesSupplier(root);
                String jarName = jarPath.getFileName().toString();
                String packId = "neomod:" + jarName.replace(".jar", "");

                var info = new PackLocationInfo(
                        packId,
                        Component.literal("[NeoForge] " + jarName),
                        PackSource.BUILT_IN,
                        Optional.empty()
                );

                Pack pack = Pack.readMetaAndCreate(
                        info,
                        supplier,
                        event.getPackType(),
                        new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
                );

                if (pack != null) {
                    event.addRepositorySource(consumer -> consumer.accept(pack));
                    LOGGER.info("[ReForged] ✓ Registered resource pack '{}' for {}",
                            packId, event.getPackType());
                } else {
                    LOGGER.warn("[ReForged] ✗ Could not read pack metadata from '{}' for {} — " +
                            "JAR may be missing pack.mcmeta", jarName, event.getPackType());
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to register resource pack for {}",
                        jarPath.getFileName(), e);
            }
        }
    }
}
