package org.xiyu.reforged.shim.attachment;

import java.util.function.Supplier;

/**
 * AttachmentType — Shim for NeoForge's data attachment type registry.
 *
 * <p>NeoForge mods create attachment types via:</p>
 * <pre>
 * public static final AttachmentType&lt;MyData&gt; MY_ATTACHMENT =
 *     AttachmentType.builder(() -&gt; new MyData()).build();
 * </pre>
 *
 * @param <T> the attachment data type
 */
public final class AttachmentType<T> {

    private final String id;
    private final Supplier<T> defaultValue;
    private final boolean serialize;

    private AttachmentType(Builder<T> builder) {
        this.id = builder.id != null ? builder.id : "unknown";
        this.defaultValue = builder.defaultValue;
        this.serialize = builder.serialize;
    }

    public String id() {
        return id;
    }

    public Supplier<T> defaultValueSupplier() {
        return defaultValue;
    }

    public boolean shouldSerialize() {
        return serialize;
    }

    // ─── Builder (matches NeoForge's API) ──────────────────────

    /**
     * Create a builder with a default value supplier.
     */
    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(defaultValue);
    }

    /**
     * Create a builder without a default value.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>(null);
    }

    public static final class Builder<T> {
        private Supplier<T> defaultValue;
        private String id;
        private boolean serialize = false;
        private Object serializer; // codec reference, not used in shim

        Builder(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * Set the serializer (NeoForge uses a Codec).
         * In our shim, we store the reference but don't actually serialize.
         */
        public Builder<T> serialize(Object codecOrSerializer) {
            this.serializer = codecOrSerializer;
            this.serialize = true;
            return this;
        }

        /**
         * Set the serializer with a condition.
         */
        public Builder<T> serialize(Object codec, Object copyHandler) {
            this.serializer = codec;
            this.serialize = true;
            return this;
        }

        /**
         * Copy on death flag.
         */
        public Builder<T> copyOnDeath() {
            return this;
        }

        /**
         * Copy handler.
         */
        public Builder<T> copyHandler(Object handler) {
            return this;
        }

        /**
         * Set a custom comparator.
         */
        public Builder<T> comparator(Object comparator) {
            return this;
        }

        /**
         * Build the attachment type.
         */
        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }

        // Allow setting id
        Builder<T> id(String id) {
            this.id = id;
            return this;
        }
    }
}
