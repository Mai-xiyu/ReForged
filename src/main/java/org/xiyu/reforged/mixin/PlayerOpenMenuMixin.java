package org.xiyu.reforged.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Adds the NeoForge-style {@code openMenu(MenuProvider, Consumer)} method
 * to {@link Player} that returns {@link OptionalInt}.
 *
 * <p>In NeoForge 1.21.1, Player has {@code openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)}
 * returning OptionalInt. In Forge 1.21, only {@code IForgeServerPlayer.openMenu(MenuProvider, Consumer<FriendlyByteBuf>)}
 * exists, returning void. This bridges the gap.</p>
 */
@Mixin(Player.class)
public abstract class PlayerOpenMenuMixin {

    @Shadow
    public abstract OptionalInt openMenu(@Nullable MenuProvider provider);

    /**
     * NeoForge-style openMenu with extra data consumer.
     * Delegates to Forge's IForgeServerPlayer.openMenu for actual packet handling,
     * then returns the container ID.
     */
    @SuppressWarnings("unchecked")
    public OptionalInt openMenu(@Nullable MenuProvider provider, @Nullable Consumer<?> extraDataWriter) {
        if (provider == null) return OptionalInt.empty();

        Player self = (Player) (Object) this;

        if (self instanceof ServerPlayer sp) {
            // Delegate to Forge's IForgeServerPlayer.openMenu(MenuProvider, Consumer<FriendlyByteBuf>)V
            // which handles container creation, network packet, and menu initialization.
            // The Consumer<FriendlyByteBuf> cast is safe due to type erasure;
            // common use cases (e.g. buf -> buf.writeBlockPos(pos)) work with FriendlyByteBuf.
            Consumer<FriendlyByteBuf> writer = extraDataWriter != null
                    ? (Consumer<FriendlyByteBuf>) (Consumer<?>) extraDataWriter
                    : buf -> {};
            ((net.minecraftforge.common.extensions.IForgeServerPlayer) sp).openMenu(provider, writer);

            // Return the container ID if a menu was opened
            if (sp.containerMenu != sp.inventoryMenu) {
                return OptionalInt.of(sp.containerMenu.containerId);
            }
            return OptionalInt.empty();
        }

        // Client-side or non-ServerPlayer: use single-arg fallback
        return this.openMenu(provider);
    }
}
