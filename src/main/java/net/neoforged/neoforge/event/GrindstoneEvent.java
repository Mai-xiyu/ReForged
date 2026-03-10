package net.neoforged.neoforge.event;

import net.minecraft.world.item.ItemStack;

/**
 * Events fired for grindstone interactions.
 */
public class GrindstoneEvent extends net.neoforged.bus.api.Event {

    /**
     * Fired when items are placed into the grindstone.
     * Allows modifying the output or canceling.
     */
    public static class OnPlaceItem extends GrindstoneEvent {
        private final ItemStack topItem;
        private final ItemStack bottomItem;
        private ItemStack output = ItemStack.EMPTY;
        private int xp = -1;

        public OnPlaceItem(ItemStack topItem, ItemStack bottomItem, int xp) {
            this.topItem = topItem;
            this.bottomItem = bottomItem;
            this.xp = xp;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public OnPlaceItem(net.minecraftforge.event.GrindstoneEvent.OnPlaceItem delegate) {
            this(delegate.getTopItem(), delegate.getBottomItem(), delegate.getXp());
        }

        public ItemStack getTopItem() { return topItem; }
        public ItemStack getBottomItem() { return bottomItem; }
        public ItemStack getOutput() { return output; }
        public void setOutput(ItemStack output) { this.output = output; }
        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }

        @Override public boolean isCancelable() { return true; }
    }

    /**
     * Fired when the output of a grindstone is taken.
     */
    public static class OnTakeItem extends GrindstoneEvent {
        private final ItemStack topItem;
        private final ItemStack bottomItem;
        private ItemStack newTop = ItemStack.EMPTY;
        private ItemStack newBottom = ItemStack.EMPTY;
        private int xp = -1;

        public OnTakeItem(ItemStack topItem, ItemStack bottomItem, int xp) {
            this.topItem = topItem;
            this.bottomItem = bottomItem;
            this.xp = xp;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public OnTakeItem(net.minecraftforge.event.GrindstoneEvent.OnTakeItem delegate) {
            this(delegate.getTopItem(), delegate.getBottomItem(), delegate.getXp());
        }

        public ItemStack getTopItem() { return topItem; }
        public ItemStack getBottomItem() { return bottomItem; }
        public ItemStack getNewTopItem() { return newTop; }
        public void setNewTopItem(ItemStack stack) { this.newTop = stack; }
        public ItemStack getNewBottomItem() { return newBottom; }
        public void setNewBottomItem(ItemStack stack) { this.newBottom = stack; }
        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }
    }
}
