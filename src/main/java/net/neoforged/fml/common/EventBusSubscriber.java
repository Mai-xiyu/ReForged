package net.neoforged.fml.common;

import net.neoforged.api.distmarker.Dist;

import java.lang.annotation.*;

/**
 * Proxy for NeoForge's {@code @EventBusSubscriber} annotation.
 * In NeoForge this is a top-level annotation; in Forge it's {@code @Mod.EventBusSubscriber}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    String modid() default "";
    Dist[] value() default {};
    Bus bus() default Bus.GAME;

    enum Bus {
        GAME,
        MOD
    }
}
