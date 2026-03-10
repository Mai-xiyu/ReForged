package net.neoforged.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.IItemDecorator;

/**
 * Fired to allow registration of item decorators (overlays on item icons).
 */
public class RegisterItemDecorationsEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<Item, List<IItemDecorator>> decorators;

    public RegisterItemDecorationsEvent(Map<Item, List<IItemDecorator>> decorators) {
        this.decorators = decorators;
    }

    public void register(ItemLike itemLike, IItemDecorator decorator) {
        decorators.computeIfAbsent(itemLike.asItem(), k -> new ArrayList<>()).add(decorator);
    }
}
