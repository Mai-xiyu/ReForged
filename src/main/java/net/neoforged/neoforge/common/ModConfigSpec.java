package net.neoforged.neoforge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.neoforged.fml.config.IConfigSpec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Proxy: NeoForge's ModConfigSpec → wraps Forge's ForgeConfigSpec.
 *
 * <p>This class wraps a ForgeConfigSpec and implements both
 * NeoForge's IConfigSpec and Forge's IConfigSpec so it can be cast to either.</p>
 */
public final class ModConfigSpec implements IConfigSpec,
        net.minecraftforge.fml.config.IConfigSpec<ModConfigSpec> {

    private static final Map<ForgeConfigSpec, ModConfigSpec> WRAPPERS = new ConcurrentHashMap<>();

    private final ForgeConfigSpec forgeSpec;

    ModConfigSpec(ForgeConfigSpec forgeSpec) {
        this.forgeSpec = forgeSpec;
    }

    public static ModConfigSpec wrap(ForgeConfigSpec forgeSpec) {
        return WRAPPERS.computeIfAbsent(forgeSpec, ModConfigSpec::new);
    }

    /** Get the underlying ForgeConfigSpec */
    public ForgeConfigSpec getForgeSpec() { return forgeSpec; }

    @Override
    public boolean isEmpty() { return false; }

    public boolean isLoaded() {
        return forgeSpec.isLoaded();
    }

    public UnmodifiableConfig getSpec() {
        return forgeSpec.getSpec();
    }

    public UnmodifiableConfig getValues() {
        return forgeSpec.getValues();
    }

    public void afterReload() {
        forgeSpec.afterReload();
    }

    // ══════════════════════════════════════════════════════════
    //  Forge IConfigSpec<ModConfigSpec> implementation
    // ══════════════════════════════════════════════════════════

    @Override
    public ModConfigSpec self() { return this; }

    @Override
    public void acceptConfig(CommentedConfig data) { forgeSpec.acceptConfig(data); }

    @Override
    public boolean isCorrecting() { return forgeSpec.isCorrecting(); }

    @Override
    public boolean isCorrect(CommentedConfig commentedFileConfig) { return forgeSpec.isCorrect(commentedFileConfig); }

    @Override
    public int correct(CommentedConfig commentedFileConfig) { return forgeSpec.correct(commentedFileConfig); }

    // ══════════════════════════════════════════════════════════
    //  UnmodifiableConfig implementation (required by Forge IConfigSpec)
    // ══════════════════════════════════════════════════════════

    @Override
    public <T> T getRaw(List<String> path) { return forgeSpec.getRaw(path); }

    @Override
    public boolean contains(List<String> path) { return forgeSpec.contains(path); }

    @Override
    public int size() { return forgeSpec.size(); }

    @Override
    public Map<String, Object> valueMap() { return forgeSpec.valueMap(); }

    @Override
    public Set<? extends UnmodifiableConfig.Entry> entrySet() { return forgeSpec.entrySet(); }

    @Override
    public ConfigFormat<?> configFormat() { return forgeSpec.configFormat(); }

    public void resetCaches(RestartType restartType) {
        // Forge doesn't track NeoForge restart granularities, so clear all cached values.
        forgeSpec.afterReload();
    }

    public void validateSpec(net.neoforged.fml.config.ModConfig config) {
        // Forge already validated the underlying spec when loading the config.
    }

    // ══════════════════════════════════════════════════════════
    //  Value wrapper types
    // ══════════════════════════════════════════════════════════

    public static class ConfigValue<T> implements Supplier<T> {
        protected final ForgeConfigSpec.ConfigValue<T> delegate;

        public ConfigValue(ForgeConfigSpec.ConfigValue<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() { return delegate.get(); }
        public T getDefault() { return delegate.getDefault(); }
        public void set(T value) { delegate.set(value); }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(ForgeConfigSpec.BooleanValue delegate) { super(delegate); }
    }

    public static class IntValue extends ConfigValue<Integer> {
        private final ForgeConfigSpec.IntValue intDelegate;
        public IntValue(ForgeConfigSpec.IntValue delegate) { super(delegate); this.intDelegate = delegate; }
        public int getAsInt() { return intDelegate.get(); }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        public DoubleValue(ForgeConfigSpec.DoubleValue delegate) { super(delegate); }
    }

    public static class LongValue extends ConfigValue<Long> {
        public LongValue(ForgeConfigSpec.LongValue delegate) { super(delegate); }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        public EnumValue(ForgeConfigSpec.EnumValue<T> delegate) { super(delegate); }
    }

    // ══════════════════════════════════════════════════════════
    //  Builder — composition wrapper
    // ══════════════════════════════════════════════════════════

    public static class Builder {
        private final ForgeConfigSpec.Builder delegate = new ForgeConfigSpec.Builder();

        // ── Section methods ─────────────────────────────────
        public Builder push(String path) { delegate.push(path); return this; }
        public Builder push(List<String> path) { delegate.push(path); return this; }
        public Builder pop() { delegate.pop(); return this; }
        public Builder pop(int count) { delegate.pop(count); return this; }

        // ── Comment methods ─────────────────────────────────
        public Builder comment(String comment) { delegate.comment(comment); return this; }
        public Builder comment(String... comment) { delegate.comment(comment); return this; }

        // ── Translation/WorldRestart ────────────────────────
        public Builder translation(String translationKey) { delegate.translation(translationKey); return this; }
        public Builder worldRestart() { delegate.worldRestart(); return this; }

        // ── Build — returns ModConfigSpec ────────────────────
        public ModConfigSpec build() { return ModConfigSpec.wrap(delegate.build()); }

        // ── configure() — returns Pair<T, ModConfigSpec> ────
        public <T> org.apache.commons.lang3.tuple.Pair<T, ModConfigSpec> configure(
                Function<Builder, T> consumer) {
            Builder builder = new Builder();
            T obj = consumer.apply(builder);
            ModConfigSpec spec = builder.build();
            return org.apache.commons.lang3.tuple.Pair.of(obj, spec);
        }

        // ── Value define methods ────────────────────────────

        public <T> ConfigValue<T> define(String path, T defaultValue) {
            return new ConfigValue<>(delegate.define(path, defaultValue));
        }

        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier,
                Predicate<Object> validator) {
            return new ConfigValue<>(delegate.define(path, defaultSupplier, validator));
        }

        public BooleanValue define(String path, boolean defaultValue) {
            return new BooleanValue(delegate.define(path, defaultValue));
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return new IntValue(delegate.defineInRange(path, defaultValue, min, max));
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return new DoubleValue(delegate.defineInRange(path, defaultValue, min, max));
        }

        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return new LongValue(delegate.defineInRange(path, defaultValue, min, max));
        }

        // ── defineEnum overloads ─────────────────────────

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter));
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, acceptableValues));
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, acceptableValues));
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, acceptableValues));
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, acceptableValues));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, validator));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, validator));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, validator));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, converter, validator));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return new EnumValue<>(delegate.defineEnum(path, defaultSupplier, validator, clazz));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            return new EnumValue<>(delegate.defineEnum(path, defaultSupplier, converter, validator, clazz));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return new EnumValue<>(delegate.defineEnum(path, defaultSupplier, validator, clazz));
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            return new EnumValue<>(delegate.defineEnum(path, defaultSupplier, converter, validator, clazz));
        }

        public <T> ConfigValue<List<? extends T>> defineList(String path,
                List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            List<? extends T> safeDefault = defaultValue != null ? defaultValue : List.of();
            return new ConfigValue<>(delegate.defineList(path, safeDefault, elementValidator));
        }

        public <T> ConfigValue<List<? extends T>> defineList(String path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineList(path, safeSupplier, elementValidator));
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineListAllowEmpty(path, safeSupplier, elementValidator));
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path,
                List<? extends T> defaultValue, Supplier<T> newElementSupplier,
                Predicate<Object> elementValidator) {
            // NeoForge's 4-arg overload: the Supplier<T> is for new element creation (config UI),
            // not for providing the default list. We ignore it and use defaultValue directly.
            List<? extends T> safeDefault = defaultValue != null ? defaultValue : List.of();
            return new ConfigValue<>(delegate.defineListAllowEmpty(
                    java.util.Collections.singletonList(path), () -> safeDefault, elementValidator));
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineListAllowEmpty(
                    java.util.Collections.singletonList(path), safeSupplier, elementValidator));
        }

        // NeoForge variant: List<String> path, Supplier defaultSupplier, Supplier<T> newElement (ignored), Predicate
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path,
                Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier,
                Predicate<Object> elementValidator) {
            return new ConfigValue<>(delegate.defineListAllowEmpty(path, defaultSupplier, elementValidator));
        }
    }

    public enum RestartType {
        NONE(),
        WORLD(net.neoforged.fml.config.ModConfig.Type.STARTUP),
        GAME(net.neoforged.fml.config.ModConfig.Type.STARTUP);

        private final java.util.EnumSet<net.neoforged.fml.config.ModConfig.Type> invalidTypes;

        RestartType(net.neoforged.fml.config.ModConfig.Type... invalidTypes) {
            this.invalidTypes = invalidTypes.length == 0
                    ? java.util.EnumSet.noneOf(net.neoforged.fml.config.ModConfig.Type.class)
                    : java.util.EnumSet.of(invalidTypes[0], java.util.Arrays.copyOfRange(invalidTypes, 1, invalidTypes.length));
        }

        public boolean isValid(net.neoforged.fml.config.ModConfig.Type type) {
            return !invalidTypes.contains(type);
        }

        public RestartType with(RestartType other) {
            return ordinal() >= other.ordinal() ? this : other;
        }
    }
}
