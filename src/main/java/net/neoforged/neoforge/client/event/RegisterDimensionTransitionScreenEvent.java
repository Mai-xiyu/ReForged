package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.DimensionTransitionScreenManager.ReceivingLevelScreenFactory;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to register dimension transition screens.
 */
public class RegisterDimensionTransitionScreenEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalDimensionEffects;
    private final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> toEffects;
    private final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> fromEffects;

    public RegisterDimensionTransitionScreenEvent(
            Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalDimensionEffects,
            Map<ResourceKey<Level>, ReceivingLevelScreenFactory> toEffects,
            Map<ResourceKey<Level>, ReceivingLevelScreenFactory> fromEffects) {
        this.conditionalDimensionEffects = conditionalDimensionEffects;
        this.toEffects = toEffects;
        this.fromEffects = fromEffects;
    }

    public boolean registerIncomingEffect(ResourceKey<Level> dimension, ReceivingLevelScreenFactory screen) {
        return this.toEffects.putIfAbsent(dimension, screen) == null;
    }

    public boolean registerOutgoingEffect(ResourceKey<Level> dimension, ReceivingLevelScreenFactory screen) {
        return this.fromEffects.putIfAbsent(dimension, screen) == null;
    }

    public boolean registerConditionalEffect(ResourceKey<Level> toDimension, ResourceKey<Level> fromDimension, ReceivingLevelScreenFactory screen) {
        return this.conditionalDimensionEffects.putIfAbsent(Pair.of(toDimension, fromDimension), screen) == null;
    }
}
