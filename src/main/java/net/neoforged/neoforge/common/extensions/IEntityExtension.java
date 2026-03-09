package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.attachment.AttachmentInternals;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraftforge.common.extensions.IForgeEntity;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.Collection;

/**
 * Stub extension interface for Entity.
 */
public interface IEntityExtension {

    default Entity self() {
        return (Entity) this;
    }

    @Nullable
    default Collection<ItemEntity> captureDrops() {
        return self().captureDrops();
    }

    default Collection<ItemEntity> captureDrops(@Nullable Collection<ItemEntity> drops) {
        return self().captureDrops(drops);
    }

    default CompoundTag getPersistentData() {
        return self().getPersistentData();
    }

    default boolean shouldRiderSit() {
        return true;
    }

    @Nullable
    default ItemStack getPickedResult(HitResult target) {
        return self().getPickResult();
    }

    default boolean canRiderInteract() {
        return false;
    }

    default boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
        return ((IForgeEntity) self()).canBeRiddenUnderFluidType(type, rider);
    }

    boolean canTrample(BlockState state, BlockPos pos, float fallDistance);

    default MobCategory getClassification(boolean forSpawnCount) {
        return self().getType().getCategory();
    }

    default boolean isAddedToLevel() {
        return ((IForgeEntity) self()).isAddedToWorld();
    }

    default void onAddedToLevel() {
        ((IForgeEntity) self()).onAddedToWorld();
    }

    default void onRemovedFromLevel() {
        ((IForgeEntity) self()).onRemovedFromWorld();
    }

    default void revive() {
        ((IForgeEntity) self()).revive();
    }

    default boolean isMultipartEntity() {
        return ((IForgeEntity) self()).isMultipartEntity();
    }

    @Nullable
    default PartEntity<?>[] getParts() {
        return null;
    }

    default double getFluidTypeHeight(FluidType type) {
        return ((IForgeEntity) self()).getFluidTypeHeight(type);
    }

    @Nullable
    default FluidType getMaxHeightFluidType() {
        net.minecraftforge.fluids.FluidType fluidType = ((IForgeEntity) self()).getMaxHeightFluidType();
        return fluidType instanceof FluidType neoFluid ? neoFluid : null;
    }

    default boolean isInFluidType(FluidState state) {
        return ((IForgeEntity) self()).isInFluidType(state);
    }

    default boolean isInFluidType(FluidType type) {
        return ((IForgeEntity) self()).isInFluidType(type);
    }

    default boolean isInFluidType(BiPredicate<FluidType, Double> predicate) {
        return isInFluidType(predicate, false);
    }

    default boolean isInFluidType(BiPredicate<FluidType, Double> predicate, boolean forAllTypes) {
        return ((IForgeEntity) self()).isInFluidType((forgeType, height) -> forgeType instanceof FluidType neoType && predicate.test(neoType, height), forAllTypes);
    }

    default boolean isInFluidType() {
        return ((IForgeEntity) self()).isInFluidType();
    }

    @Nullable
    default FluidType getEyeInFluidType() {
        net.minecraftforge.fluids.FluidType fluidType = ((IForgeEntity) self()).getEyeInFluidType();
        return fluidType instanceof FluidType neoFluid ? neoFluid : null;
    }

    default boolean isEyeInFluidType(FluidType type) {
        return type == this.getEyeInFluidType();
    }

    default boolean canStartSwimming() {
        FluidType eyeFluid = this.getEyeInFluidType();
        return eyeFluid != null && !eyeFluid.isAir() && this.canSwimInFluidType(eyeFluid);
    }

    default double getFluidMotionScale(FluidType type) {
        return ((IForgeEntity) self()).getFluidMotionScale(type);
    }

    default boolean isPushedByFluid(FluidType type) {
        return ((IForgeEntity) self()).isPushedByFluid(type);
    }

    default boolean canSwimInFluidType(FluidType type) {
        return ((IForgeEntity) self()).canSwimInFluidType(type);
    }

    default boolean canFluidExtinguish(FluidType type) {
        return ((IForgeEntity) self()).canFluidExtinguish(type);
    }

    default float getFluidFallDistanceModifier(FluidType type) {
        return ((IForgeEntity) self()).getFluidFallDistanceModifier(type);
    }

    default boolean canHydrateInFluidType(FluidType type) {
        return ((IForgeEntity) self()).canHydrateInFluidType(type);
    }

    @Nullable
    default SoundEvent getSoundFromFluidType(FluidType type, SoundAction action) {
        return ((IForgeEntity) self()).getSoundFromFluidType(type, net.minecraftforge.common.SoundAction.get(action.name()));
    }

    default boolean hasCustomOutlineRendering(Player player) {
        return ((IForgeEntity) self()).hasCustomOutlineRendering(player);
    }

    default void sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
        // Complex spawn payload bridging is wired later with payload codec support.
    }

    default boolean canUpdate() {
        return self().canUpdate();
    }

    default void canUpdate(boolean value) {
        self().canUpdate(value);
    }

    default CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag ret = new CompoundTag();
        String id = self().getEncodeId();
        if (id != null) {
            ret.putString("id", id);
        }
        return self().saveWithoutId(ret);
    }

    default void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt) {
        self().load(nbt);
    }

    default void copyAttachmentsFrom(Entity other, boolean isDeath) {
        AttachmentInternals.copyEntityAttachments(other, self(), isDeath);
    }
}
