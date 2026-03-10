package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired to allow mods to register custom spawn placements.
 * Delegates to {@link RegisterSpawnPlacementsEvent} which accesses the underlying Forge system.
 */
public class SpawnPlacementRegisterEvent extends Event implements IModBusEvent {
    private final RegisterSpawnPlacementsEvent delegate = new RegisterSpawnPlacementsEvent();

    public <T extends Entity> void register(
            EntityType<T> entityType,
            SpawnPlacements.SpawnPredicate<T> predicate,
            Operation operation) {
        register(entityType, null, null, predicate, operation);
    }

    public <T extends Entity> void register(
            EntityType<T> entityType,
            @Nullable SpawnPlacementType placementType,
            @Nullable Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> predicate,
            Operation operation) {
        RegisterSpawnPlacementsEvent.Operation op = switch (operation) {
            case AND -> RegisterSpawnPlacementsEvent.Operation.AND;
            case OR -> RegisterSpawnPlacementsEvent.Operation.OR;
            case REPLACE -> RegisterSpawnPlacementsEvent.Operation.REPLACE;
        };
        if (placementType != null && heightmap != null) {
            delegate.register(entityType, placementType, heightmap, predicate, op);
        } else {
            // For AND/OR without explicit placement type, use existing defaults
            delegate.register(entityType, SpawnPlacements.getPlacementType(entityType), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, op);
        }
    }

    public enum Operation {
        AND, OR, REPLACE
    }
}
