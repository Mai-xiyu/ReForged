package net.neoforged.neoforge.event.entity.living;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDamageEvent}.
 * NeoForge has Pre/Post subclasses; Forge just has the base event.
 */
public abstract class LivingDamageEvent extends LivingEvent {
    private final net.minecraftforge.event.entity.living.LivingDamageEvent delegate;

    public LivingDamageEvent() {
        super();
        this.delegate = null;
    }

    public LivingDamageEvent(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    protected net.minecraftforge.event.entity.living.LivingDamageEvent delegate() {
        return delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public void setAmount(float amount) { delegate.setAmount(amount); }
    public DamageSource getSource() { return delegate.getSource(); }

    public static class Pre extends LivingDamageEvent {
        private final DamageContainer container;

        public Pre(LivingEntity entity, DamageContainer container) {
            super();
            this.container = container;
        }

        public Pre(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
            super(delegate);
            this.container = new DamageContainer(delegate.getSource(), delegate.getAmount());
        }

        public DamageContainer getContainer() { return container; }
        public float getNewDamage() { return container.getNewDamage(); }
        public float getOriginalDamage() { return container.getOriginalDamage(); }
        public void setNewDamage(float newDamage) {
            container.setNewDamage(newDamage);
            if (delegate() != null) {
				setAmount(newDamage);
			}
        }
    }

    public static class Post extends LivingDamageEvent {
        private final float originalDamage;
        private final DamageSource source;
        private final float newDamage;
        private final float blockedDamage;
        private final float shieldDamage;
        private final int postAttackInvulnerabilityTicks;
        private final EnumMap<DamageContainer.Reduction, Float> reductions;

        public Post(LivingEntity entity, DamageContainer container) {
            super();
            this.originalDamage = container.getOriginalDamage();
            this.source = container.getSource();
            this.newDamage = container.getNewDamage();
            this.blockedDamage = container.getBlockedDamage();
            this.shieldDamage = container.getShieldDamage();
            this.postAttackInvulnerabilityTicks = container.getPostAttackInvulnerabilityTicks();
            this.reductions = new EnumMap<>(DamageContainer.Reduction.class);
			Arrays.stream(DamageContainer.Reduction.values()).forEach(type -> this.reductions.put(type, container.getReduction(type)));
        }

        public Post(net.minecraftforge.event.entity.living.LivingDamageEvent delegate) {
            super(delegate);
            DamageContainer container = new DamageContainer(delegate.getSource(), delegate.getAmount());
            container.setNewDamage(delegate.getAmount());
            this.originalDamage = container.getOriginalDamage();
            this.source = container.getSource();
            this.newDamage = container.getNewDamage();
            this.blockedDamage = container.getBlockedDamage();
            this.shieldDamage = container.getShieldDamage();
            this.postAttackInvulnerabilityTicks = container.getPostAttackInvulnerabilityTicks();
            this.reductions = new EnumMap<>(DamageContainer.Reduction.class);
			Arrays.stream(DamageContainer.Reduction.values()).forEach(type -> this.reductions.put(type, container.getReduction(type)));
        }

        public float getOriginalDamage() { return originalDamage; }
        public DamageSource getSource() { return source; }
        public float getNewDamage() { return newDamage; }
        public float getBlockedDamage() { return blockedDamage; }
        public float getShieldDamage() { return shieldDamage; }
        public int getPostAttackInvulnerabilityTicks() { return postAttackInvulnerabilityTicks; }
        public float getReduction(DamageContainer.Reduction reduction) { return reductions.getOrDefault(reduction, 0f); }
    }
}
