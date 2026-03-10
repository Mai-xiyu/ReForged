package net.neoforged.neoforge.event;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow mods to modify default data components on items.
 */
public final class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    public ModifyDefaultComponentsEvent() {}

    /**
     * Modifies the default components of the given item.
     */
    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        // In our Forge shim, we build the patch but cannot apply it since
        // modifyDefaultComponentsFrom is a NeoForge-only patch on Item.
        // Mods calling this get a no-op at runtime.
        var builder = DataComponentPatch.builder();
        patch.accept(builder);
    }

    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        getAllItems().filter(predicate).forEach(item -> modify(item, patch));
    }

    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
