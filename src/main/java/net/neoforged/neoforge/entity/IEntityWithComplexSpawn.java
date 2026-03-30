package net.neoforged.neoforge.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Interface for entities that require additional spawn data beyond standard entity data.
 * Entities implementing this will have extra data sent to the client on spawn.
 * <p>
 * Bridges to Forge's {@link net.minecraftforge.entity.IEntityAdditionalSpawnData}.
 */
public interface IEntityWithComplexSpawn extends net.minecraftforge.entity.IEntityAdditionalSpawnData {
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

    // Bridge to Forge's FriendlyByteBuf-based interface
    @Override
    default void writeSpawnData(FriendlyByteBuf buffer) {
        if (buffer instanceof RegistryFriendlyByteBuf registryBuf) {
            writeSpawnData(registryBuf);
        } else {
            // In 1.21.1, network buffers should always be RegistryFriendlyByteBuf
            // Wrap with server registry access as fallback
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                writeSpawnData(new RegistryFriendlyByteBuf(buffer, server.registryAccess()));
            }
        }
    }

    @Override
    default void readSpawnData(FriendlyByteBuf buffer) {
        if (buffer instanceof RegistryFriendlyByteBuf registryBuf) {
            readSpawnData(registryBuf);
        } else {
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                readSpawnData(new RegistryFriendlyByteBuf(buffer, server.registryAccess()));
            }
        }
    }
}
