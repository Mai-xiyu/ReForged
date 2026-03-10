package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link CommandSourceStack}.
 */
public interface ICommandSourceStackExtension {

    private CommandSourceStack self() { return (CommandSourceStack) this; }

    default Scoreboard getScoreboard() {
        return self().getServer().getScoreboard();
    }

    @Nullable
    default AdvancementHolder getAdvancement(ResourceLocation id) {
        return self().getServer().getAdvancements().get(id);
    }

    default RecipeManager getRecipeManager() {
        return self().getServer().getRecipeManager();
    }

    default Level getUnsidedLevel() {
        return self().getLevel();
    }
}
