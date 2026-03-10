package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.MonsterRoomMob;
import org.jetbrains.annotations.ApiStatus;

/**
 * Hooks for configuring monster room (dungeon) spawner mob weights via data maps.
 */
public class MonsterRoomHooks {
    private static List<MobEntry> monsterRoomMobs = List.of();

    private MonsterRoomHooks() {}

    /**
     * Called when data maps are updated to refresh the monster room mob list.
     */
    @ApiStatus.Internal
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        var dataMap = ((net.neoforged.neoforge.registries.IRegistryExtension<EntityType<?>>) registry)
                .getDataMap(NeoForgeDataMaps.MONSTER_ROOM_MOBS);
        if (dataMap != null && !dataMap.isEmpty()) {
            List<MobEntry> entries = new ArrayList<>();
            dataMap.forEach((key, value) -> {
                EntityType<?> type = registry.get(key.location());
                if (type != null) {
                    entries.add(new MobEntry(type, value.weight()));
                }
            });
            monsterRoomMobs = List.copyOf(entries);
        }
    }

    /**
     * Gets a random entity type from the weighted monster room mob list.
     */
    public static EntityType<?> getRandomMonsterRoomMob(RandomSource rand) {
        if (monsterRoomMobs.isEmpty()) {
            return EntityType.ZOMBIE;
        }
        MobEntry mob = WeightedRandom.getRandomItem(rand, monsterRoomMobs).orElseThrow();
        return mob.type;
    }

    public record MobEntry(EntityType<?> type, Weight weight) implements WeightedEntry {
        @Override
        public Weight getWeight() {
            return weight;
        }
    }
}
