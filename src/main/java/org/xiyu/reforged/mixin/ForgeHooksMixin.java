package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.ForgeHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridges Forge's entity attribute registration to NeoForge mods.
 *
 * <p>After Forge's {@code ForgeHooks.modifyAttributes()} fires Forge's own attribute events,
 * this mixin injects additional logic to fire the NeoForge equivalents so that NeoForge mods
 * (e.g., Twilight Forest) can register their entity attributes.</p>
 *
 * <p>Dispatch is routed through an untyped helper to avoid verifier issues at synthetic
 * mixin call-sites during bootstrap.</p>
 */
@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin {

    private static final Logger REFORGED_LOGGER = LoggerFactory.getLogger("ReForged");

    @Shadow
    @Final
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> FORGE_ATTRIBUTES;

    /**
     * After Forge's modifyAttributes() completes, fire the NeoForge equivalents.
     */
    @Inject(method = "modifyAttributes", at = @At("TAIL"))
    private static void reforged$bridgeAttributeEvents(CallbackInfo ci) {
        REFORGED_LOGGER.info("[ReForged] Bridging entity attribute events to NeoForge mods...");

        // ---- 1. Fire NeoForge EntityAttributeCreationEvent ----
        try {
            var neoCreationEvent = new net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent(FORGE_ATTRIBUTES);
            dispatchToNeoForgeModBus(neoCreationEvent);
            REFORGED_LOGGER.info("[ReForged] EntityAttributeCreationEvent dispatched. Attributes map now has {} entries.",
                    FORGE_ATTRIBUTES.size());
        } catch (Throwable t) {
            REFORGED_LOGGER.error("[ReForged] Failed to dispatch NeoForge EntityAttributeCreationEvent", t);
        }

        // ---- 2. Fire NeoForge EntityAttributeModificationEvent ----
        try {
            var modMap = new HashMap<EntityType<? extends LivingEntity>, AttributeSupplier.Builder>();
            var neoModEvent = new net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent(modMap);
            dispatchToNeoForgeModBus(neoModEvent);

            // Merge modifications into FORGE_ATTRIBUTES (same logic as Forge's modifyAttributes)
            modMap.forEach((entityType, builder) -> {
                AttributeSupplier existing = DefaultAttributes.getSupplier(entityType);
                AttributeSupplier.Builder newBuilder = existing != null
                        ? new AttributeSupplier.Builder(existing) : new AttributeSupplier.Builder();
                newBuilder.combine(builder);
                FORGE_ATTRIBUTES.put(entityType, newBuilder.build());
            });

            if (!modMap.isEmpty()) {
                REFORGED_LOGGER.info("[ReForged] EntityAttributeModificationEvent dispatched. {} entity types modified.",
                        modMap.size());
            }
        } catch (Throwable t) {
            REFORGED_LOGGER.error("[ReForged] Failed to dispatch NeoForge EntityAttributeModificationEvent", t);
        }
    }

    /**
     * Dispatch a NeoForge event to the Forge mod event bus via NeoForgeModLoader.
     * Uses reflection to avoid hard dependency from the mixin target's classloader scope.
     */
    private static void dispatchToNeoForgeModBus(Object neoEvent) {
        try {
            Class<?> neoModLoader = Class.forName("org.xiyu.reforged.core.NeoForgeModLoader");
            Method dispatch = neoModLoader.getMethod("dispatchNeoForgeModEventUntyped", Object.class);
            dispatch.invoke(null, neoEvent);
        } catch (Throwable t) {
            REFORGED_LOGGER.warn("[ReForged] Could not dispatch event via NeoForgeModLoader: {}", t.getMessage());
        }
    }
}
