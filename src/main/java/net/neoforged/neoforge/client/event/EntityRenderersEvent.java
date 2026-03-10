package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

/**
 * NeoForge wrapper events for entity/block-entity renderer registration.
 * Each inner class wraps the corresponding Forge event via a constructor.
 *
 * <p>Extends {@link net.neoforged.bus.api.Event} so NeoForge code
 * can safely cast to {@code Event}.</p>
 */
public abstract class EntityRenderersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers}. */
    public static class RegisterRenderers extends EntityRenderersEvent {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate;

        public RegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate) {
            this.delegate = delegate;
        }

        public <T extends Entity> void registerEntityRenderer(EntityType<T> entityType,
                                                               EntityRendererProvider<T> provider) {
            delegate.registerEntityRenderer(entityType, provider);
        }

        public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
                                                                         BlockEntityRendererProvider<T> provider) {
            delegate.registerBlockEntityRenderer(type, provider);
        }
    }

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static class RegisterLayerDefinitions extends EntityRenderersEvent {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate;

        public RegisterLayerDefinitions(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate) {
            this.delegate = delegate;
        }

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            delegate.registerLayerDefinition(layerLocation, supplier);
        }
    }

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.AddLayers}. */
    public static class AddLayers extends EntityRenderersEvent {
        private final net.minecraftforge.client.event.EntityRenderersEvent.AddLayers delegate;

        public AddLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers delegate) {
            this.delegate = delegate;
        }

        public Set<PlayerSkin.Model> getSkins() {
            return delegate.getSkins();
        }

        /** NeoForge API name. */
        @Nullable
        @SuppressWarnings("unchecked")
        public <R extends EntityRenderer<? extends Player>> R getSkin(PlayerSkin.Model skinName) {
            return delegate.getPlayerSkin(skinName);
        }

        /** Forge-compatible alias. */
        @Nullable
        @SuppressWarnings("unchecked")
        public <R extends EntityRenderer<? extends Player>> R getPlayerSkin(PlayerSkin.Model skinName) {
            return delegate.getPlayerSkin(skinName);
        }

        /** NeoForge API name — accepts any Entity type (NeoForge signature). */
        @Nullable
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T extends Entity, R extends EntityRenderer<T>> R getRenderer(EntityType<? extends T> entityType) {
            return (R) delegate.getEntityRenderer((EntityType) entityType);
        }

        /** Forge-compatible alias — restricted to LivingEntity. */
        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends LivingEntity, R extends EntityRenderer<T>> R getEntityRenderer(EntityType<? extends T> entityType) {
            return delegate.getEntityRenderer(entityType);
        }

        public EntityModelSet getEntityModels() {
            return delegate.getEntityModels();
        }

        public EntityRendererProvider.Context getContext() {
            return delegate.getContext();
        }
    }

    public static class CreateSkullModels extends EntityRenderersEvent {
        private final ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder;
        private final EntityModelSet entityModelSet;

        public CreateSkullModels(net.minecraftforge.client.event.EntityRenderersEvent.CreateSkullModels delegate) {
            this.builder = null;
            this.entityModelSet = delegate.getEntityModelSet();
        }

        public CreateSkullModels(ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder, EntityModelSet entityModelSet) {
            this.builder = builder;
            this.entityModelSet = entityModelSet;
        }

        public EntityModelSet getEntityModelSet() {
            return entityModelSet;
        }

        public void registerSkullModel(SkullBlock.Type type, SkullModelBase model) {
            if (builder == null) {
                throw new IllegalStateException("Skull model registration is not available for this wrapper instance");
            }
            builder.put(type, model);
        }
    }
}
