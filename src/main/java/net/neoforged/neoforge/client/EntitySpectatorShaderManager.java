package net.neoforged.neoforge.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-compatible spectator shader lookup.
 */
public final class EntitySpectatorShaderManager {
    private EntitySpectatorShaderManager() {}

    @Nullable
    public static ResourceLocation get(EntityType<?> entityType) {
        return net.minecraftforge.client.EntitySpectatorShaderManager.get(entityType);
    }
}
