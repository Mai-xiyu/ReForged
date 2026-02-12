package org.xiyu.reforged.asm;

import org.objectweb.asm.commons.Remapper;

/**
 * ReForgedRemapper — ASM {@link Remapper} that delegates to {@link MappingRegistry}.
 *
 * <p>This is plugged into ASM's {@code ClassRemapper} so that all class references,
 * field descriptors, method descriptors, and annotation values are automatically
 * rewritten according to our mapping table.
 */
public final class ReForgedRemapper extends Remapper {

    private final MappingRegistry registry;

    public ReForgedRemapper() {
        this.registry = MappingRegistry.getInstance();
    }

    public ReForgedRemapper(MappingRegistry registry) {
        this.registry = registry;
    }

    /**
     * Core override — ASM calls this for every internal class name it encounters
     * in constant pools, descriptors, signatures, annotations, etc.
     */
    @Override
    public String map(String internalName) {
        return registry.remapClass(internalName);
    }
}
