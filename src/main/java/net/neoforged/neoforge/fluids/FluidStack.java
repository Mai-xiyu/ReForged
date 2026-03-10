package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: NeoForge's FluidStack — analogous to ItemStack but for fluids.
 */
public final class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec().validate(holder -> {
        return holder.is(Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> {
            return "Fluid must not be minecraft:empty";
        }) : DataResult.success(holder);
    });

    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(FluidStack::getAmount)
    ).apply(inst, FluidStack::new));

    public static final Codec<FluidStack> OPTIONAL_CODEC = CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC =
            StreamCodec.of(FluidStack::encode, FluidStack::decode);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = OPTIONAL_STREAM_CODEC;

    private Fluid fluid;
    private int amount;
    private final PatchedDataComponentMap components;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount);
    }

    public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
        this.components.applyPatch(patch);
    }

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid.value(), amount, patch);
    }

    public static FluidStack copy(FluidStack stack) {
        if (stack.isEmpty()) return EMPTY;
        return new FluidStack(stack.fluid, stack.amount);
    }

    public FluidStack copy() {
        return copy(this);
    }

    public FluidStack copyWithAmount(int amount) {
        if (isEmpty()) return EMPTY;
        return new FluidStack(this.fluid, amount);
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    public Fluid getFluid() { return fluid; }

    public Holder<Fluid> getFluidHolder() {
        return BuiltInRegistries.FLUID.wrapAsHolder(fluid);
    }

    public int getAmount() { return amount; }

    public void setAmount(int amount) { this.amount = amount; }

    public void grow(int amount) { this.amount += amount; }

    public void shrink(int amount) { this.amount -= amount; }

    public boolean is(Fluid fluid) { return this.fluid == fluid; }

    public boolean is(Holder<Fluid> fluid) { return this.fluid == fluid.value(); }

    public boolean is(TagKey<Fluid> tag) {
        return getFluidHolder().is(tag);
    }

    public boolean is(Predicate<Holder<Fluid>> predicate) {
        return predicate.test(getFluidHolder());
    }

    public Component getHoverName() {
        return Component.translatable(getFluid().defaultFluidState().createLegacyBlock().getBlock().getDescriptionId());
    }

    public static boolean isSameFluid(FluidStack a, FluidStack b) {
        return a.fluid == b.fluid;
    }

    public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
        if (!isSameFluid(a, b)) return false;
        if (a.isEmpty() && b.isEmpty()) return true;
        return true; // simplified - no full component comparison
    }

    public boolean isFluidEqual(FluidStack other) {
        return isSameFluid(this, other);
    }

    @Nullable
    public <T> T get(DataComponentType<? extends T> type) {
        return components.get(type);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> type, @Nullable T value) {
        return components.set(type, value);
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> type) {
        return components.remove(type);
    }

    public DataComponentMap getComponents() {
        return components;
    }

    public DataComponentPatch getComponentsPatch() {
        return components.asPatch();
    }

    public void applyComponents(DataComponentPatch patch) {
        components.applyPatch(patch);
    }

    public void applyComponents(DataComponentMap map) {
        for (var typed : map) {
            applyTyped(typed);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void applyTyped(net.minecraft.core.component.TypedDataComponent<T> typed) {
        components.set(typed.type(), typed.value());
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_HOLDER_STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.FLUID);

    private static void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
        if (stack.isEmpty()) {
            buf.writeVarInt(0);
        } else {
            buf.writeVarInt(stack.getAmount());
            FLUID_HOLDER_STREAM_CODEC.encode(buf, stack.getFluidHolder());
            DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
        }
    }

    private static FluidStack decode(RegistryFriendlyByteBuf buf) {
        int amount = buf.readVarInt();
        if (amount <= 0) {
            return EMPTY;
        }
        Holder<Fluid> holder = FLUID_HOLDER_STREAM_CODEC.decode(buf);
        DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
        return new FluidStack(holder, amount, patch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidStack that)) return false;
        return amount == that.amount && fluid == that.fluid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, amount);
    }

    @Override
    public String toString() {
        return amount + " " + BuiltInRegistries.FLUID.getKey(fluid);
    }
}
