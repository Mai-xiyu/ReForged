package net.neoforged.neoforge.event.entity.living;

import java.util.List;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Proxy: NeoForge MobSplitEvent */
public class MobSplitEvent extends Event implements ICancellableEvent {
	private final Mob parent;
	private final List<Mob> children;

	public MobSplitEvent(Mob parent, List<Mob> children) {
		this.parent = parent;
		this.children = children;
	}

	public Mob getParent() {
		return parent;
	}

	public List<Mob> getChildren() {
		return children;
	}
}
