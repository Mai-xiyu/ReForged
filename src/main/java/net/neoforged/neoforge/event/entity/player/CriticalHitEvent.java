package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Stub: Fired when a player attacks an entity with a critical hit.
 */
public class CriticalHitEvent extends PlayerEvent {
    private final Entity target;
    private final float vanillaDmgMultiplier;
    private final boolean isVanillaCritical;
    private float dmgMultiplier;
    private boolean isCriticalHit;

    public CriticalHitEvent(Player player, Entity target, float dmgMultiplier, boolean isCriticalHit) {
        super();
        this.target = target;
        this.dmgMultiplier = this.vanillaDmgMultiplier = dmgMultiplier;
        this.isCriticalHit = this.isVanillaCritical = isCriticalHit;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public CriticalHitEvent(net.minecraftforge.event.entity.player.CriticalHitEvent delegate) {
        this(delegate.getEntity(), delegate.getTarget(), delegate.getDamageModifier(), delegate.isVanillaCritical());
    }

    public Entity getTarget() { return target; }
    public float getDamageMultiplier() { return dmgMultiplier; }
    public void setDamageMultiplier(float dmgMultiplier) { this.dmgMultiplier = dmgMultiplier; }
    public boolean isCriticalHit() { return isCriticalHit; }
    public void setCriticalHit(boolean isCriticalHit) { this.isCriticalHit = isCriticalHit; }
    public float getVanillaMultiplier() { return vanillaDmgMultiplier; }
    public boolean isVanillaCritical() { return isVanillaCritical; }
}
