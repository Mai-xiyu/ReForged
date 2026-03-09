package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;

public class ItemExpireEvent extends ItemEvent {
    private final net.minecraftforge.event.entity.item.ItemExpireEvent forgeDelegate;
    private int extraLife = 0;

    public ItemExpireEvent(ItemEntity entityItem) {
        super(entityItem);
        this.forgeDelegate = null;
    }

    public ItemExpireEvent(ItemEntity entityItem, int extraLife) {
        super(entityItem);
        this.forgeDelegate = null;
        this.extraLife = extraLife;
    }

    public ItemExpireEvent(net.minecraftforge.event.entity.item.ItemExpireEvent forge) {
        super(forge.getEntity());
        this.forgeDelegate = forge;
        this.extraLife = forge.getExtraLife();
    }

    public int getExtraLife() {
        return extraLife;
    }

    public void setExtraLife(int extraLife) {
        this.extraLife = extraLife;
        if (forgeDelegate != null) {
            forgeDelegate.setExtraLife(extraLife);
            forgeDelegate.setCanceled(true);
        }
    }

    public void addExtraLife(int extraLife) {
        this.extraLife += extraLife;
        if (forgeDelegate != null) {
            forgeDelegate.setExtraLife(this.extraLife);
            forgeDelegate.setCanceled(true);
        }
    }
}
