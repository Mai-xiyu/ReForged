package net.neoforged.neoforge.event.tick;

import net.minecraft.server.MinecraftServer;
import java.util.function.BooleanSupplier;

/**
 * NeoForge ServerTickEvent with Forge wrapper constructors.
 */
public class ServerTickEvent extends net.neoforged.bus.api.Event {
    private MinecraftServer server;
    private BooleanSupplier haveTime;

    /** Required by Forge's EventListenerHelper */
    public ServerTickEvent() {}

    public ServerTickEvent(MinecraftServer server, BooleanSupplier haveTime) {
        this.server = server;
        this.haveTime = haveTime;
    }

    public MinecraftServer getServer() { return server; }
    public boolean haveTime() { return haveTime != null && haveTime.getAsBoolean(); }

    public static class Pre extends ServerTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Pre() { super(); }

        public Pre(MinecraftServer server, BooleanSupplier haveTime) { super(server, haveTime); }

        /** Wrapper constructor */
        public Pre(net.minecraftforge.event.TickEvent.ServerTickEvent.Pre forge) {
            super(forge.getServer(), forge::haveTime);
        }
    }

    public static class Post extends ServerTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Post() { super(); }

        public Post(MinecraftServer server, BooleanSupplier haveTime) { super(server, haveTime); }

        /** Wrapper constructor */
        public Post(net.minecraftforge.event.TickEvent.ServerTickEvent.Post forge) {
            super(forge.getServer(), forge::haveTime);
        }
    }
}
