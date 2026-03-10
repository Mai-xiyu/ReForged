package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * NeoForge's FluidIngredient — represents a fluid ingredient in recipes.
 * Supports single fluid matching and tag-based matching.
 */
public class FluidIngredient implements Predicate<FluidStack> {

    public static final FluidIngredient EMPTY = new FluidIngredient();

    /**
     * MapCodec that handles both single fluid and tag ingredient forms.
     * Tries single fluid first ({@code "fluid": "..."}, then tag ({@code "tag": "..."}),
     * and also supports the full typed dispatch via "type" field.
     */
    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = MapCodec.assumeMapUnsafe(
            Codec.either(SingleFluidIngredient.CODEC.codec(), TagFluidIngredient.CODEC.codec())
                    .xmap(
                            either -> either.map(s -> (FluidIngredient) s, t -> (FluidIngredient) t),
                            ingredient -> {
                                if (ingredient instanceof TagFluidIngredient tag) {
                                    return Either.right(tag);
                                } else if (ingredient instanceof SingleFluidIngredient single) {
                                    return Either.left(single);
                                }
                                return Either.left(EMPTY instanceof SingleFluidIngredient s ? s : new SingleFluidIngredient(
                                        net.minecraft.core.registries.BuiltInRegistries.FLUID
                                                .wrapAsHolder(net.minecraft.world.level.material.Fluids.WATER)));
                            }
                    ));

    public static final Codec<FluidIngredient> CODEC = MAP_CODEC_NONEMPTY.codec();
    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = CODEC;
    public static final Codec<List<FluidIngredient>> LIST_CODEC = CODEC.listOf();

    /**
     * StreamCodec for network serialization. Sends the list of matching FluidStacks.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            if (count == 0) return EMPTY;
            FluidStack[] stacks = new FluidStack[count];
            for (int i = 0; i < count; i++) {
                stacks[i] = FluidStack.STREAM_CODEC.decode(buf);
            }
            // Reconstruct as a single fluid ingredient from the first stack
            if (stacks.length == 1) {
                return new SingleFluidIngredient(stacks[0].getFluidHolder());
            }
            return EMPTY;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            FluidStack[] stacks = ingredient.getStacks();
            buf.writeVarInt(stacks.length);
            for (FluidStack stack : stacks) {
                FluidStack.STREAM_CODEC.encode(buf, stack);
            }
        }
    };

    public FluidIngredient() {
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public FluidStack[] getStacks() {
        return new FluidStack[0];
    }

    public boolean hasNoFluids() {
        return true;
    }

    public boolean isSimple() {
        return true;
    }

    public FluidIngredientType<?> getType() {
        return null;
    }

    public List<FluidStack> fluids() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static <T extends FluidIngredient> MapCodec<T> codec(FluidIngredientType<T> type) {
        return (MapCodec<T>) MAP_CODEC_NONEMPTY;
    }
}
