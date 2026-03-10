package net.neoforged.neoforge.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * An empty/no-op PackResources implementation, useful as a placeholder.
 */
public class EmptyPackResources implements PackResources {
    private final PackLocationInfo info;

    public EmptyPackResources(PackLocationInfo info) {
        this.info = info;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {
    }
}
