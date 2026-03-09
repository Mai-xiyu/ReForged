package net.neoforged.neoforge.event.entity.living;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Stub: Fired when a living entity shields a blow.
 */
@Cancelable
public class LivingShieldBlockEvent extends LivingEvent implements ICancellableEvent {
    private final DamageContainer container;
    private final DamageSource damageSource;
    private final float originalBlockedDamage;
    private float blockedDamage;
    private float shieldDamage;
    private final boolean originalBlocked;
    private boolean blocked;

    public LivingShieldBlockEvent(LivingEntity entity, DamageSource damageSource, float blockedDamage) {
        super(entity);
        this.container = null;
        this.damageSource = damageSource;
        this.originalBlockedDamage = blockedDamage;
        this.blockedDamage = blockedDamage;
        this.shieldDamage = blockedDamage;
        this.originalBlocked = blockedDamage > 0;
        this.blocked = this.originalBlocked;
    }

    public LivingShieldBlockEvent(LivingEntity blocker, DamageContainer container, boolean originalBlockedState) {
		super(blocker);
		this.container = container;
		this.damageSource = container.getSource();
		this.originalBlockedDamage = container.getNewDamage();
		this.blockedDamage = container.getNewDamage();
		this.shieldDamage = container.getNewDamage();
		this.originalBlocked = originalBlockedState;
		this.blocked = originalBlockedState;
	}

    public DamageContainer getDamageContainer() { return container; }
    public DamageSource getDamageSource() { return damageSource; }
    public float getOriginalBlockedDamage() { return originalBlockedDamage; }
    public float getBlockedDamage() { return Math.min(blockedDamage, originalBlockedDamage); }
    public void setBlockedDamage(float blockedDamage) { this.blockedDamage = Mth.clamp(blockedDamage, 0, originalBlockedDamage); }
    public float shieldDamage() { return blocked ? Math.max(0f, shieldDamage) : 0f; }
    public void setShieldDamage(float shieldDamage) { this.shieldDamage = shieldDamage; }
    public boolean getOriginalBlock() { return originalBlocked; }
    public boolean getBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean shieldTakesDamage() { return shieldDamage() > 0f; }
    public void setShieldTakesDamage(boolean shieldTakesDamage) { if (!shieldTakesDamage) this.shieldDamage = 0f; }
}
