package org.xiyu.reforged.shim.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * CustomPacketPayload — Shim interface matching NeoForge's network payload contract.
 *
 * <p>NeoForge mods implement this interface for custom network packets.
 * After bytecode rewriting, references to NeoForge's {@code CustomPacketPayload}
 * are redirected here.</p>
 *
 * <p>Note: Minecraft 1.21.1 already has {@code CustomPacketPayload} in the
 * vanilla codebase. We re-export it here so the package mapping works.</p>
 */
public interface ReForgedPayload {

    /**
     * The payload type identifier.
     * NeoForge: {@code CustomPacketPayload.Type<T> type()}
     */
    // Marker interface — actual implementations use Minecraft's CustomPacketPayload

    /**
     * Helper to create a Type from a ResourceLocation.
     */
    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String id) {
        return new CustomPacketPayload.Type<>(ResourceLocation.parse(id));
    }
}
