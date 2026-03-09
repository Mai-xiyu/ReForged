package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds NeoForge capability query overloads directly onto {@link Entity}.
 *
 * <p>NeoForge injects these methods via extension interfaces. Forge's {@link Entity}
 * lacks them, so NeoForge mods like Jade fail with {@link NoSuchMethodError}.
 * This mixin forwards to the shimmed {@link EntityCapability} implementation.</p>
 */
@Mixin(Entity.class)
public abstract class EntityCapabilityMixin {

    @Nullable
    public <T, C extends @Nullable Object> T getCapability(EntityCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability((Entity) (Object) this, context);
    }

    @Nullable
    public <T> T getCapability(EntityCapability<T, @Nullable Void> capability) {
        return capability.getCapability((Entity) (Object) this, null);
    }
}