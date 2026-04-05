package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.xiyu.reforged.core.NeoForgeModLoader;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection-based bridge that calls Flywheel APIs through the NeoModClassLoader.
 *
 * <p>Because Flywheel classes are only loaded by NeoModClassLoader (a child-first classloader),
 * code running in the TransformingClassLoader (including our Mixins) cannot reference Flywheel
 * types directly. This bridge uses reflection to cross the classloader boundary.</p>
 *
 * <p>All methods are fail-safe and log errors rather than crashing.</p>
 */
public final class FlywheelRenderBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    // =============================================================  
    // Initialization state
    // =============================================================
    private static volatile boolean initialized = false;
    private static volatile boolean available = false;

    // =============================================================
    // Cached reflection handles (set during init)
    // =============================================================

    // FlwImpl.freezeRegistries()
    private static Method mFreezeRegistries;

    // EndClientResourceReloadEvent constructor
    private static Constructor<?> ctorEndReloadEvent;

    // ReloadLevelRendererEvent constructor
    private static Constructor<?> ctorReloadLevelRendererEvent;

    // RenderContextImpl.create(LevelRenderer, ClientLevel, RenderBuffers, Matrix4fc, Matrix4f, Camera, float)
    private static Method mCreateRenderContext;

    // VisualizationManager.get(LevelAccessor) and .supportsVisualization(LevelAccessor)
    private static Method mVMGet;
    private static Method mVMSupportsVisualization;

    // VisualizationManager.renderDispatcher() and .blockEntities()
    private static Method mVMRenderDispatcher;
    private static Method mVMBlockEntities;

    // RenderDispatcher.onStartLevelRender(RenderContext), .afterEntities(RenderContext), .beforeCrumbling(RenderContext, Long2ObjectMap)
    private static Method mOnStartLevelRender;
    private static Method mAfterEntities;
    private static Method mBeforeCrumbling;

    // VisualManager.queueAdd(Object), .queueRemove(Object)
    private static Method mQueueAdd;
    private static Method mQueueRemove;

    // VisualizationHelper.skipVanillaRender(Entity)
    private static Method mSkipVanillaRender;

    // FlwImplXplat.INSTANCE field
    private static Object flwImplXplatInstance;
    private static Method mDispatchReloadLevelRendererEvent;

    // Per-frame mutable state (only read/written on render thread)
    private static Object currentRenderContext;
    private static int beginRenderLogCount = 0;

    private FlywheelRenderBridge() {}

    // =============================================================
    // Lazy initialization
    // =============================================================
    private static void ensureInit() {
        if (initialized) return;
        synchronized (FlywheelRenderBridge.class) {
            if (initialized) return;
            try {
                doInit();
                available = true;
                LOGGER.info("[ReForged] FlywheelRenderBridge initialized successfully");
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge init failed — Flywheel rendering will be unavailable: {}", t.getMessage());
                available = false;
            } finally {
                initialized = true;
            }
        }
    }

    private static void doInit() throws Exception {
        ClassLoader cl = NeoForgeModLoader.getNeoModClassLoader();
        if (cl == null) {
            throw new IllegalStateException("NeoModClassLoader not available yet");
        }

        // FlwImpl
        Class<?> cFlwImpl = cl.loadClass("dev.engine_room.flywheel.impl.FlwImpl");
        mFreezeRegistries = cFlwImpl.getMethod("freezeRegistries");

        // EndClientResourceReloadEvent
        Class<?> cEndReloadEvent = cl.loadClass("dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent");
        ctorEndReloadEvent = cEndReloadEvent.getConstructor(Minecraft.class, ResourceManager.class, boolean.class, Optional.class);

        // ReloadLevelRendererEvent
        Class<?> cReloadLREvent = cl.loadClass("dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent");
        ctorReloadLevelRendererEvent = cReloadLREvent.getConstructor(ClientLevel.class);

        // FlwImplXplat.INSTANCE
        Class<?> cFlwImplXplat = cl.loadClass("dev.engine_room.flywheel.impl.FlwImplXplat");
        Field fInstance = cFlwImplXplat.getField("INSTANCE");
        flwImplXplatInstance = fInstance.get(null);
        mDispatchReloadLevelRendererEvent = cFlwImplXplat.getMethod("dispatchReloadLevelRendererEvent", ClientLevel.class);

        // RenderContextImpl
        Class<?> cRenderContextImpl = cl.loadClass("dev.engine_room.flywheel.impl.event.RenderContextImpl");
        // create(LevelRenderer, ClientLevel, RenderBuffers, Matrix4fc, Matrix4f, Camera, float)
        Class<?> cMatrix4fc = org.joml.Matrix4fc.class;
        mCreateRenderContext = cRenderContextImpl.getMethod("create",
                LevelRenderer.class, ClientLevel.class, RenderBuffers.class,
                cMatrix4fc, Matrix4f.class, Camera.class, float.class);

        // VisualizationManager
        Class<?> cVisMgr = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualizationManager");
        mVMGet = cVisMgr.getMethod("get", net.minecraft.world.level.LevelAccessor.class);
        mVMSupportsVisualization = cVisMgr.getMethod("supportsVisualization", net.minecraft.world.level.LevelAccessor.class);
        mVMRenderDispatcher = cVisMgr.getMethod("renderDispatcher");
        mVMBlockEntities = cVisMgr.getMethod("blockEntities");

        // RenderDispatcher
        Class<?> cRenderContext = cl.loadClass("dev.engine_room.flywheel.api.backend.RenderContext");
        Class<?> cRenderDispatcher = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualizationManager$RenderDispatcher");
        mOnStartLevelRender = cRenderDispatcher.getMethod("onStartLevelRender", cRenderContext);
        mAfterEntities = cRenderDispatcher.getMethod("afterEntities", cRenderContext);
        mBeforeCrumbling = cRenderDispatcher.getMethod("beforeCrumbling", cRenderContext, Long2ObjectMap.class);

        // VisualManager
        Class<?> cVisualManager = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualManager");
        mQueueAdd = cVisualManager.getMethod("queueAdd", Object.class);
        mQueueRemove = cVisualManager.getMethod("queueRemove", Object.class);

        // VisualizationHelper
        Class<?> cVisHelper = cl.loadClass("dev.engine_room.flywheel.lib.visualization.VisualizationHelper");
        mSkipVanillaRender = cVisHelper.getMethod("skipVanillaRender", Entity.class);
    }

    /** Whether Flywheel rendering bridge is available. */
    public static boolean isAvailable() {
        ensureInit();
        return available;
    }

    // =============================================================
    // Event firing
    // =============================================================

    /** Call FlwImpl.freezeRegistries() — must happen before EndClientResourceReloadEvent. */
    public static void freezeRegistries() {
        ensureInit();
        if (!available) return;
        try {
            mFreezeRegistries.invoke(null);
            LOGGER.info("[ReForged] FlywheelRenderBridge: freezeRegistries() called");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: freezeRegistries() failed: {}", t.getMessage());
        }
    }

    /**
     * Fire EndClientResourceReloadEvent through ModLoader.postEvent() → fallback dispatch.
     */
    public static void fireEndClientResourceReloadEvent(Minecraft mc, ResourceManager rm,
                                                         boolean isInitial, Optional<Throwable> error) {
        ensureInit();
        if (!available) return;
        try {
            Object event = ctorEndReloadEvent.newInstance(mc, rm, isInitial, error);
            NeoForgeEventBusAdapter.dispatchFallback(event);
            LOGGER.info("[ReForged] FlywheelRenderBridge: EndClientResourceReloadEvent dispatched (initial={})", isInitial);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: EndClientResourceReloadEvent failed: {}", t.getMessage(), t);
        }
    }

    /**
     * Fire ReloadLevelRendererEvent through the game bus fallback dispatch.
     */
    public static void fireReloadLevelRendererEvent(ClientLevel level) {
        ensureInit();
        if (!available || level == null) return;
        try {
            // Use FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level)
            // which internally fires the event on NeoForge.EVENT_BUS
            mDispatchReloadLevelRendererEvent.invoke(flwImplXplatInstance, level);
            LOGGER.info("[ReForged] FlywheelRenderBridge: dispatchReloadLevelRendererEvent called");
        } catch (Throwable t) {
            // Fallback: construct and dispatch directly
            try {
                Object event = ctorReloadLevelRendererEvent.newInstance(level);
                NeoForgeEventBusAdapter.dispatchFallback(event);
                LOGGER.info("[ReForged] FlywheelRenderBridge: ReloadLevelRendererEvent dispatched via fallback");
            } catch (Throwable t2) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: ReloadLevelRendererEvent failed: {}", t2.getMessage());
            }
        }
    }

    // =============================================================
    // Per-frame render hooks
    // =============================================================

    /**
     * Called at the start of LevelRenderer.renderLevel() — creates the Flywheel RenderContext.
     */
    public static void beginRender(LevelRenderer renderer, ClientLevel level,
                                    RenderBuffers buffers, Matrix4f viewMatrix,
                                    Matrix4f projMatrix, Camera camera, float partialTick) {
        ensureInit();
        if (!available || level == null) return;
        try {
            Object renderCtx = mCreateRenderContext.invoke(null,
                    renderer, level, buffers, viewMatrix, projMatrix, camera, partialTick);
            currentRenderContext = renderCtx;

            Object visMgr = mVMGet.invoke(null, level);
            if (visMgr != null) {
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mOnStartLevelRender.invoke(dispatcher, renderCtx);
            } else if (beginRenderLogCount++ < 3) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: beginRender — VisualizationManager.get(level) returned null");
            }
        } catch (Throwable t) {
            if (beginRenderLogCount++ < 5) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: beginRender failed: {}", t.getMessage(), t);
            }
            currentRenderContext = null;
        }
    }

    /**
     * Called before block entity rendering in LevelRenderer.renderLevel().
     */
    public static void beforeBlockEntities(ClientLevel level) {
        if (!available || currentRenderContext == null || level == null) return;
        try {
            Object visMgr = mVMGet.invoke(null, level);
            if (visMgr != null) {
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mAfterEntities.invoke(dispatcher, currentRenderContext);
            }
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] FlywheelRenderBridge: beforeBlockEntities failed: {}", t.getMessage());
        }
    }

    /**
     * Called before render crumbling in LevelRenderer.renderLevel().
     */
    @SuppressWarnings("rawtypes")
    public static void beforeRenderCrumbling(ClientLevel level, Long2ObjectMap destructionProgress) {
        if (!available || currentRenderContext == null || level == null) return;
        try {
            Object visMgr = mVMGet.invoke(null, level);
            if (visMgr != null) {
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mBeforeCrumbling.invoke(dispatcher, currentRenderContext, destructionProgress);
            }
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] FlywheelRenderBridge: beforeRenderCrumbling failed: {}", t.getMessage());
        }
    }

    /**
     * Called at the end of LevelRenderer.renderLevel().
     */
    public static void endRender() {
        currentRenderContext = null;
    }

    // =============================================================
    // Entity rendering skip
    // =============================================================

    /**
     * Check if vanilla rendering should be skipped for this entity (Flywheel handles it).
     */
    public static boolean shouldSkipVanillaRender(Entity entity) {
        if (!available) return false;
        try {
            boolean supports = (boolean) mVMSupportsVisualization.invoke(null, entity.level());
            if (!supports) return false;
            return (boolean) mSkipVanillaRender.invoke(null, entity);
        } catch (Throwable t) {
            return false;
        }
    }

    // =============================================================
    // Visual tracking (block entity add/remove)
    // =============================================================

    /**
     * Notify Flywheel that a block entity was added (e.g., from chunk loading).
     */
    public static void onBlockEntityAdded(Level level, BlockEntity blockEntity) {
        ensureInit();
        if (!available || level == null) return;
        try {
            Object visMgr = mVMGet.invoke(null, level);
            if (visMgr != null) {
                Object beStorage = mVMBlockEntities.invoke(visMgr);
                mQueueAdd.invoke(beStorage, blockEntity);
            }
        } catch (Throwable t) {
            // Silent — this fires very frequently during chunk loading
        }
    }

    /**
     * Notify Flywheel that a block entity was removed.
     */
    public static void onBlockEntityRemoved(Level level, BlockEntity blockEntity) {
        ensureInit();
        if (!available || level == null) return;
        try {
            Object visMgr = mVMGet.invoke(null, level);
            if (visMgr != null) {
                Object beStorage = mVMBlockEntities.invoke(visMgr);
                mQueueRemove.invoke(beStorage, blockEntity);
            }
        } catch (Throwable t) {
            // Silent
        }
    }
}
