package net.neoforged.neoforge.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

/**
 * Data provider for atlas configuration files.
 * <p>An atlas configuration is bound to a specific texture atlas and allows adding
 * additional textures to the atlas by adding {@link SpriteSource}s.</p>
 */
public abstract class SpriteSourceProvider extends JsonCodecProvider<List<SpriteSource>> {
    protected static final ResourceLocation BLOCKS_ATLAS = ResourceLocation.withDefaultNamespace("blocks");
    protected static final ResourceLocation BANNER_PATTERNS_ATLAS = ResourceLocation.withDefaultNamespace("banner_patterns");
    protected static final ResourceLocation BEDS_ATLAS = ResourceLocation.withDefaultNamespace("beds");
    protected static final ResourceLocation CHESTS_ATLAS = ResourceLocation.withDefaultNamespace("chests");
    protected static final ResourceLocation SHIELD_PATTERNS_ATLAS = ResourceLocation.withDefaultNamespace("shield_patterns");
    protected static final ResourceLocation SHULKER_BOXES_ATLAS = ResourceLocation.withDefaultNamespace("shulker_boxes");
    protected static final ResourceLocation SIGNS_ATLAS = ResourceLocation.withDefaultNamespace("signs");
    protected static final ResourceLocation MOB_EFFECTS_ATLAS = ResourceLocation.withDefaultNamespace("mob_effects");
    protected static final ResourceLocation PAINTINGS_ATLAS = ResourceLocation.withDefaultNamespace("paintings");
    protected static final ResourceLocation PARTICLES_ATLAS = ResourceLocation.withDefaultNamespace("particles");

    private final Map<ResourceLocation, SourceList> atlases = new HashMap<>();

    public SpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                String modId, ExistingFileHelper existingFileHelper) {
        super(output, PackOutput.Target.RESOURCE_PACK, "atlases", PackType.CLIENT_RESOURCES,
                SpriteSources.FILE_CODEC, lookupProvider, modId, existingFileHelper);
    }

    /**
     * Get or create a {@link SourceList} for the given atlas.
     */
    protected final SourceList atlas(ResourceLocation id) {
        return atlases.computeIfAbsent(id, i -> {
            final SourceList newAtlas = new SourceList(new ArrayList<>());
            unconditional(i, newAtlas.sources());
            return newAtlas;
        });
    }

    protected record SourceList(List<SpriteSource> sources) {
        public SourceList addSource(SpriteSource source) {
            sources.add(source);
            return this;
        }
    }
}
