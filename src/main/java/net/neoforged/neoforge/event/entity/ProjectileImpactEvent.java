package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ProjectileImpactEvent extends EntityEvent implements ICancellableEvent {
	private final net.minecraftforge.event.entity.ProjectileImpactEvent delegate;
	private final HitResult ray;
	private final Projectile projectile;

	public ProjectileImpactEvent() {
		super();
		this.delegate = null;
		this.ray = null;
		this.projectile = null;
	}

	public ProjectileImpactEvent(Projectile projectile, HitResult ray) {
		super(projectile);
		this.delegate = null;
		this.ray = ray;
		this.projectile = projectile;
	}

	public ProjectileImpactEvent(net.minecraftforge.event.entity.ProjectileImpactEvent delegate) {
		super(delegate);
		this.delegate = delegate;
		this.ray = delegate.getRayTraceResult();
		this.projectile = delegate.getProjectile();
	}

	public HitResult getRayTraceResult() {
		return ray;
	}

	public Projectile getProjectile() {
		return projectile;
	}

	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		if (delegate != null) {
			delegate.setImpactResult(canceled ? net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE : net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult.DEFAULT);
		}
	}
}
