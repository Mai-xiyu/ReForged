package net.neoforged.neoforge.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.neoforged.fml.config.IConfigSpec;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Proxy: NeoForge's ModConfigSpec → wraps Forge's ForgeConfigSpec.
 *
 * <p>This class wraps a ForgeConfigSpec and implements IConfigSpec.
 * The Builder uses composition and returns ModConfigSpec from build()/configure().</p>
 */
public final class ModConfigSpec implements IConfigSpec {

    private final ForgeConfigSpec forgeSpec;

    ModConfigSpec(ForgeConfigSpec forgeSpec) {
        this.forgeSpec = forgeSpec;
    }

    /** Get the underlying ForgeConfigSpec */
    public ForgeConfigSpec getForgeSpec() { return forgeSpec; }

    @Override
    public boolean isEmpty() { return false; }

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
        public ModConfigSpec build() { return new ModConfigSpec(delegate.build()); }

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

        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue));
        }

        @SafeVarargs
        public final <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            return new EnumValue<>(delegate.defineEnum(path, defaultValue, acceptableValues));
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineList(String path,
                List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            List<? extends T> safeDefault = defaultValue != null ? defaultValue : List.of();
            return new ConfigValue<>(delegate.defineList(path, safeDefault, elementValidator));
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineList(String path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineList(path, safeSupplier, elementValidator));
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineListAllowEmpty(path, safeSupplier, elementValidator));
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path,
                List<? extends T> defaultValue, Supplier<List<? extends T>> defaultSupplier,
                Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineListAllowEmpty(
                    java.util.Collections.singletonList(path), safeSupplier, elementValidator));
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path,
                Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            Supplier<List<? extends T>> safeSupplier = () -> {
                List<? extends T> v = defaultSupplier.get();
                return v != null ? v : List.of();
            };
            return new ConfigValue<>(delegate.defineListAllowEmpty(
                    java.util.Collections.singletonList(path), safeSupplier, elementValidator));
        }

        // NeoForge variant: List<String> path, Supplier defaultSupplier, Supplier (ignored), Predicate
        @SuppressWarnings("unchecked")
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path,
                Supplier<List<? extends T>> defaultSupplier, Supplier<List<? extends T>> supplier2,
                Predicate<Object> elementValidator) {
            return new ConfigValue<>(delegate.defineListAllowEmpty(path, defaultSupplier, elementValidator));
        }
    }
}
