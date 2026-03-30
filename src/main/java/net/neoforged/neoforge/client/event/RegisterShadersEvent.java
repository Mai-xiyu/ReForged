package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterShadersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
	private final ResourceProvider resourceProvider;
	private final List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList;
	private final net.minecraftforge.client.event.RegisterShadersEvent forgeDelegate;

	public RegisterShadersEvent(net.minecraftforge.client.event.RegisterShadersEvent delegate) {
		this.resourceProvider = delegate.getResourceProvider();
		this.shaderList = new ArrayList<>();
		this.forgeDelegate = delegate;
	}

	public RegisterShadersEvent(ResourceProvider resourceProvider, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList) {
		this.resourceProvider = resourceProvider;
		this.shaderList = shaderList;
		this.forgeDelegate = null;
	}

	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
		if (forgeDelegate != null) {
			// Forward to the real Forge event so shaders are actually registered in Forge's pipeline
			try {
				forgeDelegate.registerShader(shaderInstance, onLoaded);
			} catch (Throwable t) {
				// Fallback: store locally
				shaderList.add(Pair.of(shaderInstance, onLoaded));
			}
		} else if (shaderList != null) {
			shaderList.add(Pair.of(shaderInstance, onLoaded));
		}
	}
}
