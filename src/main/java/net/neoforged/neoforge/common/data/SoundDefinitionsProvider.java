package net.neoforged.neoforge.common.data;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data provider for the {@code sounds.json} file.
 */
public abstract class SoundDefinitionsProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final PackOutput output;
    private final String modId;
    private final ExistingFileHelper helper;
    private final Map<String, SoundDefinition> sounds = new LinkedHashMap<>();

    protected SoundDefinitionsProvider(final PackOutput output, final String modId, final ExistingFileHelper helper) {
        this.output = output;
        this.modId = modId;
        this.helper = helper;
    }

    public abstract void registerSounds();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.sounds.clear();
        this.registerSounds();
        this.validate();
        if (!this.sounds.isEmpty()) {
            return this.save(cache, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                    .resolve(this.modId).resolve("sounds.json"));
        }
        return CompletableFuture.allOf();
    }

    @Override
    public String getName() { return "Sound Definitions"; }

    protected static SoundDefinition definition() { return SoundDefinition.definition(); }

    protected static SoundDefinition.Sound sound(final ResourceLocation name, final SoundDefinition.SoundType type) {
        return SoundDefinition.Sound.sound(name, type);
    }

    protected static SoundDefinition.Sound sound(final ResourceLocation name) {
        return sound(name, SoundDefinition.SoundType.SOUND);
    }

    protected static SoundDefinition.Sound sound(final String name, final SoundDefinition.SoundType type) {
        return sound(ResourceLocation.parse(name), type);
    }

    protected static SoundDefinition.Sound sound(final String name) {
        return sound(ResourceLocation.parse(name));
    }

    protected void add(final Supplier<SoundEvent> soundEvent, final SoundDefinition definition) {
        this.add(soundEvent.get(), definition);
    }

    protected void add(final SoundEvent soundEvent, final SoundDefinition definition) {
        this.add(soundEvent.getLocation(), definition);
    }

    protected void add(final ResourceLocation soundEvent, final SoundDefinition definition) {
        this.addSounds(soundEvent.getPath(), definition);
    }

    protected void add(final String soundEvent, final SoundDefinition definition) {
        this.add(ResourceLocation.parse(soundEvent), definition);
    }

    private void addSounds(final String soundEvent, final SoundDefinition definition) {
        if (this.sounds.put(soundEvent, definition) != null) {
            throw new IllegalStateException("Sound event '" + this.modId + ":" + soundEvent + "' already exists");
        }
    }

    private void validate() {
        final List<String> notValid = this.sounds.entrySet().stream()
                .filter(it -> !this.validate(it.getKey(), it.getValue()))
                .map(Map.Entry::getKey)
                .map(it -> this.modId + ":" + it)
                .toList();
        if (!notValid.isEmpty()) {
            throw new IllegalStateException("Found invalid sound events: " + notValid);
        }
    }

    private boolean validate(final String name, final SoundDefinition def) {
        return def.soundList().stream().allMatch(it -> this.validate(name, it));
    }

    private boolean validate(final String name, final SoundDefinition.Sound sound) {
        return switch (sound.type()) {
            case SOUND -> this.validateSound(name, sound.name());
            case EVENT -> this.validateEvent(name, sound.name());
        };
    }

    private boolean validateSound(final String soundName, final ResourceLocation name) {
        final boolean valid = this.helper.exists(name,
                new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".ogg", "sounds"));
        if (!valid) {
            LOGGER.warn("Unable to find OGG file for sound event '{}'", soundName);
        }
        return valid;
    }

    private boolean validateEvent(final String soundName, final ResourceLocation name) {
        final boolean valid = this.sounds.containsKey(soundName) || BuiltInRegistries.SOUND_EVENT.containsKey(name);
        if (!valid) {
            LOGGER.warn("Unable to find event '{}' referenced from '{}'", name, soundName);
        }
        return valid;
    }

    private CompletableFuture<?> save(final CachedOutput cache, final Path targetFile) {
        return DataProvider.saveStable(cache, this.mapToJson(this.sounds), targetFile);
    }

    private JsonObject mapToJson(final Map<String, SoundDefinition> map) {
        final JsonObject obj = new JsonObject();
        map.forEach((k, v) -> obj.add(k, v.serialize()));
        return obj;
    }
}
