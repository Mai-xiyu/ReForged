package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * /neoforge entity — lists entity counts per type in the current dimension.
 */
public class EntityCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("entity")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.literal("list")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    Map<EntityType<?>, Integer> counts = new HashMap<>();
                    for (Entity entity : level.getAllEntities()) {
                        counts.merge(entity.getType(), 1, Integer::sum);
                    }
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("Entity counts in " + level.dimension().location() + ":"), false);
                    counts.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                        .limit(20)
                        .forEach(entry -> {
                            var key = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
                            ctx.getSource().sendSuccess(() ->
                                Component.literal(String.format("  %s: %d", key, entry.getValue())), false);
                        });
                    return 1;
                }));
    }
}
