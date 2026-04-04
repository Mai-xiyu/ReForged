package org.xiyu.reforged.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Ensures {@link MappedRegistry#createIntrusiveHolder(Object)} always works,
 * even after the registry has been frozen (which nulls the map).
 *
 * <p>NeoForge mods like Create trigger Block class loading during non-BLOCK
 * RegisterEvent handlers. The Block constructor calls {@code createIntrusiveHolder},
 * which fails if the registry was previously frozen. This mixin lazily
 * re-creates the map so the call succeeds.</p>
 */
@Mixin(value = MappedRegistry.class, remap = false)
public abstract class MappedRegistryMixin<T> {

    @Shadow(remap = false)
    protected Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow @Final
    private Map<T, Holder.Reference<T>> byValue;

    @Shadow(remap = false)
    abstract void validateWrite();

    @Shadow(remap = false)
    public abstract HolderLookup.RegistryLookup<T> asLookup();

    @Inject(method = "createIntrusiveHolder", at = @At("HEAD"), cancellable = true, remap = false)
    private void reforged$createIntrusiveHolderCompat(T value, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        this.validateWrite();

        // NeoForge mods may create intrusive holders on registries that are
        // not marked intrusive in Forge. Keep a compatibility path here.
        if (this.unregisteredIntrusiveHolders == null) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
        }

        Holder.Reference<T> holder = this.unregisteredIntrusiveHolders.computeIfAbsent(
                value,
            key -> Holder.Reference.createIntrusive(this.asLookup(), key)
        );
        cir.setReturnValue(holder);
    }

    /**
     * NeoForge adds containsValue(Object) to DefaultedRegistry.
     * Forge only has containsKey. Create calls containsValue on item registries.
     */
    public boolean containsValue(Object value) {
        return this.byValue.containsKey(value);
    }
}
