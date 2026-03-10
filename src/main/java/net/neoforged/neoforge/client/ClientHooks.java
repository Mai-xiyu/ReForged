package net.neoforged.neoforge.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * NeoForge ClientHooks — delegates to Forge's ForgeHooksClient where possible.
 * Method signatures match NeoForge's API; the implementation forwards to Forge.
 */
public class ClientHooks {

    // ── GUI Layer Management ──────────────────────────────

    public static void pushGuiLayer(Minecraft minecraft, Screen screen) {
        ForgeHooksClient.pushGuiLayer(minecraft, screen);
    }

    public static void popGuiLayer(Minecraft minecraft) {
        ForgeHooksClient.popGuiLayer(minecraft);
    }

    public static void clearGuiLayers(Minecraft minecraft) {
        ForgeHooksClient.clearGuiLayers(minecraft);
    }

    public static void resizeGuiLayers(Minecraft minecraft, int width, int height) {
        ForgeHooksClient.resizeGuiLayers(minecraft, width, height);
    }

    public static float getGuiFarPlane() {
        return ForgeHooksClient.getGuiFarPlane();
    }

    // ── Armor / Entity Rendering ──────────────────────────

    public static ResourceLocation getArmorTexture(Entity entity, ItemStack armor, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot) {
        return ForgeHooksClient.getArmorTexture(entity, armor, slot, layer, innerModel);
    }

    public static Model getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> _default) {
        return ForgeHooksClient.getArmorModel(entityLiving, itemStack, slot, _default);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> void copyModelProperties(HumanoidModel<T> original, HumanoidModel<?> replacement) {
        ForgeHooksClient.copyModelProperties(original, replacement);
    }

    public static boolean shouldCauseReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, int slot) {
        return ForgeHooksClient.shouldCauseReequipAnimation(from, to, slot);
    }

    public static boolean shouldRenderEffect(MobEffectInstance effectInstance) {
        return ForgeHooksClient.shouldRenderEffect(effectInstance);
    }

    public static boolean isNameplateInRenderDistance(Entity entity, double squareDistance) {
        return ForgeHooksClient.isNameplateInRenderDistance(entity, squareDistance);
    }

    // ── Pause / Tick Events ───────────────────────────────

    public static boolean onClientPauseChangePre(boolean pause) {
        return ForgeHooksClient.onClientPauseChangePre(pause);
    }

    public static void onClientPauseChangePost(boolean pause) {
        ForgeHooksClient.onClientPauseChangePost(pause);
    }

    public static void fireClientTickPre() {
        // NeoForge fires ClientTickEvent.Pre — not available in Forge; no-op
    }

    public static void fireClientTickPost() {
        // NeoForge fires ClientTickEvent.Post — not available in Forge; no-op
    }

    public static void fireRenderFramePre(Object partialTick) {
        // NeoForge fires RenderFrameEvent.Pre — not available in Forge; no-op
    }

    public static void fireRenderFramePost(Object partialTick) {
        // NeoForge fires RenderFrameEvent.Post — not available in Forge; no-op
    }

    // ── Level Rendering ───────────────────────────────────

    public static boolean onDrawHighlight(LevelRenderer context, Camera camera, HitResult target, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        return ForgeHooksClient.onDrawHighlight(context, camera, target, partialTick, poseStack, bufferSource);
    }

    public static void dispatchRenderStage(RenderType renderType, LevelRenderer levelRenderer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(renderType, levelRenderer, modelViewMatrix, projectionMatrix, renderTick, camera, frustum);
    }

    public static void dispatchRenderStage(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        // NeoForge's Stage-based overload — delegate to the RenderType variant where possible
        // Forge uses RenderType-based dispatch, so this is a compatibility shim
    }

    // ── First-Person Rendering ────────────────────────────

    public static boolean renderSpecificFirstPersonHand(InteractionHand hand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return ForgeHooksClient.renderSpecificFirstPersonHand(hand, poseStack, bufferSource, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack);
    }

    public static boolean renderSpecificFirstPersonArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        // NeoForge-specific — not available in Forge; always return false (don't cancel)
        return false;
    }

    // ── Texture & Color Init ──────────────────────────────

    public static void onTextureAtlasStitched(TextureAtlas atlas) {
        // Forge 1.21.1 does not expose a matching public post-stitch event here.
        // Keep the NeoForge API entrypoint to preserve binary compatibility.
    }

    public static void onBlockColorsInit(BlockColors blockColors) {
        ForgeHooksClient.onBlockColorsInit(blockColors);
    }

    public static void onItemColorsInit(ItemColors itemColors, BlockColors blockColors) {
        ForgeHooksClient.onItemColorsInit(itemColors, blockColors);
    }

    // ── Model Layer Definitions ───────────────────────────

    public static void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
        ForgeHooksClient.registerLayerDefinition(layerLocation, supplier);
    }

    public static void loadLayerDefinitions(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        ForgeHooksClient.loadLayerDefinitions(builder);
    }

    // ── Screen Rendering ──────────────────────────────────

    public static void drawScreen(Screen screen, GuiGraphics guiGraphics,
                                   int mouseX, int mouseY, float partialTick) {
        ForgeHooksClient.drawScreen(screen, guiGraphics, mouseX, mouseY, partialTick);
    }

    // ── FOV & Camera ──────────────────────────────────────

    public static float getFieldOfViewModifier(Player entity, float fovModifier) {
        return ForgeHooksClient.getFieldOfViewModifier(entity, fovModifier);
    }

    public static double getFieldOfView(GameRenderer renderer, Camera camera, double partialTick, double fov, boolean usedConfiguredFov) {
        // NeoForge fires ViewportEvent.ComputeFov — Forge has getFieldOfViewModifier but not this exact variant
        return fov;
    }

    public static CalculatePlayerTurnEvent getTurnPlayerValues(double mouseSensitivity, boolean cinematicCameraEnabled) {
        return new CalculatePlayerTurnEvent(mouseSensitivity, cinematicCameraEnabled);
    }

    public static float getDetachedCameraDistance(Camera camera, boolean flipped, float entityScale, float distance) {
        // NeoForge fires ViewportEvent.ComputeCameraAngles — Forge doesn't expose this
        return distance;
    }

    // ── Title Screen ──────────────────────────────────────

    public static void renderMainMenu(TitleScreen gui, GuiGraphics guiGraphics, Font font, int width, int height, int alpha) {
        ForgeHooksClient.renderMainMenu(gui, guiGraphics, font, width, height, alpha);
    }

    // ── Sound ─────────────────────────────────────────────

    @Nullable
    public static SoundInstance playSound(SoundEngine manager, SoundInstance sound) {
        return ForgeHooksClient.playSound(manager, sound);
    }

    @Nullable
    public static Music selectMusic(Music situational, @Nullable SoundInstance playing) {
        // NeoForge-specific event — not available in Forge
        return situational;
    }

    // ── Fog & Viewport ────────────────────────────────────

    public static Vector3f getFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, float fogRed, float fogGreen, float fogBlue) {
        return ForgeHooksClient.getFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fogRed, fogGreen, fogBlue);
    }

    public static void onFogRender(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, com.mojang.blaze3d.shaders.FogShape shape) {
        ForgeHooksClient.onFogRender(mode, type, camera, partialTick, renderDistance, nearDistance, farDistance, shape);
    }

    // ── Model Baking ──────────────────────────────────────

    public static void onModifyBakingResult(Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        ForgeHooksClient.onModifyBakingResult(models, modelBakery);
    }

    public static void onModelBake(ModelManager modelManager, Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        ForgeHooksClient.onModelBake(modelManager, models, modelBakery);
    }

    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, net.minecraft.world.item.ItemDisplayContext cameraTransformType, boolean applyLeftHandTransform) {
        model.getTransforms().getTransform(cameraTransformType).apply(applyLeftHandTransform, poseStack);
        return model;
    }

    public static void onRegisterAdditionalModels(Set<ModelResourceLocation> additionalModels) {
        ForgeHooksClient.onRegisterAdditionalModels(additionalModels);
    }

    // ── Block / Fluid Materials ───────────────────────────

    @SuppressWarnings("deprecation")
    public static Material getBlockMaterial(ResourceLocation loc) {
        return ForgeHooksClient.getBlockMaterial(loc);
    }

    public static TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return ForgeHooksClient.getFluidSprites(level, pos, fluidState);
    }

    public static void fillNormal(int[] faceData, Direction facing) {
        ForgeHooksClient.fillNormal(faceData, facing);
    }

    public static boolean calculateFaceWithoutAO(BlockAndTintGetter getter, BlockState state, BlockPos pos, BakedQuad quad, boolean isFaceCubic, float[] brightness, int[] lightmap) {
        return ForgeHooksClient.calculateFaceWithoutAO(getter, state, pos, quad, isFaceCubic, brightness, lightmap);
    }

    public static boolean isBlockInSolidLayer(BlockState state) {
        return ForgeHooksClient.isBlockInSolidLayer(state);
    }

    // ── Entity Shader ─────────────────────────────────────

    public static void loadEntityShader(Entity entity, GameRenderer entityRenderer) {
        ForgeHooksClient.loadEntityShader(entity, entityRenderer);
    }

    // ── Boss Bar ──────────────────────────────────────────

    public static CustomizeGuiOverlayEvent.BossEventProgress onCustomizeBossEventProgress(GuiGraphics guiGraphics, com.mojang.blaze3d.platform.Window window, net.minecraft.client.gui.components.LerpingBossEvent bossInfo, int x, int y, int increment) {
        return ForgeHooksClient.onCustomizeBossEventProgress(guiGraphics, window, bossInfo, x, y, increment);
    }

    // ── Screenshot ────────────────────────────────────────

    public static ScreenshotEvent onScreenshot(NativeImage image, File screenshotFile) {
        return new ScreenshotEvent(image, screenshotFile);
    }

    // ── Game Type Change ──────────────────────────────────

    public static void onClientChangeGameType(net.minecraft.client.multiplayer.PlayerInfo info, net.minecraft.world.level.GameType currentGameMode, net.minecraft.world.level.GameType newGameMode) {
        ForgeHooksClient.onClientChangeGameType(info, currentGameMode, newGameMode);
    }

    // ── Input Events ──────────────────────────────────────

    public static void onMovementInputUpdate(Player player, Input movementInput) {
        ForgeHooksClient.onMovementInputUpdate(player, movementInput);
    }

    // ── Screen Mouse Events ───────────────────────────────

    public static boolean onScreenMouseClickedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        // Delegate to Forge's ScreenEvent系统 via ForgeEventFactoryClient
        return false; // Forge handles this internally in its Screen mixin
    }

    public static boolean onScreenMouseClickedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        return handled;
    }

    public static boolean onScreenMouseReleasedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        return false;
    }

    public static boolean onScreenMouseReleasedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        return handled;
    }

    public static boolean onScreenMouseDragPre(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        return false;
    }

    public static void onScreenMouseDragPost(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        // no-op — Forge handles these events internally
    }

    public static boolean onScreenMouseScrollPre(MouseHandler mouseHelper, Screen guiScreen, double scrollDeltaX, double scrollDeltaY) {
        return false;
    }

    public static void onScreenMouseScrollPost(MouseHandler mouseHelper, Screen guiScreen, double scrollDeltaX, double scrollDeltaY) {
        // no-op
    }

    // ── Screen Key Events ─────────────────────────────────

    public static boolean onScreenKeyPressedPre(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        return false; // Forge handles in its Screen mixin
    }

    public static boolean onScreenKeyPressedPost(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public static boolean onScreenKeyReleasedPre(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public static boolean onScreenKeyReleasedPost(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public static boolean onScreenCharTypedPre(Screen guiScreen, char codePoint, int modifiers) {
        return false;
    }

    public static void onScreenCharTypedPost(Screen guiScreen, char codePoint, int modifiers) {
        // no-op
    }

    // ── Recipe Update ─────────────────────────────────────

    public static void onRecipesUpdated(RecipeManager mgr) {
        ForgeHooksClient.onRecipesUpdated(mgr);
    }

    // ── Raw Input Events ──────────────────────────────────

    public static boolean onMouseButtonPre(int button, int action, int mods) {
        // NeoForge fires InputEvent.MouseButton.Pre — Forge handles this internally
        return false;
    }

    public static void onMouseButtonPost(int button, int action, int mods) {
        // NeoForge fires InputEvent.MouseButton.Post — no-op
    }

    public static boolean onMouseScroll(MouseHandler mouseHelper, double scrollDeltaX, double scrollDeltaY) {
        // NeoForge fires InputEvent.MouseScrollingEvent — Forge handles internally
        return false;
    }

    public static void onKeyInput(int key, int scanCode, int action, int modifiers) {
        ForgeHooksClient.onKeyInput(key, scanCode, action, modifiers);
    }

    public static InputEvent.InteractionKeyMappingTriggered onClickInput(int button, KeyMapping keyBinding, InteractionHand hand) {
        return new InputEvent.InteractionKeyMappingTriggered(button, keyBinding, hand);
    }

    // ── Piston Rendering ──────────────────────────────────

    public static void renderPistonMovedBlocks(BlockPos pos, BlockState state, PoseStack stack, MultiBufferSource bufferSource, Level level, boolean checkSides, int packedOverlay, BlockRenderDispatcher blockRenderer) {
        // Forge handles piston rendering internally; no-op for NeoForge API compat
    }

    // ── Registration Events ───────────────────────────────

    public static void onRegisterParticleProviders(ParticleEngine particleEngine) {
        ForgeHooksClient.onRegisterParticleProviders(particleEngine);
    }

    public static void onRegisterKeyMappings(Options options) {
        ForgeHooksClient.onRegisterKeyMappings(options);
    }

    // ── Player Login/Logout/Respawn Events ────────────────

    public static void firePlayerLogin(MultiPlayerGameMode pc, LocalPlayer player, Connection networkManager) {
        // NeoForge fires ClientPlayerNetworkEvent.LoggingIn — Forge handles this via its own event
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
            new net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn(pc, player, networkManager));
    }

    public static void firePlayerLogout(@Nullable MultiPlayerGameMode pc, @Nullable LocalPlayer player) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
            new net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut(pc, player, player != null ? player.connection.getConnection() : null));
    }

    public static void firePlayerRespawn(MultiPlayerGameMode pc, LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection networkManager) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
            new net.minecraftforge.client.event.ClientPlayerNetworkEvent.Clone(pc, oldPlayer, newPlayer, networkManager));
    }

    // ── Chat Events ───────────────────────────────────────

    @Nullable
    public static Component onClientChat(ChatType.Bound boundChatType, Component message, UUID sender) {
        return ForgeHooksClient.onClientChat(boundChatType, message, sender);
    }

    @Nullable
    public static Component onClientPlayerChat(ChatType.Bound boundChatType, Component message, PlayerChatMessage playerChatMessage, UUID sender) {
        return ForgeHooksClient.onClientPlayerChat(boundChatType, message, playerChatMessage, sender);
    }

    @Nullable
    public static Component onClientSystemChat(Component message, boolean overlay) {
        return ForgeHooksClient.onClientSystemMessage(message, overlay);
    }

    @NotNull
    public static String onClientSendMessage(String message) {
        return ForgeHooksClient.onClientSendMessage(message);
    }

    // ── Render Type ───────────────────────────────────────

    @NotNull
    public static RenderType getEntityRenderType(RenderType chunkRenderType, boolean cull) {
        return ForgeHooksClient.getEntityRenderType(chunkRenderType, cull);
    }

    // ── Tooltip Methods ───────────────────────────────────

    public static Font getTooltipFont(@NotNull ItemStack stack, Font fallbackFont) {
        return ForgeHooksClient.getTooltipFont(stack, fallbackFont);
    }

    public static RenderTooltipEvent.Pre onRenderTooltipPre(
            @NotNull ItemStack stack, GuiGraphics graphics, int x, int y,
            int screenWidth, int screenHeight,
            @NotNull List<ClientTooltipComponent> components,
            @NotNull Font fallbackFont,
            @NotNull ClientTooltipPositioner positioner) {
        return ForgeHooksClient.onRenderTooltipPre(
                stack, graphics, x, y, screenWidth, screenHeight,
                components, fallbackFont, positioner);
    }

    public static RenderTooltipEvent.Color onRenderTooltipColor(
            @NotNull ItemStack stack, GuiGraphics graphics, int x, int y,
            @NotNull Font font, @NotNull List<ClientTooltipComponent> components) {
        return ForgeHooksClient.onRenderTooltipColor(stack, graphics, x, y, font, components);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(
            ItemStack stack, List<? extends FormattedText> textElements,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponents(
                stack, textElements, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(
            ItemStack stack, List<? extends FormattedText> textElements,
            Optional<TooltipComponent> itemComponent,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponents(
                stack, textElements, itemComponent, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponentsFromElements(
            ItemStack stack, List<Either<FormattedText, TooltipComponent>> elements,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponentsFromElements(
                stack, elements, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    // ── Particle Comparator ───────────────────────────────

    public static Comparator<ParticleRenderType> makeParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
        return ForgeHooksClient.makeParticleRenderTypeComparator(renderOrder);
    }

    // ── Screen / Potion / Toast ───────────────────────────

    public static ScreenEvent.RenderInventoryMobEffects onScreenPotionSize(Screen screen, int availableSpace, boolean compact, int horizontalOffset) {
        return ForgeHooksClient.onScreenPotionSize(screen, availableSpace, compact, horizontalOffset);
    }

    public static boolean onToastAdd(Toast toast) {
        return ForgeHooksClient.onToastAdd(toast);
    }

    // ── Block Overlays ────────────────────────────────────

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderFireOverlay(player, mat);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderWaterOverlay(player, mat);
    }

    public static boolean renderBlockOverlay(Player player, PoseStack mat, RenderBlockScreenEffectEvent.OverlayType type, BlockState block, BlockPos pos) {
        return ForgeHooksClient.renderBlockOverlay(player, mat,
                net.minecraftforge.client.event.RenderBlockScreenEffectEvent.OverlayType.valueOf(type.name()),
                block, pos);
    }

    // ── Mipmap / Shader ───────────────────────────────────

    public static int getMaxMipmapLevel(int width, int height) {
        return ForgeHooksClient.getMaxMipmapLevel(width, height);
    }

    public static ResourceLocation getShaderImportLocation(String basePath, boolean isRelative, String importPath) {
        return ForgeHooksClient.getShaderImportLocation(basePath, isRelative, importPath);
    }

    // ── Utility ───────────────────────────────────────────

    public static String fixDomain(String base, String complex) {
        return ForgeHooksClient.fixDomain(base, complex);
    }

    public static Direction getNearestStable(float nX, float nY, float nZ) {
        return ForgeHooksClient.getNearestStable(nX, nY, nZ);
    }

    // ── Sprite Source Types ───────────────────────────────

    public static BiMap<ResourceLocation, SpriteSourceType> makeSpriteSourceTypesMap() {
        return HashBiMap.create();
    }

    @ApiStatus.Internal
    public static void registerSpriteSourceTypes() {
        // NeoForge-specific sprite source type registration — no-op on Forge
    }

    // ── Block Entity Rendering ────────────────────────────

    @ApiStatus.Internal
    public static <T extends BlockEntity> boolean isBlockEntityRendererVisible(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, Frustum frustum) {
        // Delegate to vanilla frustum check
        return frustum.isVisible(blockEntity.getRenderBoundingBox());
    }

    // ── Section Geometry ──────────────────────────────────

    public static List<AddSectionGeometryEvent.AdditionalSectionRenderer> gatherAdditionalRenderers(BlockPos sectionOrigin, Level level) {
        return List.of(); // NeoForge-specific — no-op on Forge
    }

    public static void addAdditionalGeometry(List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers, Function<RenderType, VertexConsumer> getOrCreateBuilder, Object region, PoseStack transformation) {
        // NeoForge-specific — no-op on Forge
    }

    // ── Init ──────────────────────────────────────────────

    @ApiStatus.Internal
    public static void initClientHooks(Minecraft mc, ReloadableResourceManager resourceManager) {
        ForgeHooksClient.initClientHooks(mc, resourceManager);
    }

    // ── Registry Lookup ───────────────────────────────────

    @Nullable
    public static <T> RegistryAccess.RegistryEntry<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        // NeoForge-specific — provides client-side registry lookup
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            var reg = mc.level.registryAccess().registry(key);
            return reg.map(r -> new RegistryAccess.RegistryEntry<>(key, r)).orElse(null);
        }
        return null;
    }

    // ── Effect Tooltip ────────────────────────────────────

    public static List<Component> getEffectTooltip(EffectRenderingInventoryScreen<?> screen, MobEffectInstance effectInst, List<Component> tooltip) {
        // NeoForge-specific — return original tooltip
        return tooltip;
    }

    // ── Recipe Book Types ─────────────────────────────────

    public static net.minecraft.world.inventory.RecipeBookType[] getFilteredRecipeBookTypeValues() {
        return net.minecraft.world.inventory.RecipeBookType.values();
    }

    // ── Material Atlases ──────────────────────────────────

    public static Map<ResourceLocation, ResourceLocation> gatherMaterialAtlases(Map<ResourceLocation, ResourceLocation> vanillaAtlases) {
        // NeoForge fires RegisterMaterialAtlasesEvent — no-op, return vanilla
        return vanillaAtlases;
    }

    // ── Item Model Seams Fix ──────────────────────────────

    public static List<net.minecraft.client.renderer.block.model.BlockElement> fixItemModelSeams(List<net.minecraft.client.renderer.block.model.BlockElement> elements, TextureAtlasSprite sprite) {
        // NeoForge-specific item model seam fix — return as-is on Forge
        return elements;
    }

    // ── Client Disconnect ─────────────────────────────────

    public static boolean onClientDisconnect(Connection connection, Minecraft mc, Screen parent, Component message) {
        return ForgeHooksClient.onClientDisconnect(connection, mc, parent, message);
    }

    /**
     * Triggers a renderer reload (e.g. when experimental pipeline setting changes).
     */
    public static void reloadRenderer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    // ── Inner class: Client shader events ─────────────────

    public static class ClientEvents {
        @Nullable
        private static net.minecraft.client.renderer.ShaderInstance rendertypeEntityTranslucentUnlitShader;

        public static net.minecraft.client.renderer.ShaderInstance getEntityTranslucentUnlitShader() {
            return java.util.Objects.requireNonNull(rendertypeEntityTranslucentUnlitShader,
                    "Attempted to call getEntityTranslucentUnlitShader before shaders have finished loading.");
        }

        public static void registerShaders(Object event) {
            // Delegate to Forge's ClientEvents inner class for shader registration
            rendertypeEntityTranslucentUnlitShader = ForgeHooksClient.ClientEvents.getEntityTranslucentUnlitShader();
        }
    }
}
