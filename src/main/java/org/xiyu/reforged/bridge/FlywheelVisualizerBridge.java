package org.xiyu.reforged.bridge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge for Flywheel's visualizer registry.
 *
 * <p>NeoForge's Flywheel uses Mixins to make {@code EntityType} implement
 * {@code EntityTypeExtension} and {@code BlockEntityType} implement
 * {@code BlockEntityTypeExtension}. These Mixins cannot be applied in the
 * Forge environment. Instead, we intercept the CHECKCAST + INVOKEINTERFACE
 * pattern in {@code VisualizerRegistryImpl} (via {@code MethodCallRedirector})
 * and store visualizers in side maps here.</p>
 */
public final class FlywheelVisualizerBridge {

    private FlywheelVisualizerBridge() {}

    // Maps keyed by identity (EntityType / BlockEntityType) → visualizer object
    private static final Map<Object, Object> ENTITY_VISUALIZERS = new ConcurrentHashMap<>();
    private static final Map<Object, Object> BLOCK_ENTITY_VISUALIZERS = new ConcurrentHashMap<>();

    // ── Entity visualizers ─────────────────────────────────────────────

    public static void setEntityVisualizer(Object entityType, Object visualizer) {
        if (entityType != null && visualizer != null) {
            ENTITY_VISUALIZERS.put(entityType, visualizer);
        }
    }

    public static Object getEntityVisualizer(Object entityType) {
        return ENTITY_VISUALIZERS.get(entityType);
    }

    // ── Block entity visualizers ───────────────────────────────────────

    public static void setBlockEntityVisualizer(Object blockEntityType, Object visualizer) {
        if (blockEntityType != null && visualizer != null) {
            BLOCK_ENTITY_VISUALIZERS.put(blockEntityType, visualizer);
        }
    }

    public static Object getBlockEntityVisualizer(Object blockEntityType) {
        return BLOCK_ENTITY_VISUALIZERS.get(blockEntityType);
    }
}
