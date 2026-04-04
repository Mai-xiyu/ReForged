package org.xiyu.reforged.mixin;

import net.minecraft.core.Registry;
import net.minecraftforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds NeoForge's {@code getRegistry()} alias to Forge's RegisterEvent.
 * NeoForge mods call {@code event.getRegistry()} which doesn't exist in Forge —
 * Forge only has {@code getVanillaRegistry()}.
 */
@Mixin(value = RegisterEvent.class, remap = false)
public abstract class RegisterEventMixin {

    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry() {
        return ((RegisterEvent) (Object) this).getVanillaRegistry();
    }
}
