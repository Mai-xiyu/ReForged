package net.neoforged.neoforge.client.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A dynamic fluid container model, capable of re-texturing itself at runtime to match the contained fluid.
 * <p>
 * Composed of a base layer, a fluid layer (applied with a mask) and a cover layer (optionally applied with a mask).
 * The entire model may optionally be flipped if the fluid is gaseous, and the fluid layer may glow if light-emitting.
 * <p>
 * Fluid tinting requires registering a separate {@link ItemColor}. An implementation is provided in {@link Colors}.
 *
 * @see Colors
 */
public class DynamicFluidContainerModel implements IUnbakedGeometry<DynamicFluidContainerModel> {
    // Depth offsets to prevent Z-fighting
    private static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());
    private static final Transformation COVER_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.004f), new Quaternionf());

    private final Fluid fluid;
    private final boolean flipGas;
    private final boolean coverIsMask;
    private final boolean applyFluidLuminosity;

    private DynamicFluidContainerModel(Fluid fluid, boolean flipGas, boolean coverIsMask, boolean applyFluidLuminosity) {
        this.fluid = fluid;
        this.flipGas = flipGas;
        this.coverIsMask = coverIsMask;
        this.applyFluidLuminosity = applyFluidLuminosity;
    }

    public static RenderTypeGroup getLayerRenderTypes(boolean unlit) {
        return new RenderTypeGroup(RenderType.translucent(), unlit ? NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get() : NeoForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
    }

    /**
     * Returns a new model representing the given fluid, but with the same
     * other properties (flipGas, coverIsMask, applyFluidLuminosity).
     */
    public DynamicFluidContainerModel withFluid(Fluid newFluid) {
        return new DynamicFluidContainerModel(newFluid, flipGas, coverIsMask, applyFluidLuminosity);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState, ItemOverrides overrides) {
        // Simplified bake: gather sprites and build a basic composite model
        Material particleLocation = context.hasMaterial("particle") ? context.getMaterial("particle") : null;
        Material baseLocation = context.hasMaterial("base") ? context.getMaterial("base") : null;

        TextureAtlasSprite baseSprite = baseLocation != null ? spriteGetter.apply(baseLocation) : null;
        TextureAtlasSprite particleSprite = particleLocation != null ? spriteGetter.apply(particleLocation) : null;
        if (particleSprite == null) particleSprite = baseSprite;

        // If the fluid is lighter than air, rotate 180deg to turn it upside down
        if (flipGas && fluid != Fluids.EMPTY && fluid.getFluidType().isLighterThanAir()) {
            modelState = new SimpleModelState(
                    modelState.getRotation().compose(
                            new Transformation(null, new Quaternionf(0, 0, 1, 0), null, null)));
        }

        var modelBuilder = CompositeModel.Baked.builder(context, particleSprite,
                new ContainedFluidOverrideHandler(overrides, baker, context, this), context.getTransforms());

        var normalRenderTypes = getLayerRenderTypes(false);

        if (baseLocation != null && baseSprite != null) {
            var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, baseSprite, null);
            var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> baseSprite, modelState);
            modelBuilder.addQuads(normalRenderTypes, quads);
        }

        return modelBuilder.build();
    }

    public static final class Loader implements IGeometryLoader<DynamicFluidContainerModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public DynamicFluidContainerModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            if (!jsonObject.has("fluid"))
                throw new RuntimeException("Bucket model requires 'fluid' value.");

            ResourceLocation fluidName = ResourceLocation.parse(jsonObject.get("fluid").getAsString());

            Fluid fluid = BuiltInRegistries.FLUID.get(fluidName);

            boolean flip = GsonHelper.getAsBoolean(jsonObject, "flip_gas", false);
            boolean coverIsMask = GsonHelper.getAsBoolean(jsonObject, "cover_is_mask", true);
            boolean applyFluidLuminosity = GsonHelper.getAsBoolean(jsonObject, "apply_fluid_luminosity", true);

            return new DynamicFluidContainerModel(fluid, flip, coverIsMask, applyFluidLuminosity);
        }
    }

    private static final class ContainedFluidOverrideHandler extends ItemOverrides {
        private final Map<String, BakedModel> cache = Maps.newHashMap();
        private final ItemOverrides nested;
        private final ModelBaker baker;
        private final IGeometryBakingContext owner;
        private final DynamicFluidContainerModel parent;

        private ContainedFluidOverrideHandler(ItemOverrides nested, ModelBaker baker, IGeometryBakingContext owner, DynamicFluidContainerModel parent) {
            this.nested = nested;
            this.baker = baker;
            this.owner = owner;
            this.parent = parent;
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel overridden = nested.resolve(originalModel, stack, level, entity, seed);
            if (overridden != originalModel) return overridden;
            // Try to resolve contained fluid via Forge's FluidUtil
            try {
                return net.minecraftforge.fluids.FluidUtil.getFluidContained(stack)
                        .map(forgeStack -> {
                            Fluid fl = forgeStack.getFluid();
                            String name = BuiltInRegistries.FLUID.getKey(fl).toString();
                            if (!cache.containsKey(name)) {
                                DynamicFluidContainerModel unbaked = parent.withFluid(fl);
                                BakedModel bakedModel = unbaked.bake(owner, baker, Material::sprite, BlockModelRotation.X0_Y0, this);
                                cache.put(name, bakedModel);
                                return bakedModel;
                            }
                            return cache.get(name);
                        })
                        .orElse(originalModel);
            } catch (Exception e) {
                return originalModel;
            }
        }
    }

    /**
     * {@link ItemColor} implementation for fluid container models.
     * Tint index 1 is used for the fluid layer.
     */
    public static class Colors implements ItemColor {
        @Override
        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 1) return 0xFFFFFFFF;
            try {
                return net.minecraftforge.fluids.FluidUtil.getFluidContained(stack)
                        .map(forgeStack -> IClientFluidTypeExtensions.of(forgeStack.getFluid()).getTintColor())
                        .orElse(0xFFFFFFFF);
            } catch (Exception e) {
                return 0xFFFFFFFF;
            }
        }
    }
}
