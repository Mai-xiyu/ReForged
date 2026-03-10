package net.neoforged.neoforge.client;

import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge NamedRenderTypeManager stub — delegates to Forge's implementation,
 * converting the result to NeoForge's RenderTypeGroup.
 */
public final class NamedRenderTypeManager {
    private NamedRenderTypeManager() {}

    public static RenderTypeGroup get(ResourceLocation name) {
        return RenderTypeGroup.fromForge(net.minecraftforge.client.NamedRenderTypeManager.get(name));
    }

	public static RenderTypeGroup getOrDefault(ResourceLocation name, RenderTypeGroup fallback) {
		RenderTypeGroup result = get(name);
		return result.isEmpty() ? fallback : result;
	}
}
