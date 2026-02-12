package net.neoforged.neoforge.event.entity.living;

import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge LivingExperienceDropEvent */
public class LivingExperienceDropEvent extends Event {
    private int droppedExperience;
    public LivingExperienceDropEvent(int xp) { this.droppedExperience = xp; }
    public int getDroppedExperience() { return droppedExperience; }
    public void setDroppedExperience(int xp) { this.droppedExperience = xp; }
}
