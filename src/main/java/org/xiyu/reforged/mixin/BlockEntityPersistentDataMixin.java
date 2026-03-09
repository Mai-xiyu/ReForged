package org.xiyu.reforged.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityPersistentDataMixin {

    @Unique
    private CompoundTag reforged$customPersistentData;

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void reforged$loadPersistentData(CompoundTag tag, HolderLookup.Provider lookupProvider, CallbackInfo ci) {
        if (tag.contains("NeoForgeData", Tag.TAG_COMPOUND)) {
            this.reforged$customPersistentData = tag.getCompound("NeoForgeData").copy();
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void reforged$savePersistentData(CompoundTag tag, HolderLookup.Provider lookupProvider, CallbackInfo ci) {
        if (this.reforged$customPersistentData != null) {
            tag.put("NeoForgeData", this.reforged$customPersistentData.copy());
        }
    }

    public CompoundTag getPersistentData() {
        if (this.reforged$customPersistentData == null) {
            this.reforged$customPersistentData = new CompoundTag();
        }
        return this.reforged$customPersistentData;
    }
}