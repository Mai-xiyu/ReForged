package net.neoforged.neoforge.client.event;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;

/**
 * NeoForge shim wrapping Forge's RegisterColorHandlersEvent sub-events.
 *
 * <p>Each inner class wraps the corresponding Forge event, delegating method calls.
 * The event bus adapter creates instances of these wrappers when Forge fires its events.</p>
 */
public class RegisterColorHandlersEvent {

    public static class Item {
        private final net.minecraftforge.client.event.RegisterColorHandlersEvent.Item delegate;

        public Item(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item delegate) {
            this.delegate = delegate;
        }

        public void register(ItemColor itemColor, ItemLike... items) {
            delegate.register(itemColor, items);
        }
    }

    public static class Block {
        private final net.minecraftforge.client.event.RegisterColorHandlersEvent.Block delegate;

        public Block(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block delegate) {
            this.delegate = delegate;
        }

        public void register(BlockColor blockColor, net.minecraft.world.level.block.Block... blocks) {
            delegate.register(blockColor, blocks);
        }
    }
}
