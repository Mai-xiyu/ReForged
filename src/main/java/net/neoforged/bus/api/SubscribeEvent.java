package net.neoforged.bus.api;

import java.lang.annotation.*;

/**
 * Proxy for NeoForge's {@code @SubscribeEvent} annotation.
 * Mirrors Forge's {@link net.minecraftforge.eventbus.api.SubscribeEvent}.
 *
 * <p>Note: Forge's event bus scanner looks for its OWN annotation descriptor.
 * We handle this via the ReForged event bus shim which scans for BOTH annotations.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    net.neoforged.bus.api.EventPriority priority() default net.neoforged.bus.api.EventPriority.NORMAL;
    boolean receiveCanceled() default false;
}
