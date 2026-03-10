package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterShadersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
	private final ResourceProvider resourceProvider;
	private final List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList;

	public RegisterShadersEvent(net.minecraftforge.client.event.RegisterShadersEvent delegate) {
		this.resourceProvider = delegate.getResourceProvider();
		this.shaderList = null;
	}

	public RegisterShadersEvent(ResourceProvider resourceProvider, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList) {
		this.resourceProvider = resourceProvider;
		this.shaderList = shaderList;
	}

	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
		if (shaderList == null) {
			throw new IllegalStateException("Shader registration is not available for this wrapper instance");
		}
		shaderList.add(Pair.of(shaderInstance, onLoaded));
	}
}
