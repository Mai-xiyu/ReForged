package net.neoforged.neoforge.event.entity.living;

import java.util.Collection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.ICancellableEvent;

public class LivingDropsEvent extends LivingEvent implements ICancellableEvent {
	private final DamageSource source;
	private final Collection<ItemEntity> drops;
	private final boolean recentlyHit;

	public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, boolean recentlyHit) {
		super(entity);
		this.source = source;
		this.drops = drops;
		this.recentlyHit = recentlyHit;
	}

	/** Forge wrapper constructor for automatic event bridging */
	public LivingDropsEvent(net.minecraftforge.event.entity.living.LivingDropsEvent delegate) {
		this((LivingEntity) delegate.getEntity(), delegate.getSource(), delegate.getDrops(), delegate.isRecentlyHit());
	}

	public DamageSource getSource() {
		return source;
	}

	public Collection<ItemEntity> getDrops() {
		return drops;
	}

	public boolean isRecentlyHit() {
		return recentlyHit;
	}
}
