package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * /neoforge tags — query tags from registries.
 */
public class TagsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tags")
            .requires(cs -> cs.hasPermission(0))
            .then(Commands.literal("list")
                .then(Commands.argument("registry", StringArgumentType.string())
                    .executes(ctx -> {
                        String registryName = StringArgumentType.getString(ctx, "registry");
                        ResourceLocation registryId = ResourceLocation.tryParse(registryName);
                        if (registryId == null) {
                            ctx.getSource().sendFailure(Component.literal("Invalid registry: " + registryName));
                            return 0;
                        }
                        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
                        if (registry == null) {
                            ctx.getSource().sendFailure(Component.literal("Unknown registry: " + registryId));
                            return 0;
                        }
                        return listTags(ctx.getSource(), registry);
                    })));
    }

    private static <T> int listTags(CommandSourceStack source, Registry<T> registry) {
        var tags = registry.getTagNames().toList();
        source.sendSuccess(() -> Component.literal("Tags in " + registry.key().location() + " (" + tags.size() + "):"), false);
        for (var tagKey : tags) {
            var tag = registry.getTag(tagKey);
            int size = tag.map(holders -> holders.size()).orElse(0);
            source.sendSuccess(() ->
                Component.literal(String.format("  #%s (%d entries)", tagKey.location(), size)), false);
        }
        return tags.size();
    }
}
