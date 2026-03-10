package net.neoforged.neoforge.common;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * NeoForge shim layer: bridges NeoForge API surface to Forge 1.21.1 runtime.
 * Mods compiled against NeoForge will reference fields and methods here, which
 * delegate to the underlying Forge equivalents where they exist.
 */
public class NeoForgeMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker NEOFORGEMOD = MarkerManager.getMarker("NEOFORGE");

    private static boolean milkFluidEnabled = false;
    private static boolean enableMergedAttributeTooltips = false;
    private static boolean enableProperFilenameValidation = false;

    // ── Enable flags ─────────────────────────────────────────────────────

    /**
     * NeoForge method called by mods (e.g. Create) to enable milk as a fluid.
     * On Forge this delegates to {@link ForgeMod#enableMilkFluid()}.
     */
    public static void enableMilkFluid() {
        milkFluidEnabled = true;
        ForgeMod.enableMilkFluid();
    }

    /**
     * Enables merged (combined) attribute tooltips instead of per-slot.
     */
    public static void enableMergedAttributeTooltips() {
        enableMergedAttributeTooltips = true;
    }

    /**
     * Returns whether merged attribute tooltips are enabled.
     */
    public static boolean shouldMergeAttributeTooltips() {
        return enableMergedAttributeTooltips;
    }

    /**
     * Enables proper filename validation for resource paths.
     */
    public static void enableProperFilenameValidation() {
        enableProperFilenameValidation = true;
    }

    /**
     * Returns whether proper filename validation is enabled.
     */
    public static boolean getProperFilenameValidation() {
        return enableProperFilenameValidation;
    }

    // ── Attribute holders bridged from Forge's ForgeMod ──────────────────

    /**
     * Swim speed attribute — controls how fast entities swim.
     * Bridged from {@link ForgeMod#SWIM_SPEED}.
     */
    public static final Holder<Attribute> SWIM_SPEED = lazyHolder(ForgeMod.SWIM_SPEED);

    /**
     * Name tag render distance attribute.
     * Bridged from {@link ForgeMod#NAMETAG_DISTANCE}.
     */
    public static final Holder<Attribute> NAMETAG_DISTANCE = lazyHolder(ForgeMod.NAMETAG_DISTANCE);

    /**
     * Creative flight attribute — NeoForge-only, no Forge equivalent.
     * Provides a dummy holder that returns a no-op RangedAttribute.
     */
    public static final Holder<Attribute> CREATIVE_FLIGHT = Holder.direct(
        new RangedAttribute("neoforge.creative_flight", 0.0D, 0.0D, 1.0D).setSyncable(true)
    );

    // ── FluidType holders bridged from Forge's ForgeMod ──────────────────

    public static final Holder<FluidType> EMPTY_TYPE = lazyHolder(ForgeMod.EMPTY_TYPE);
    public static final Holder<FluidType> WATER_TYPE = lazyHolder(ForgeMod.WATER_TYPE);
    public static final Holder<FluidType> LAVA_TYPE  = lazyHolder(ForgeMod.LAVA_TYPE);

    // ── Milk fluid holders ───────────────────────────────────────────────

    /**
     * Milk FluidType — optional, only registered if {@link #enableMilkFluid()} was called.
     * Bridged from {@link ForgeMod#MILK_TYPE}.
     */
    public static final Supplier<FluidType> MILK_TYPE = () -> ForgeMod.MILK_TYPE.get();

    // ── Damage types ─────────────────────────────────────────────────────

    /**
     * Damage type resource key for NeoForge's poison damage type.
     */
    public static final ResourceKey<DamageType> POISON_DAMAGE = ResourceKey.create(
        net.minecraft.core.registries.Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath("neoforge", "poison")
    );

    // ── HolderSetType singletons ─────────────────────────────────────────

    /** HolderSetType for {@link net.neoforged.neoforge.registries.holdersets.AnyHolderSet}. */
    public static final HolderSetType ANY_HOLDER_SET = new HolderSetType() {
        @Override public <T> com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, com.mojang.serialization.Codec<Holder<T>> holderCodec, boolean forceList) {
            return net.minecraft.resources.RegistryOps.retrieveRegistryLookup(registryKey)
                    .xmap(net.neoforged.neoforge.registries.holdersets.AnyHolderSet::new,
                           net.neoforged.neoforge.registries.holdersets.AnyHolderSet::registryLookup);
        }
        @Override public <T> net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeStreamCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey) {
            return new net.minecraft.network.codec.StreamCodec<>() {
                @Override
                public net.neoforged.neoforge.registries.holdersets.AnyHolderSet<T> decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
                    return new net.neoforged.neoforge.registries.holdersets.AnyHolderSet<>(buf.registryAccess().lookupOrThrow(registryKey));
                }
                @Override
                public void encode(net.minecraft.network.RegistryFriendlyByteBuf buf, net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T> set) {
                    // AnyHolderSet has no payload — just the type tag
                }
            };
        }
    };

    /** HolderSetType for {@link net.neoforged.neoforge.registries.holdersets.AndHolderSet}. */
    public static final HolderSetType AND_HOLDER_SET = new HolderSetType() {
        @Override public <T> com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, com.mojang.serialization.Codec<Holder<T>> holderCodec, boolean forceList) {
            return net.minecraft.resources.HolderSetCodec.create(registryKey, holderCodec, forceList)
                    .listOf()
                    .xmap(net.neoforged.neoforge.registries.holdersets.AndHolderSet::new,
                           net.neoforged.neoforge.registries.holdersets.CompositeHolderSet::homogenize)
                    .fieldOf("values");
        }
        @Override public <T> net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeStreamCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey) {
            return net.minecraft.network.codec.ByteBufCodecs.<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.core.HolderSet<T>>list()
                    .apply(net.minecraft.network.codec.ByteBufCodecs.holderSet(registryKey))
                    .map(net.neoforged.neoforge.registries.holdersets.AndHolderSet::new,
                         net.neoforged.neoforge.registries.holdersets.CompositeHolderSet::getComponents);
        }
    };

    /** HolderSetType for {@link net.neoforged.neoforge.registries.holdersets.OrHolderSet}. */
    public static final HolderSetType OR_HOLDER_SET = new HolderSetType() {
        @Override public <T> com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, com.mojang.serialization.Codec<Holder<T>> holderCodec, boolean forceList) {
            return net.minecraft.resources.HolderSetCodec.create(registryKey, holderCodec, forceList)
                    .listOf()
                    .xmap(net.neoforged.neoforge.registries.holdersets.OrHolderSet::new,
                           net.neoforged.neoforge.registries.holdersets.CompositeHolderSet::homogenize)
                    .fieldOf("values");
        }
        @Override public <T> net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeStreamCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey) {
            return net.minecraft.network.codec.ByteBufCodecs.<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.core.HolderSet<T>>list()
                    .apply(net.minecraft.network.codec.ByteBufCodecs.holderSet(registryKey))
                    .map(net.neoforged.neoforge.registries.holdersets.OrHolderSet::new,
                         net.neoforged.neoforge.registries.holdersets.CompositeHolderSet::getComponents);
        }
    };

    /** HolderSetType for {@link net.neoforged.neoforge.registries.holdersets.NotHolderSet}. */
    public static final HolderSetType NOT_HOLDER_SET = new HolderSetType() {
        @Override public <T> com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, com.mojang.serialization.Codec<Holder<T>> holderCodec, boolean forceList) {
            return com.mojang.serialization.codecs.RecordCodecBuilder.<net.neoforged.neoforge.registries.holdersets.NotHolderSet<T>>mapCodec(
                    builder -> builder
                            .group(
                                    net.minecraft.resources.RegistryOps.retrieveRegistryLookup(registryKey)
                                            .forGetter(net.neoforged.neoforge.registries.holdersets.NotHolderSet::registryLookup),
                                    net.minecraft.resources.HolderSetCodec.create(registryKey, holderCodec, forceList)
                                            .fieldOf("value")
                                            .forGetter(net.neoforged.neoforge.registries.holdersets.NotHolderSet::value))
                            .apply(builder, net.neoforged.neoforge.registries.holdersets.NotHolderSet::new));
        }
        @Override public <T> net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ? extends net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T>>
        makeStreamCodec(ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey) {
            return new net.minecraft.network.codec.StreamCodec<>() {
                private final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.core.HolderSet<T>> holderSetCodec =
                        net.minecraft.network.codec.ByteBufCodecs.holderSet(registryKey);
                @Override
                public net.neoforged.neoforge.registries.holdersets.NotHolderSet<T> decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
                    return new net.neoforged.neoforge.registries.holdersets.NotHolderSet<>(buf.registryAccess().lookupOrThrow(registryKey), holderSetCodec.decode(buf));
                }
                @Override
                public void encode(net.minecraft.network.RegistryFriendlyByteBuf buf, net.neoforged.neoforge.registries.holdersets.ICustomHolderSet<T> set) {
                    holderSetCodec.encode(buf, ((net.neoforged.neoforge.registries.holdersets.NotHolderSet<T>) set).value());
                }
            };
        }
    };

    // ── Internal helpers ─────────────────────────────────────────────────

    /**
     * Creates a lazy {@link Holder} that resolves from a Forge {@link RegistryObject}.
     * At class-init time the RegistryObject may not yet be populated, so we defer
     * resolution until first access via {@link Holder#value()}.
     */
    @SuppressWarnings("unchecked")
    private static <T> Holder<T> lazyHolder(RegistryObject<T> registryObject) {
        return new Holder<T>() {
            private volatile Holder<T> delegate;

            private Holder<T> resolve() {
                if (delegate == null) {
                    synchronized (this) {
                        if (delegate == null) {
                            delegate = registryObject.getHolder()
                                    .orElseGet(() -> Holder.direct(registryObject.get()));
                        }
                    }
                }
                return delegate;
            }

            @Override public T value() { return resolve().value(); }
            @Override public boolean isBound() { return resolve().isBound(); }
            @Override public boolean is(ResourceLocation loc) { return resolve().is(loc); }
            @Override public boolean is(ResourceKey<T> key) { return resolve().is(key); }
            @Override public boolean is(Predicate<ResourceKey<T>> pred) { return resolve().is(pred); }
            @Override public boolean is(TagKey<T> tag) { return resolve().is(tag); }
            @Override public boolean is(Holder<T> other) { return resolve().is(other); }
            @Override public Stream<TagKey<T>> tags() { return resolve().tags(); }
            @Override public Either<ResourceKey<T>, T> unwrap() { return resolve().unwrap(); }
            @Override public Optional<ResourceKey<T>> unwrapKey() { return resolve().unwrapKey(); }
            @Override public Kind kind() { return resolve().kind(); }
            @Override public boolean canSerializeIn(HolderOwner<T> owner) { return resolve().canSerializeIn(owner); }
        };
    }
}
