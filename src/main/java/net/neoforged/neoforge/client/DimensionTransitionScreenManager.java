package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-compatible dimension transition screen registry.
 */
public class DimensionTransitionScreenManager {
    private static final Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> CONDITIONAL_EFFECTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> TO_EFFECTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> FROM_EFFECTS = new HashMap<>();

    private DimensionTransitionScreenManager() {}

    public static ReceivingLevelScreenFactory getScreenFromLevel(@Nullable Level target, @Nullable Level source) {
        if (source == null) {
            return getScreen(null, null);
        } else if (target == null) {
            return getScreen(null, source.dimension());
        }
        return getScreen(target.dimension(), source.dimension());
    }

    public static ReceivingLevelScreenFactory getScreen(@Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        ReceivingLevelScreenFactory conditional = CONDITIONAL_EFFECTS.get(Pair.of(toDimension, fromDimension));
        if (conditional != null) {
            return conditional;
        }
        ReceivingLevelScreenFactory to = TO_EFFECTS.get(toDimension);
        if (to != null) {
            return to;
        }
        ReceivingLevelScreenFactory from = FROM_EFFECTS.get(fromDimension);
        if (from != null) {
            return from;
        }
        return ReceivingLevelScreen::new;
    }

    public static Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalEffects() {
        return CONDITIONAL_EFFECTS;
    }

    public static Map<ResourceKey<Level>, ReceivingLevelScreenFactory> incomingEffects() {
        return TO_EFFECTS;
    }

    public static Map<ResourceKey<Level>, ReceivingLevelScreenFactory> outgoingEffects() {
        return FROM_EFFECTS;
    }

    public interface ReceivingLevelScreenFactory {
        ReceivingLevelScreen create(BooleanSupplier supplier, ReceivingLevelScreen.Reason reason);
    }
}
