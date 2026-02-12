package net.neoforged.neoforge.common.data;

/** Proxy: NeoForge's AdvancementProvider */
public class AdvancementProvider {
    @FunctionalInterface
    public interface AdvancementGenerator {
        void generate(Object registries, Object saver, Object existingFileHelper);
    }
}
