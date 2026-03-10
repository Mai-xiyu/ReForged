package net.neoforged.neoforge.event.entity.living;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.Nullable;

/**
 * Events related to mob effects (potions).
 */
public abstract class MobEffectEvent extends LivingEvent {
    @Nullable
    protected final MobEffectInstance effectInstance;

    protected MobEffectEvent(LivingEntity living, @Nullable MobEffectInstance effectInstance) {
        super(living);
        this.effectInstance = effectInstance;
    }

    @Nullable
    public MobEffectInstance getEffectInstance() { return effectInstance; }

    public static class Remove extends MobEffectEvent implements ICancellableEvent {
        private final Holder<MobEffect> effect;
        @Nullable private final EffectCure cure;

        public Remove(LivingEntity living, Holder<MobEffect> effect, @Nullable EffectCure cure) {
            super(living, living.getEffect(effect));
            this.effect = effect;
            this.cure = cure;
        }

        public Remove(LivingEntity living, MobEffectInstance effectInstance, @Nullable EffectCure cure) {
            super(living, effectInstance);
            this.effect = effectInstance.getEffect();
            this.cure = cure;
        }

        public Holder<MobEffect> getEffect() { return effect; }
        @Nullable public EffectCure getCure() { return cure; }
    }

    public static class Applicable extends MobEffectEvent {
        protected Result result = Result.DEFAULT;
        @Nullable private final Entity source;

        public Applicable(LivingEntity living, MobEffectInstance effectInstance, @Nullable Entity source) {
            super(living, effectInstance);
            this.source = source;
        }

        @Override
        public MobEffectInstance getEffectInstance() { return super.getEffectInstance(); }

        public void setApplicableResult(Result result) { this.result = result; }
        public Result getApplicableResult() { return result; }
        @Nullable public Entity getEffectSource() { return source; }

        @SuppressWarnings("deprecation")
        public boolean getApplicationResult() {
            if (result == Result.APPLY) return true;
            return result == Result.DEFAULT && getEntity().canBeAffected(getEffectInstance());
        }

        public enum Result { APPLY, DEFAULT, DO_NOT_APPLY }
    }

    public static class Added extends MobEffectEvent {
        @Nullable private final MobEffectInstance oldEffectInstance;
        @Nullable private final Entity source;

        public Added(LivingEntity living, @Nullable MobEffectInstance oldEffectInstance,
                MobEffectInstance newEffectInstance, @Nullable Entity source) {
            super(living, newEffectInstance);
            this.oldEffectInstance = oldEffectInstance;
            this.source = source;
        }

        @Override
        public MobEffectInstance getEffectInstance() { return super.getEffectInstance(); }
        @Nullable public MobEffectInstance getOldEffectInstance() { return oldEffectInstance; }
        @Nullable public Entity getEffectSource() { return source; }
    }

    public static class Expired extends MobEffectEvent implements ICancellableEvent {
        public Expired(LivingEntity living, MobEffectInstance effectInstance) {
            super(living, effectInstance);
        }
    }
}
