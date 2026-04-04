package net.neoforged.fml;

import com.mojang.logging.LogUtils;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Proxy: NeoForge FML's ModLoader — central mod loading coordinator.
 *
 * <p>NeoForge mods call static methods on this class to report loading issues
 * and post events. This stub provides the required API surface so that
 * mod code referencing ModLoader does not crash with ClassNotFoundException.</p>
 */
public final class ModLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ModLoadingIssue> LOADING_ISSUES = new CopyOnWriteArrayList<>();

    private ModLoader() {}

    /**
     * Add a loading issue (warning or error) reported by a mod.
     */
    public static void addLoadingIssue(ModLoadingIssue issue) {
        LOADING_ISSUES.add(issue);
        LOGGER.warn("[ReForged] ModLoader.addLoadingIssue: {}", issue);
    }

    /**
     * Get all loading issues reported so far.
     */
    public static List<ModLoadingIssue> getLoadingIssues() {
        return Collections.unmodifiableList(LOADING_ISSUES);
    }

    /**
     * Check if any loading errors (not just warnings) have been reported.
     */
    public static boolean hasErrors() {
        return LOADING_ISSUES.stream()
                .anyMatch(i -> i.severity() == ModLoadingIssue.Severity.ERROR);
    }

    /**
     * Post an event to the mod event bus. Dispatches to fallback listeners
     * registered via NeoForgeEventBusAdapter for events that Forge's bus can't handle.
     */
    public static void postEvent(Object event) {
        LOGGER.info("[ReForged] ModLoader.postEvent: {}", event.getClass().getSimpleName());
        NeoForgeEventBusAdapter.dispatchFallback(event);
    }

    /**
     * Overload matching NeoForge's exact signature: postEvent(net.neoforged.bus.api.Event).
     * Flywheel's mixin bytecode calls this exact descriptor.
     */
    public static void postEvent(net.neoforged.bus.api.Event event) {
        LOGGER.info("[ReForged] ModLoader.postEvent(Event): {}", event.getClass().getSimpleName());
        NeoForgeEventBusAdapter.dispatchFallback(event);
    }

    /**
     * Post an event wrapping each mod container. Dispatches same as postEvent.
     */
    public static void postEventWrapContainerInModOrder(Object event) {
        LOGGER.info("[ReForged] ModLoader.postEventWrapContainerInModOrder: {}", event.getClass().getSimpleName());
        NeoForgeEventBusAdapter.dispatchFallback(event);
    }
}
