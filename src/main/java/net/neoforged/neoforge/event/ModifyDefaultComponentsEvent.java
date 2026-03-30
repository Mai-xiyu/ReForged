package net.neoforged.neoforge.event;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.slf4j.Logger;

/**
 * Fired to allow mods to modify default data components on items.
 * In the ReForged shim, we apply the patch via reflection on Item's components field.
 */
public final class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Field componentsField;

    static {
        try {
            // Find the DataComponentMap field on Item (private final)
            for (Field f : Item.class.getDeclaredFields()) {
                if (DataComponentMap.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    componentsField = f;
                    break;
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[ReForged] ModifyDefaultComponentsEvent: Failed to find Item.components field", t);
        }
    }

    public ModifyDefaultComponentsEvent() {}

    /**
     * Modifies the default components of the given item.
     */
    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        Item actualItem = item.asItem();
        var builder = DataComponentPatch.builder();
        patch.accept(builder);
        DataComponentPatch builtPatch = builder.build();

        if (componentsField == null) {
            LOGGER.warn("[ReForged] ModifyDefaultComponentsEvent: Cannot apply patch — components field not found");
            return;
        }

        try {
            DataComponentMap existing = (DataComponentMap) componentsField.get(actualItem);
            PatchedDataComponentMap patched = new PatchedDataComponentMap(existing);
            patched.applyPatch(builtPatch);
            componentsField.set(actualItem, patched);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] ModifyDefaultComponentsEvent: Failed to apply patch to {}: {}", actualItem, t.getMessage());
        }
    }

    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        getAllItems().filter(predicate).forEach(item -> modify(item, patch));
    }

    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
