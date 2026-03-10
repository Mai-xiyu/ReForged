package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Stub: Fired to determine the projectile a living entity will use.
 */
public class LivingGetProjectileEvent extends LivingEvent {
    private final ItemStack projectileWeaponItemStack;
    private ItemStack projectileItemStack;

    public LivingGetProjectileEvent(LivingEntity entity, ItemStack projectileWeaponItemStack, ItemStack projectileItemStack) {
        super(entity);
        this.projectileWeaponItemStack = projectileWeaponItemStack;
        this.projectileItemStack = projectileItemStack;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public LivingGetProjectileEvent(net.minecraftforge.event.entity.living.LivingGetProjectileEvent delegate) {
        this(delegate.getEntity(), delegate.getProjectileWeaponItemStack(), delegate.getProjectileItemStack());
    }

    public ItemStack getProjectileWeaponItemStack() { return projectileWeaponItemStack; }
    public ItemStack getProjectileItemStack() { return projectileItemStack; }
    public void setProjectileItemStack(ItemStack projectile) { this.projectileItemStack = projectile; }
}
