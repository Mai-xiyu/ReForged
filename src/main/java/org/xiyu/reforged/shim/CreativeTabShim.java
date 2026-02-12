package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * CreativeTabShim — Bridges NeoForge's creative tab registration to Forge's.
 *
 * <h3>NeoForge API</h3>
 * <pre>
 * CREATIVE_TABS.register("my_tab", () -&gt; CreativeModeTab.builder()
 *     .title(Component.translatable("tab.mymod"))
 *     .icon(() -&gt; new ItemStack(MY_ITEM.get()))
 *     .displayItems((params, output) -&gt; { output.accept(MY_ITEM.get()); })
 *     .build());
 * </pre>
 *
 * <h3>Forge API</h3>
 * <p>Forge uses the same vanilla {@code CreativeModeTab.Builder} API,
 * but registration goes through {@code DeferredRegister<CreativeModeTab>}
 * with Forge's registry system.</p>
 *
 * <p>The APIs are similar enough that direct mapping works for most cases.
 * This shim handles edge cases and NeoForge-specific extensions.</p>
 */
public final class CreativeTabShim {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Create a DeferredRegister for creative tabs.
     * Both NeoForge and Forge use vanilla's Registries.CREATIVE_MODE_TAB.
     */
    public static DeferredRegister<CreativeModeTab> createRegister(String modId) {
        return DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId);
    }

    /**
     * NeoForge's event for populating creative tabs.
     * In Forge, this is {@code BuildCreativeModeTabContentsEvent}.
     * The event classes are structurally compatible after package remapping.
     */
    public static void registerTabContents(IEventBus modBus, Consumer<BuildCreativeModeTabContentsEvent> handler) {
        modBus.addListener(handler);
        LOGGER.debug("[ReForged] Registered creative tab contents handler");
    }

    private CreativeTabShim() {}
}
