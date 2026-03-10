package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Fluid ingredient that matches all fluids within the given tag.
 */
public class TagFluidIngredient extends FluidIngredient {
    public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID)
            .xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");

    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(tag);
    }

    protected Stream<FluidStack> generateStacks() {
        return BuiltInRegistries.FLUID.getTag(tag)
                .stream()
                .flatMap(HolderSet::stream)
                .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return null; // No registry available in Forge shim
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof TagFluidIngredient other && other.tag.equals(this.tag);
    }

    public TagKey<Fluid> tag() {
        return tag;
    }
}
