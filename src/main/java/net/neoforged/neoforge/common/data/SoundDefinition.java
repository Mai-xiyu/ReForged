package net.neoforged.neoforge.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Contains all the data to completely define a sound event.
 */
public final class SoundDefinition {

    public static final class Sound {
        private static final SoundType DEFAULT_TYPE = SoundType.SOUND;
        private static final float DEFAULT_VOLUME = 1.0F;
        private static final float DEFAULT_PITCH = 1.0F;
        private static final int DEFAULT_WEIGHT = 1;
        private static final boolean DEFAULT_STREAM = false;
        private static final int DEFAULT_ATTENUATION_DISTANCE = 16;
        private static final boolean DEFAULT_PRELOAD = false;

        private final ResourceLocation name;
        private final SoundType type;
        private float volume = DEFAULT_VOLUME;
        private float pitch = DEFAULT_PITCH;
        private int weight = DEFAULT_WEIGHT;
        private boolean stream = DEFAULT_STREAM;
        private int attenuationDistance = DEFAULT_ATTENUATION_DISTANCE;
        private boolean preload = DEFAULT_PRELOAD;

        private Sound(final ResourceLocation name, final SoundType type) {
            this.name = name;
            this.type = type;
        }

        public static Sound sound(final ResourceLocation name, final SoundType type) {
            return new Sound(name, type);
        }

        public Sound volume(final double volume) { return this.volume((float) volume); }

        public Sound volume(final float volume) {
            if (volume <= 0.0F) throw new IllegalArgumentException("Volume must be positive for sound " + this.name + ", but got " + volume);
            this.volume = volume;
            return this;
        }

        public Sound pitch(final double pitch) { return this.pitch((float) pitch); }

        public Sound pitch(final float pitch) {
            if (pitch <= 0.0F) throw new IllegalArgumentException("Pitch must be positive for sound " + this.name + ", but got " + pitch);
            this.pitch = pitch;
            return this;
        }

        public Sound weight(final int weight) {
            if (weight <= 0) throw new IllegalArgumentException("Weight has to be positive in sound " + this.name + ", but got " + weight);
            this.weight = weight;
            return this;
        }

        public Sound stream() { return this.stream(true); }
        public Sound stream(final boolean stream) { this.stream = stream; return this; }

        public Sound attenuationDistance(final int attenuationDistance) {
            this.attenuationDistance = attenuationDistance;
            return this;
        }

        public Sound preload() { return this.preload(true); }
        public Sound preload(final boolean preload) { this.preload = preload; return this; }

        ResourceLocation name() { return this.name; }
        SoundType type() { return this.type; }

        JsonElement serialize() {
            if (this.canBeInShortForm()) {
                return new JsonPrimitive(this.stripMcPrefix(this.name));
            }
            final JsonObject object = new JsonObject();
            object.addProperty("name", this.stripMcPrefix(this.name));
            if (this.type != DEFAULT_TYPE) object.addProperty("type", this.type.jsonString);
            if (this.volume != DEFAULT_VOLUME) object.addProperty("volume", this.volume);
            if (this.pitch != DEFAULT_PITCH) object.addProperty("pitch", this.pitch);
            if (this.weight != DEFAULT_WEIGHT) object.addProperty("weight", this.weight);
            if (this.stream != DEFAULT_STREAM) object.addProperty("stream", this.stream);
            if (this.preload != DEFAULT_PRELOAD) object.addProperty("preload", this.preload);
            if (this.attenuationDistance != DEFAULT_ATTENUATION_DISTANCE)
                object.addProperty("attenuation_distance", this.attenuationDistance);
            return object;
        }

        private boolean canBeInShortForm() {
            return this.type == DEFAULT_TYPE && this.volume == DEFAULT_VOLUME && this.pitch == DEFAULT_PITCH
                    && this.weight == DEFAULT_WEIGHT && this.stream == DEFAULT_STREAM
                    && this.attenuationDistance == DEFAULT_ATTENUATION_DISTANCE && this.preload == DEFAULT_PRELOAD;
        }

        private String stripMcPrefix(final ResourceLocation name) {
            return "minecraft".equals(name.getNamespace()) ? name.getPath() : name.toString();
        }
    }

    public enum SoundType {
        SOUND("sound"),
        EVENT("event");

        private final String jsonString;

        SoundType(final String jsonString) { this.jsonString = jsonString; }
    }

    private final List<Sound> sounds = new ArrayList<>();
    private boolean replace = false;
    private String subtitle = null;

    private SoundDefinition() {}

    public static SoundDefinition definition() { return new SoundDefinition(); }

    public SoundDefinition replace(final boolean replace) { this.replace = replace; return this; }

    public SoundDefinition subtitle(@Nullable final String subtitle) { this.subtitle = subtitle; return this; }

    public SoundDefinition with(final Sound sound) { this.sounds.add(sound); return this; }

    public SoundDefinition with(final Sound... sounds) { this.sounds.addAll(Arrays.asList(sounds)); return this; }

    List<Sound> soundList() { return this.sounds; }

    JsonObject serialize() {
        if (this.sounds.isEmpty()) throw new IllegalStateException("Unable to serialize a sound definition that has no sounds!");
        final JsonObject object = new JsonObject();
        if (this.replace) object.addProperty("replace", true);
        if (this.subtitle != null) object.addProperty("subtitle", this.subtitle);
        final JsonArray sounds = new JsonArray();
        this.sounds.stream().map(Sound::serialize).forEach(sounds::add);
        object.add("sounds", sounds);
        return object;
    }
}
