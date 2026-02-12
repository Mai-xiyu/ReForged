package net.neoforged.neoforge.event.tick;

import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge EntityTickEvent */
public class EntityTickEvent extends Event {
    public static class Pre extends EntityTickEvent {}
    public static class Post extends EntityTickEvent {}
}
