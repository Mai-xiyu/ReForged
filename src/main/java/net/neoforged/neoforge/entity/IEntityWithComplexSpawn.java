package net.neoforged.neoforge.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Interface for entities that require additional spawn data beyond standard entity data.
 * Entities implementing this will have extra data sent to the client on spawn.
 */
public interface IEntityWithComplexSpawn {
    /**
     * Write additional spawn data to the buffer.
     * @param buffer the buffer to write to
     */
    void writeSpawnData(RegistryFriendlyByteBuf buffer);

    /**
     * Read additional spawn data from the buffer.
     * @param additionalData the buffer to read from
     */
    void readSpawnData(RegistryFriendlyByteBuf additionalData);
}
