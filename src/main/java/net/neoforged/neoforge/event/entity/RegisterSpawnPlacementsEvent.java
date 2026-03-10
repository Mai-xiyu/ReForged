package net.neoforged.neoforge.event.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Fired to allow mods to register spawn placements for entities.
 * Delegates to the underlying Forge SpawnPlacements.DATA_BY_TYPE map.
 */
public class RegisterSpawnPlacementsEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<EntityType<?>, ?> dataByType;

    @SuppressWarnings("unchecked")
    private static Map<EntityType<?>, Object> getDataByType() {
        if (dataByType == null) {
            synchronized (RegisterSpawnPlacementsEvent.class) {
                if (dataByType == null) {
                    try {
                        Field field = SpawnPlacements.class.getDeclaredField("DATA_BY_TYPE");
                        field.setAccessible(true);
                        dataByType = (Map<EntityType<?>, ?>) field.get(null);
                    } catch (Exception e) {
                        LOGGER.error("[ReForged] Failed to access SpawnPlacements.DATA_BY_TYPE", e);
                    }
                }
            }
        }
        return (Map<EntityType<?>, Object>) dataByType;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> void register(
            EntityType<T> entityType,
            SpawnPlacementType placementType,
            Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> predicate,
            Operation operation) {
        try {
            Map<EntityType<?>, Object> map = getDataByType();
            if (map == null) {
                LOGGER.warn("[ReForged] Cannot register spawn placement: DATA_BY_TYPE unavailable");
                return;
            }
            // Use Forge SpawnPlacementRegisterEvent.MergedSpawnPredicate if available,
            // or directly create SpawnPlacements.Data
            if (operation == Operation.REPLACE || !map.containsKey(entityType)) {
                // Create new Data entry
                Class<?> dataClass = Class.forName("net.minecraft.world.entity.SpawnPlacements$Data");
                var constructor = dataClass.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                Object data = constructor.newInstance(heightmap, placementType, predicate);
                map.put(entityType, data);
            } else {
                // AND/OR with existing predicate
                Object existingData = map.get(entityType);
                Field predicateField = existingData.getClass().getDeclaredField("predicate");
                predicateField.setAccessible(true);
                SpawnPlacements.SpawnPredicate<T> existing = (SpawnPlacements.SpawnPredicate<T>) predicateField.get(existingData);

                SpawnPlacements.SpawnPredicate<T> combined;
                if (operation == Operation.AND) {
                    combined = (type, level, spawnType, pos, random) ->
                            existing.test(type, level, spawnType, pos, random) && predicate.test(type, level, spawnType, pos, random);
                } else { // OR
                    combined = (type, level, spawnType, pos, random) ->
                            existing.test(type, level, spawnType, pos, random) || predicate.test(type, level, spawnType, pos, random);
                }
                predicateField.set(existingData, combined);
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to register spawn placement for {}: {}", entityType, e.getMessage());
        }
    }

    public enum Operation { AND, OR, REPLACE }
}
