package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.client.accessor.BufferBuilderAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes BufferBuilder implement Ponder's BufferBuilderAccessor,
 * exposing the vertices count.
 */
@Mixin(value = BufferBuilder.class, remap = false)
public abstract class BufferBuilderAccessorMixin implements BufferBuilderAccessor {

    @Shadow
    private int vertices;

    @Override
    public int catnip$getVertices() {
        return this.vertices;
    }
}
