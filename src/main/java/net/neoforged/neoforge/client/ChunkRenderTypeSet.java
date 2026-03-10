package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class ChunkRenderTypeSet {
    private static final ChunkRenderTypeSet ALL = new ChunkRenderTypeSet(Collections.emptySet(), true);
    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(Collections.emptySet(), false);

    private final Set<RenderType> types;
    private final boolean all;

    private ChunkRenderTypeSet(Set<RenderType> types, boolean all) {
        this.types = types;
        this.all = all;
    }

    public static ChunkRenderTypeSet all() {
        return ALL;
    }

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet of(RenderType... types) {
        Set<RenderType> set = new HashSet<>();
        Collections.addAll(set, types);
        return new ChunkRenderTypeSet(Collections.unmodifiableSet(set), false);
    }

    public boolean contains(RenderType type) {
        return all || types.contains(type);
    }

    public boolean isEmpty() {
        return !all && types.isEmpty();
    }
}
