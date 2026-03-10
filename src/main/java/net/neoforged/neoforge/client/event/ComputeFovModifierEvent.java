package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Fired to allow modification of the player's field-of-view modifier.
 */
public class ComputeFovModifierEvent extends net.neoforged.bus.api.Event {
    private final Player player;
    private final float fovModifier;
    private float newFovModifier;

    public ComputeFovModifierEvent(Player player, float fovModifier) {
        this.player = player;
        this.fovModifier = fovModifier;
        this.newFovModifier = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, fovModifier);
    }

    public Player getPlayer() { return player; }
    public float getFovModifier() { return fovModifier; }
    public float getNewFovModifier() { return newFovModifier; }
    public void setNewFovModifier(float newFovModifier) { this.newFovModifier = newFovModifier; }
}
