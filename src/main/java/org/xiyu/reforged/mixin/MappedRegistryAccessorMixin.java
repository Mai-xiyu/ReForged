package org.xiyu.reforged.mixin;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryAccessorMixin<T> {
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;

    public Reference2IntMap<T> getToId() {
        return this.toId;
    }

    public Map<T, Holder.Reference<T>> getByValue() {
        return this.byValue;
    }
}
