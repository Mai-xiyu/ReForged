package org.xiyu.reforged.bridge;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Bridge for {@link BakedModel} method signature differences between NeoForge and Forge.
 *
 * <p>NeoForge adds {@code useAmbientOcclusion(BlockState, ModelData, RenderType) → TriState}.
 * Forge's {@link net.minecraftforge.client.extensions.IForgeBakedModel IForgeBakedModel} only has
 * {@code useAmbientOcclusion(BlockState, RenderType) → boolean}.
 *
 * <p>Called from bytecode rewritten by {@link org.xiyu.reforged.asm.BytecodeRewriter}.
 */
public final class BakedModelBridge {

    private BakedModelBridge() {}

    /**
     * Redirect target for NeoForge's
     * {@code BakedModel.useAmbientOcclusion(BlockState, ModelData, RenderType) → TriState}.
     *
     * <p>Returns {@link TriState#DEFAULT} to match NeoForge's default implementation,
     * which lets the caller decide AO based on other factors (light emission, global setting).
     * No standard BakedModel overrides this method in NeoForge — they all rely on DEFAULT.
     */
    public static TriState useAmbientOcclusion(BakedModel model, BlockState state,
                                                ModelData data, RenderType renderType) {
        return TriState.DEFAULT;
    }
}
