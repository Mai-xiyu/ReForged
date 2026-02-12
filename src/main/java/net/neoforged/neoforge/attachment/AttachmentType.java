package net.neoforged.neoforge.attachment;

import java.util.function.Supplier;

/** Proxy: NeoForge's AttachmentType for entity data attachments */
public class AttachmentType<T> {
    private final Supplier<T> defaultValue;

    private AttachmentType(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Supplier<T> defaultValueSupplier() { return defaultValue; }

    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(defaultValue);
    }

    public static class Builder<T> {
        private final Supplier<T> defaultValue;

        public Builder(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Builder<T> serialize(Object serializer) { return this; }
        public Builder<T> copyOnDeath() { return this; }
        public Builder<T> copyHandler(Object handler) { return this; }

        public AttachmentType<T> build() {
            return new AttachmentType<>(defaultValue);
        }
    }
}
