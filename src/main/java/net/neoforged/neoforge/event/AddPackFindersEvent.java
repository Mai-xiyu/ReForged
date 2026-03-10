package net.neoforged.neoforge.event;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fired to allow mods to add custom resource/data pack finders.
 */
public class AddPackFindersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final PackType packType;
    private final List<RepositorySource> sources = new ArrayList<>();

    public AddPackFindersEvent(PackType packType) {
        this.packType = packType;
    }

    public PackType getPackType() {
        return packType;
    }

    /**
     * Adds a repository source for pack discovery.
     */
    public void addRepositorySource(RepositorySource source) {
        sources.add(source);
    }

    public List<RepositorySource> getSources() {
        return sources;
    }
}
