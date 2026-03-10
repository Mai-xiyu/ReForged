package net.neoforged.neoforge.common.damagesource;

import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Container for tracking damage values through the damage pipeline.
 * Stores the original damage, current damage, blocked amounts, reductions, and modifiers.
 */
public class DamageContainer {
	public enum Reduction {
		INVULNERABILITY,
		ARMOR,
		ENCHANTMENTS,
		MOB_EFFECTS,
		ABSORPTION,
		INNATE_RESISTANCE
	}

	private final DamageSource source;
	private final float originalDamage;
	private float newDamage;
	private float blockedDamage;
	private float shieldDamage;
	private int postAttackInvulnerabilityTicks = 20;
	private final EnumMap<Reduction, Float> reductions = new EnumMap<>(Reduction.class);
	private final EnumMap<Reduction, List<IReductionFunction>> reductionFunctions = new EnumMap<>(Reduction.class);

	public DamageContainer() {
		this.source = null;
		this.originalDamage = 0f;
		this.newDamage = 0f;
	}

	public DamageContainer(DamageSource source, float originalDamage) {
		this.source = source;
		this.originalDamage = originalDamage;
		this.newDamage = originalDamage;
	}

	public DamageSource getSource() {
		return source;
	}

	public float getOriginalDamage() {
		return originalDamage;
	}

	public float getNewDamage() {
		return newDamage;
	}

	public void setNewDamage(float newDamage) {
		this.newDamage = newDamage;
	}

	public float getBlockedDamage() {
		return blockedDamage;
	}

	public void setBlockedDamage(float blockedDamage) {
		this.blockedDamage = blockedDamage;
	}

	public float getShieldDamage() {
		return shieldDamage;
	}

	public void setShieldDamage(float shieldDamage) {
		this.shieldDamage = shieldDamage;
	}

	public int getPostAttackInvulnerabilityTicks() {
		return postAttackInvulnerabilityTicks;
	}

	public void setPostAttackInvulnerabilityTicks(int ticks) {
		this.postAttackInvulnerabilityTicks = ticks;
	}

	public float getReduction(Reduction reduction) {
		return reductions.getOrDefault(reduction, 0f);
	}

	public void setReduction(Reduction reduction, float amount) {
		reductions.put(reduction, amount);
	}

	/**
	 * Adds a modifier function for a specific reduction type.
	 * The modifier will be applied before the reduction value is used.
	 */
	public void addReductionModifier(Reduction reduction, IReductionFunction function) {
		reductionFunctions.computeIfAbsent(reduction, k -> new ArrayList<>()).add(function);
	}

	/**
	 * Applies all registered reduction modifiers and returns the final reduction value.
	 */
	public float getModifiedReduction(Reduction reduction) {
		float value = getReduction(reduction);
		List<IReductionFunction> functions = reductionFunctions.get(reduction);
		if (functions != null) {
			for (IReductionFunction fn : functions) {
				value = fn.modify(this, value);
			}
		}
		return value;
	}
}
