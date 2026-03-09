package net.neoforged.neoforge.event.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class EntityTeleportEvent extends EntityEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.EntityTeleportEvent delegate;

    public EntityTeleportEvent() {
        super();
        this.delegate = null;
    }

    protected EntityTeleportEvent(net.minecraftforge.event.entity.EntityTeleportEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    protected net.minecraftforge.event.entity.EntityTeleportEvent delegate() {
        return delegate;
    }

    public double getTargetX() { return delegate.getTargetX(); }
    public void setTargetX(double targetX) { delegate.setTargetX(targetX); }
    public double getTargetY() { return delegate.getTargetY(); }
    public void setTargetY(double targetY) { delegate.setTargetY(targetY); }
    public double getTargetZ() { return delegate.getTargetZ(); }
    public void setTargetZ(double targetZ) { delegate.setTargetZ(targetZ); }
    public Vec3 getTarget() { return delegate.getTarget(); }
    public double getPrevX() { return delegate.getPrevX(); }
    public double getPrevY() { return delegate.getPrevY(); }
    public double getPrevZ() { return delegate.getPrevZ(); }
    public Vec3 getPrev() { return delegate.getPrev(); }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }

    @Cancelable
    public static class TeleportCommand extends EntityTeleportEvent {
        public TeleportCommand(net.minecraftforge.event.entity.EntityTeleportEvent.TeleportCommand delegate) {
            super(delegate);
        }
    }

    @Cancelable
    public static class SpreadPlayersCommand extends EntityTeleportEvent {
        public SpreadPlayersCommand(net.minecraftforge.event.entity.EntityTeleportEvent.SpreadPlayersCommand delegate) {
            super(delegate);
        }
    }

    @Cancelable
    public static class EnderEntity extends EntityTeleportEvent {
        private final net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity delegate;

        public EnderEntity(net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        public LivingEntity getEntityLiving() {
            return delegate.getEntityLiving();
        }
    }

    @Cancelable
    public static class EnderPearl extends EntityTeleportEvent {
        private final net.minecraftforge.event.entity.EntityTeleportEvent.EnderPearl delegate;

        public EnderPearl(net.minecraftforge.event.entity.EntityTeleportEvent.EnderPearl delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        public ThrownEnderpearl getPearlEntity() { return delegate.getPearlEntity(); }
        public ServerPlayer getPlayer() { return delegate.getPlayer(); }
        @Nullable public HitResult getHitResult() { return delegate.getHitResult(); }
        public float getAttackDamage() { return delegate.getAttackDamage(); }
        public void setAttackDamage(float attackDamage) { delegate.setAttackDamage(attackDamage); }
    }

    @Cancelable
    public static class ChorusFruit extends EntityTeleportEvent {
        private final net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit delegate;

        public ChorusFruit(net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        public LivingEntity getEntityLiving() {
            return delegate.getEntityLiving();
        }
    }
}
