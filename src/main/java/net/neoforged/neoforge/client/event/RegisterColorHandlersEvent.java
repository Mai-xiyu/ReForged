package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.event.IModBusEvent;

/**
 * NeoForge shim wrapping Forge's RegisterColorHandlersEvent sub-events.
 *
 * <p>Each inner class wraps the corresponding Forge event, delegating method calls.
 * The event bus adapter creates instances of these wrappers when Forge fires its events.</p>
 *
 * <p>Extends {@link net.neoforged.bus.api.Event} so NeoForge code (e.g. Registrate's
 * {@code OneTimeEventReceiver}) can safely cast to {@code Event}.</p>
 */
public abstract class RegisterColorHandlersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    public static class Item extends RegisterColorHandlersEvent {
        private final net.minecraftforge.client.event.RegisterColorHandlersEvent.Item delegate;

        public Item(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item delegate) {
            this.delegate = delegate;
        }

        public void register(ItemColor itemColor, ItemLike... items) {
            delegate.register(itemColor, items);
        }

        public ItemColors getItemColors() {
            return delegate.getItemColors();
        }

        public BlockColors getBlockColors() {
            return delegate.getBlockColors();
        }
    }

    public static class Block extends RegisterColorHandlersEvent {
        private final net.minecraftforge.client.event.RegisterColorHandlersEvent.Block delegate;

        public Block(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block delegate) {
            this.delegate = delegate;
        }

        public void register(BlockColor blockColor, net.minecraft.world.level.block.Block... blocks) {
            delegate.register(blockColor, blocks);
        }

        public BlockColors getBlockColors() {
            return delegate.getBlockColors();
        }
    }

    public static class ColorResolvers extends RegisterColorHandlersEvent {
        private final ImmutableList.Builder<ColorResolver> builder;

        public ColorResolvers(ImmutableList.Builder<ColorResolver> builder) {
            this.builder = builder;
        }

        public void register(ColorResolver resolver) {
            this.builder.add(resolver);
        }
    }
}
