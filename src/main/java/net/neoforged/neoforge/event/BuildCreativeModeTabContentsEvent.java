package net.neoforged.neoforge.event;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: NeoForge BuildCreativeModeTabContentsEvent.
 * Fired when the contents of a specific creative mode tab are being populated.
 */
public final class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {
    private final CreativeModeTab tab;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final LinkedHashSet<ItemStack> parentEntries;
    private final LinkedHashSet<ItemStack> searchEntries;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final net.minecraftforge.event.BuildCreativeModeTabContentsEvent forgeEvent;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey,
                                              CreativeModeTab.ItemDisplayParameters parameters,
                                              LinkedHashSet<ItemStack> parentEntries,
                                              LinkedHashSet<ItemStack> searchEntries) {
        this.tab = tab;
        this.tabKey = tabKey;
        this.parameters = parameters;
        this.parentEntries = parentEntries;
        this.searchEntries = searchEntries;
        this.forgeEvent = null;
    }

    /**
     * Bridge constructor used by ReForged's event-bus adapter to wrap Forge's
     * BuildCreativeModeTabContentsEvent into a NeoForge-compatible event object.
     */
    public BuildCreativeModeTabContentsEvent(net.minecraftforge.event.BuildCreativeModeTabContentsEvent forgeEvent) {
        this.tab = forgeEvent.getTab();
        this.tabKey = forgeEvent.getTabKey();
        this.parameters = forgeEvent.getParameters();
        this.parentEntries = new LinkedHashSet<>();
        this.searchEntries = new LinkedHashSet<>();
        this.forgeEvent = forgeEvent;

        // Mirror current entries so NeoForge listeners see a coherent snapshot.
        for (Map.Entry<?, ?> entry : forgeEvent.getEntries()) {
            ItemStack stack = (ItemStack) entry.getKey();
            CreativeModeTab.TabVisibility visibility = (CreativeModeTab.TabVisibility) entry.getValue();
            if (isParentTab(visibility)) {
                this.parentEntries.add(stack);
            }
            if (isSearchTab(visibility)) {
                this.searchEntries.add(stack);
            }
        }
    }

    public CreativeModeTab getTab() {
        return this.tab;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return this.tabKey;
    }

    public FeatureFlagSet getFlags() {
        return this.parameters.enabledFeatures();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return parameters;
    }

    public boolean hasPermissions() {
        return this.parameters.hasPermissions();
    }

    public Set<ItemStack> getParentEntries() {
        return Collections.unmodifiableSet(this.parentEntries);
    }

    public Set<ItemStack> getSearchEntries() {
        return Collections.unmodifiableSet(this.searchEntries);
    }

    @Override
    public void accept(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        if (forgeEvent != null) {
            // Keep Forge's backing map authoritative so tab contents are actually populated.
            forgeEvent.accept(newEntry, visibility);
        }
        if (isParentTab(visibility)) {
            parentEntries.add(newEntry);
        }
        if (isSearchTab(visibility)) {
            searchEntries.add(newEntry);
        }
    }

    static boolean isParentTab(CreativeModeTab.TabVisibility visibility) {
        return visibility == CreativeModeTab.TabVisibility.PARENT_TAB_ONLY
                || visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
    }

    static boolean isSearchTab(CreativeModeTab.TabVisibility visibility) {
        return visibility == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY
                || visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
    }
}
