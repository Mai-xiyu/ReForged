package net.neoforged.neoforge.event;

import net.minecraft.core.RegistryAccess;
import net.neoforged.bus.api.Event;

public class TagsUpdatedEvent extends Event {
	private final RegistryAccess registryAccess;
	private final UpdateCause updateCause;
	private final boolean integratedServer;

	public TagsUpdatedEvent() {
		this.registryAccess = null;
		this.updateCause = null;
		this.integratedServer = false;
	}

	public TagsUpdatedEvent(RegistryAccess registryAccess, boolean fromClientPacket, boolean isIntegratedServerConnection) {
		this.registryAccess = registryAccess;
		this.updateCause = fromClientPacket ? UpdateCause.CLIENT_PACKET_RECEIVED : UpdateCause.SERVER_DATA_LOAD;
		this.integratedServer = isIntegratedServerConnection;
	}

	public TagsUpdatedEvent(net.minecraftforge.event.TagsUpdatedEvent delegate) {
		this(delegate.getRegistryAccess(), delegate.getUpdateCause() == net.minecraftforge.event.TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED, delegate.getUpdateCause() != net.minecraftforge.event.TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD && !delegate.shouldUpdateStaticData());
	}

	public RegistryAccess getRegistryAccess() {
		return registryAccess;
	}

	public UpdateCause getUpdateCause() {
		return updateCause;
	}

	public boolean shouldUpdateStaticData() {
		return updateCause == UpdateCause.SERVER_DATA_LOAD || !integratedServer;
	}

	public enum UpdateCause {
		SERVER_DATA_LOAD,
		CLIENT_PACKET_RECEIVED
	}
}
