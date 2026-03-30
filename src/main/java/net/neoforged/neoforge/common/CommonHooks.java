package net.neoforged.neoforge.common;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;

import javax.annotation.Nullable;

public class CommonHooks {
	private CommonHooks() {}

	public static boolean isEntityInvulnerableTo(Entity entity, DamageSource source, boolean isInvulnerable) {
		EntityInvulnerabilityCheckEvent event = new EntityInvulnerabilityCheckEvent(entity, source, isInvulnerable);
		NeoForge.EVENT_BUS.post(event);
		return event.isInvulnerable();
	}

	public static boolean onEntityIncomingDamage(LivingEntity entity, DamageContainer container) {
		return NeoForge.EVENT_BUS.post(new LivingIncomingDamageEvent(entity, container)).isCanceled();
	}

	public static boolean canContinueUsing(ItemStack from, ItemStack to) {
		if (!from.isEmpty() && !to.isEmpty()) {
			return from.getItem().canContinueUsing(from, to);
		}
		return false;
	}

	public static LivingKnockBackEvent onLivingKnockBack(LivingEntity target, float strength, double ratioX, double ratioZ) {
		return new LivingKnockBackEvent(ForgeEventFactory.onLivingKnockBack(target, strength, ratioX, ratioZ));
	}

	public static boolean onTravelToDimension(Entity entity, ResourceKey<Level> dimension) {
		return ForgeEventFactory.onTravelToDimension(entity, dimension);
	}

	public static ItemEntity onPlayerTossEvent(Player player, ItemStack item, boolean includeName) {
		return ForgeHooks.onPlayerTossEvent(player, item, includeName);
	}

	@Nullable
	public static float[] onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
		var event = ForgeEventFactory.onLivingFall(entity, distance, damageMultiplier);
		return event.isCanceled() ? null : new float[]{event.getDistance(), event.getDamageMultiplier()};
	}

	public static boolean onLivingUseTotem(LivingEntity entity, DamageSource damageSource, ItemStack totem, InteractionHand hand) {
		LivingUseTotemEvent event = new LivingUseTotemEvent(entity, damageSource, totem, hand);
		return !NeoForge.EVENT_BUS.post(event).isCanceled();
	}

	public static float onLivingDamagePre(LivingEntity entity, DamageContainer container) {
		LivingDamageEvent.Pre event = new LivingDamageEvent.Pre(entity, container);
		NeoForge.EVENT_BUS.post(event);
		return event.getNewDamage();
	}

	public static void onLivingDamagePost(LivingEntity entity, DamageContainer container) {
		NeoForge.EVENT_BUS.post(new LivingDamageEvent.Post(entity, container));
	}

	public static void onArmorHurt(DamageSource source, EquipmentSlot[] slots, float damage, LivingEntity armoredEntity) {
		EnumMap<EquipmentSlot, ArmorHurtEvent.ArmorEntry> armorMap = new EnumMap<>(EquipmentSlot.class);
		for (EquipmentSlot slot : slots) {
			ItemStack armorPiece = armoredEntity.getItemBySlot(slot);
			if (armorPiece.isEmpty()) {
				continue;
			}
			float damageAfterChecks = armorPiece.getItem() instanceof ArmorItem && armorPiece.canBeHurtBy(source) ? damage : 0;
			armorMap.put(slot, new ArmorHurtEvent.ArmorEntry(armorPiece, damageAfterChecks));
		}

		ArmorHurtEvent event = new ArmorHurtEvent(armorMap, armoredEntity, source);
		NeoForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return;
		}
		event.getArmorMap().forEach((slot, entry) -> entry.armorItemStack.hurtAndBreak((int) entry.newDamage, armoredEntity, slot));
	}

	public static boolean onLivingDeath(LivingEntity entity, DamageSource src) {
		return NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
	}

	public static boolean onLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, boolean recentlyHit) {
		return NeoForge.EVENT_BUS.post(new LivingDropsEvent(entity, source, drops, recentlyHit)).isCanceled();
	}

	public static InteractionResult onPlaceItemIntoWorld(UseOnContext context) {
		return ForgeHooks.onPlaceItemIntoWorld(context);
	}

	public static boolean onPlayerAttackTarget(Player player, Entity target) {
		return ForgeHooks.onPlayerAttackTarget(player, target);
	}

	public static InteractionResult onItemRightClick(Player player, InteractionHand hand) {
		return ForgeHooks.onItemRightClick(player, hand);
	}

	@Nullable
	public static InteractionResult onInteractEntityAt(Player player, Entity entity, HitResult ray, InteractionHand hand) {
		Vec3 vec3d = ray.getLocation().subtract(entity.position());
		return onInteractEntityAt(player, entity, vec3d, hand);
	}

	@Nullable
	public static InteractionResult onInteractEntityAt(Player player, Entity entity, Vec3 vec3d, InteractionHand hand) {
		var forgeEvent = ForgeEventFactory.onEntityInteractSpecific(player, entity, hand, vec3d);
		var event = new PlayerInteractEvent.EntityInteractSpecific(forgeEvent);
		return event.isCanceled() ? event.getCancellationResult() : null;
	}

	@Nullable
	public static InteractionResult onInteractEntity(Player player, Entity entity, InteractionHand hand) {
		var forgeEvent = ForgeEventFactory.onEntityInteract(player, entity, hand);
		var event = new PlayerInteractEvent.EntityInteract(forgeEvent);
		return event.isCanceled() ? event.getCancellationResult() : null;
	}

	public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction face, ServerboundPlayerActionPacket.Action action) {
		return new PlayerInteractEvent.LeftClickBlock(ForgeEventFactory.onLeftClickBlock(player, pos, face, action));
	}

	public static PlayerInteractEvent.LeftClickBlock onClientMineHold(Player player, BlockPos pos, Direction face) {
		return new PlayerInteractEvent.LeftClickBlock(ForgeEventFactory.onLeftClickBlockHold(player, pos, face));
	}

	public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
		return new PlayerInteractEvent.RightClickBlock(ForgeEventFactory.onRightClickBlock(player, hand, pos, hitVec));
	}

	public static void onEmptyClick(Player player, InteractionHand hand) {
		ForgeEventFactory.onRightClickEmpty(player, hand);
	}

	public static void onEmptyLeftClick(Player player) {
		ForgeEventFactory.onLeftClickEmpty(player);
	}

	public static GameType onChangeGameType(Player player, GameType currentGameType, GameType newGameType) {
		return ForgeHooks.onChangeGameType(player, currentGameType, newGameType);
	}

	public static CriticalHitEvent fireCriticalHit(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
		CriticalHitEvent event = new CriticalHitEvent(player, target, damageModifier, vanillaCritical);
		NeoForge.EVENT_BUS.post(event);
		return event;
	}

	public static ItemAttributeModifiers computeModifiedAttributes(ItemStack stack, ItemAttributeModifiers defaultModifiers) {
		ItemAttributeModifierEvent event = new ItemAttributeModifierEvent(stack, defaultModifiers);
		NeoForge.EVENT_BUS.post(event);
		return event.build();
	}

	public static ItemStack getProjectile(LivingEntity entity, ItemStack projectileWeaponItem, ItemStack projectile) {
		LivingGetProjectileEvent event = new LivingGetProjectileEvent(entity, projectileWeaponItem, projectile);
		NeoForge.EVENT_BUS.post(event);
		return event.getProjectileItemStack();
	}

	@Nullable
	public static String getDefaultCreatorModId(ItemStack itemStack) {
		return ForgeHooks.getDefaultCreatorModId(itemStack);
	}

	public static FluidType getVanillaFluidType(net.minecraft.world.level.material.Fluid fluid) {
		net.minecraftforge.fluids.FluidType forgeType = ForgeHooks.getVanillaFluidType(fluid);
		return forgeType instanceof FluidType neoType ? neoType : null;
	}

	public static boolean onFarmlandTrample(Level level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
		return ForgeHooks.onFarmlandTrample(level, pos, state, fallDistance, entity);
	}

	public static int onNoteChange(Level level, BlockPos pos, BlockState state, int oldNote, int newNote) {
		return ForgeHooks.onNoteChange(level, pos, state, oldNote, newNote);
	}

	public static EntityDataSerializer<?> getSerializer(int id, CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> vanilla) {
		return ForgeHooks.getSerializer(id, vanilla);
	}

	public static int getSerializerId(EntityDataSerializer<?> serializer, CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> vanilla) {
		return ForgeHooks.getSerializerId(serializer, vanilla);
	}

	public static boolean canEntityDestroy(Level level, BlockPos pos, LivingEntity entity) {
		return ForgeHooks.canEntityDestroy(level, pos, entity);
	}

	public static LivingShieldBlockEvent onDamageBlock(LivingEntity blocker, DamageContainer container, boolean originalBlocked) {
		LivingShieldBlockEvent event = new LivingShieldBlockEvent(blocker, container, originalBlocked);
		NeoForge.EVENT_BUS.post(event);
		return event;
	}

	public static ObjectArrayList<ItemStack> modifyLoot(ResourceLocation lootTableId, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		return ForgeHooks.modifyLoot(lootTableId, generatedLoot, context);
	}

	public static void writeAdditionalLevelSaveData(WorldData worldData, CompoundTag levelTag) {
		ForgeHooks.writeAdditionalLevelSaveData(worldData, levelTag);
	}

	public static void readAdditionalLevelSaveData(LevelStorageSource.LevelStorageAccess access, LevelStorageSource.LevelDirectory levelDirectory) {
		ForgeHooks.readAdditionalLevelSaveData(access, levelDirectory);
	}

	public static String encodeLifecycle(Lifecycle lifecycle) {
		return ForgeHooks.encodeLifecycle(lifecycle);
	}

	public static Lifecycle parseLifecycle(String lifecycle) {
		return ForgeHooks.parseLifecycle(lifecycle);
	}

	public static boolean canUseEntitySelectors(SharedSuggestionProvider provider) {
		return ForgeHooks.canUseEntitySelectors(provider);
	}

	public static <T> HolderLookup.RegistryLookup<T> wrapRegistryLookup(HolderLookup.RegistryLookup<T> lookup) {
		return ForgeHooks.wrapRegistryLookup(lookup);
	}

	public static void handleBlockDrops(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, List<ItemEntity> drops, @Nullable Entity breaker, ItemStack tool) {
		BlockDropsEvent event = new BlockDropsEvent(level, pos, state, blockEntity, drops, breaker, tool);
		NeoForge.EVENT_BUS.post(event);
		if (!event.isCanceled()) {
			for (ItemEntity entity : event.getDrops()) {
				level.addFreshEntity(entity);
			}
			state.spawnAfterBreak(level, pos, tool, true);
			if (event.getDroppedExperience() > 0) {
				state.getBlock().popExperience(level, pos, event.getDroppedExperience());
			}
		}
	}

	// ====== Anvil / Grindstone / Container ======

	public static boolean onAnvilChange(AnvilMenu container, ItemStack left, ItemStack right, Container outputSlot, String name, long baseCost, Player player) {
		return ForgeHooks.onAnvilChange(container, left, right, outputSlot, name, baseCost, player);
	}

	public static void onAnvilRepair(Player player, ItemStack output, ItemStack left, ItemStack right) {
		ForgeEventFactory.onAnvilRepair(player, output, left, right);
	}

	public static boolean onGrindstoneChange(ItemStack top, ItemStack bottom, Container outputSlot, int xp) {
		var event = ForgeEventFactory.onGrindstoneChange(top, bottom, outputSlot, xp);
		return event.isCanceled();
	}

	public static boolean onGrindstoneTake(Container inputSlots, ContainerLevelAccess access, Function<Level, Integer> xpFunction) {
		return ForgeHooks.onGrindstoneTake(inputSlots, access, xpFunction);
	}

	public static void setCraftingPlayer(Player player) {
		ForgeHooks.setCraftingPlayer(player);
	}

	@Nullable
	public static Player getCraftingPlayer() {
		return ForgeHooks.getCraftingPlayer();
	}

	public static ItemStack getCraftingRemainingItem(ItemStack stack) {
		return ForgeHooks.getCraftingRemainingItem(stack);
	}

	public static boolean onItemStackedOn(ItemStack carriedItem, ItemStack stackedOnItem, Slot slot, ClickAction action, Player player, SlotAccess carriedSlotAccess) {
		return ForgeEventFactory.onItemStackedOn(carriedItem, stackedOnItem, slot, action, player, carriedSlotAccess);
	}

	// ====== Living Entity / AI / Damage ======

	public static net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent onLivingChangeTarget(LivingEntity entity, @Nullable LivingEntity target, net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent.ILivingTargetType targetType) {
		var forgeEvent = ForgeEventFactory.onLivingChangeTargetMob(entity, target);
		return new net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent(forgeEvent);
	}

	public static double getEntityVisibilityMultiplier(LivingEntity entity, Entity lookingEntity, double originalMultiplier) {
		return ForgeHooks.getEntityVisibilityMultiplier(entity, lookingEntity, originalMultiplier);
	}

	public static Optional<BlockPos> isLivingOnLadder(BlockState state, Level level, BlockPos pos, LivingEntity entity) {
		return ForgeHooks.isLivingOnLadder(state, level, pos, entity);
	}

	public static void onLivingJump(LivingEntity entity) {
		ForgeHooks.onLivingJump(entity);
	}

	public static boolean onLivingSwapHandItems(LivingEntity entity) {
		var event = ForgeEventFactory.onLivingSwapHandItems(entity);
		return event.isCanceled();
	}

	public static void onLivingBreathe(LivingEntity entity, boolean canBreathe, int consumeAirAmount, int refillAirAmount, boolean canRefillAir) {
		ForgeEventFactory.onLivingBreathe(entity, canBreathe, consumeAirAmount, refillAirAmount, canRefillAir);
	}

	public static boolean shouldSuppressEnderManAnger(EnderMan enderMan, Player player, ItemStack mask) {
		return ForgeHooks.shouldSuppressEnderManAnger(enderMan, player, mask);
	}

	public static boolean canMobEffectBeApplied(LivingEntity entity, MobEffectInstance effectInstance) {
		var event = ForgeEventFactory.onLivingEffectCanApply(entity, effectInstance);
		return event.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY;
	}

	public static void onLivingEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
		ForgeEventFactory.onLivingEquipmentChange(entity, slot, from, to);
	}

	// ====== Block / Level ======

	public static int fireBlockBreak(Level level, GameType gameType, ServerPlayer player, BlockPos pos, BlockState state) {
		return ForgeHooks.onBlockBreakEvent(level, gameType, player, pos);
	}

	public static void onDifficultyChange(net.minecraft.world.Difficulty difficulty, net.minecraft.world.Difficulty oldDifficulty) {
		ForgeEventFactory.onDifficultyChange(difficulty, oldDifficulty);
	}

	public static boolean onVanillaGameEvent(Level level, Holder<GameEvent> vanillaEvent, Vec3 pos, GameEvent.Context context) {
		return ForgeEventFactory.onVanillaGameEvent(level, vanillaEvent, pos, context);
	}

	public static boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
		return ForgeHooks.onCropsGrowPre(level, pos, state, def);
	}

	public static void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
		ForgeHooks.onCropsGrowPost(level, pos, state);
	}

	public static void onEntityEnterSection(Entity entity, long packedOldPos, long packedNewPos) {
		ForgeEventFactory.onEntityEnterSection(entity, packedOldPos, packedNewPos);
	}

	// ====== Chat / Communication ======

	@Nullable
	public static Component onServerChatSubmittedEvent(ServerPlayer player, Component message) {
		return ForgeHooks.onServerChatSubmittedEvent(player, message);
	}

	public static Component newChatWithLinks(String string) {
		return ForgeHooks.newChatWithLinks(string);
	}

	public static Component newChatWithLinks(String string, boolean allowMissingHeader) {
		return ForgeHooks.newChatWithLinks(string, allowMissingHeader);
	}

	// ====== Registry / Data / Misc ======

	public static TagKey<net.minecraft.world.level.block.Block> getTagFromVanillaTier(Tiers tier) {
		return switch (tier) {
			case WOOD -> net.minecraft.tags.BlockTags.INCORRECT_FOR_WOODEN_TOOL;
			case STONE -> net.minecraft.tags.BlockTags.INCORRECT_FOR_STONE_TOOL;
			case IRON -> net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL;
			case DIAMOND -> net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
			case GOLD -> net.minecraft.tags.BlockTags.INCORRECT_FOR_GOLD_TOOL;
			case NETHERITE -> net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
			default -> null;
		};
	}

	public static List<String> getModDataPacks() {
		return ForgeHooks.getModPacks();
	}

	public static List<String> getModDataPacksWithVanilla() {
		return ForgeHooks.getModPacksWithVanilla();
	}

	public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAttributesView() {
		return ForgeHooks.getAttributesView();
	}

	public static void modifyAttributes() {
		ForgeHooks.modifyAttributes();
	}

	public static net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix.Conversion getStructureConversion(String originalBiome) {
		return ForgeHooks.getStructureConversion(originalBiome);
	}

	public static boolean checkStructureNamespace(String biome) {
		return ForgeHooks.checkStructureNamespace(biome);
	}

	// ====== NeoForge-only stubs (no Forge equivalent, provide safe fallbacks) ======

	public static void markComponentClassAsValid(Class<?> clazz) {
		// NeoForge component validation — no Forge equivalent; no-op
	}

	public static boolean validateComponent(@Nullable Object component) {
		// NeoForge component validation — no Forge equivalent; always valid
		return true;
	}

	@Nullable
	public static ResourceLocation prefixNamespace(ResourceLocation original) {
		// NeoForge namespace prefix helper — return as-is for Forge compat
		return original;
	}

	// ── Build Recipe Book Type ────────────────────────────

	public static java.util.Map<String, ?> buildRecipeBookTypeTagFields() {
		return java.util.Map.of();
	}

	// ── Dispense UseOnContext ─────────────────────────────

	public static net.minecraft.world.item.context.UseOnContext dispenseUseOnContext(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.core.Direction direction, net.minecraft.world.item.ItemStack stack) {
		// Create a UseOnContext for dispenser use, pointing at the target block face
		net.minecraft.world.phys.BlockHitResult hitResult = new net.minecraft.world.phys.BlockHitResult(
				net.minecraft.world.phys.Vec3.atCenterOf(pos), direction.getOpposite(), pos, false);
		// Use a FakePlayer to provide the required Player context
		if (level instanceof ServerLevel serverLevel) {
			net.neoforged.neoforge.common.util.FakePlayer fakePlayer = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(serverLevel);
			fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
			return new net.minecraft.world.item.context.UseOnContext(fakePlayer, net.minecraft.world.InteractionHand.MAIN_HAND, hitResult);
		}
		return null;
	}

	// ── Extract Lookup Provider ───────────────────────────

	@Nullable
	public static net.minecraft.core.HolderLookup.Provider extractLookupProvider(com.mojang.serialization.DynamicOps<?> ops) {
		if (ops instanceof net.minecraft.resources.RegistryOps<?> registryOps) {
			// Try multiple possible field names (mapped/unmapped/intermediary)
			for (String fieldName : new String[]{"lookupProvider", "registryInfoLookup", "f_256016_"}) {
				try {
					java.lang.reflect.Field f = net.minecraft.resources.RegistryOps.class.getDeclaredField(fieldName);
					f.setAccessible(true);
					Object val = f.get(registryOps);
					if (val instanceof net.minecraft.core.HolderLookup.Provider provider) {
						return provider;
					}
					// RegistryOps stores a RegistryInfoLookup, try to adapt it
					if (val != null) {
						// Try to get the registryAccess from the current server as fallback
						var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
						if (server != null) {
							return server.registryAccess();
						}
					}
				} catch (NoSuchFieldException ignored) {
					// continue to next field name
				} catch (Throwable ignored) {
					break;
				}
			}
		}
		// Fallback: try current server's registry access
		var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			return server.registryAccess();
		}
		return null;
	}

	// ── Sweep Attack ──────────────────────────────────────

	public static boolean fireSweepAttack(net.minecraft.world.entity.player.Player player, net.minecraft.world.entity.Entity target) {
		var event = new net.neoforged.neoforge.event.entity.player.SweepAttackEvent(player, target, true);
		NeoForge.EVENT_BUS.post(event);
		return !event.isCanceled() && event.isSweeping();
	}

	// ── Filtered Recipe Book Types ────────────────────────

	public static net.minecraft.world.inventory.RecipeBookType[] getFilteredRecipeBookTypeValues() {
		return net.minecraft.world.inventory.RecipeBookType.values();
	}

	// ── Server Chat Decorator ─────────────────────────────

	@Nullable
	public static Object getServerChatSubmittedDecorator() {
		return null; // NeoForge-specific — no Forge equivalent
	}

	// ── Mob Effect NBT ────────────────────────────────────

	@Nullable
	public static net.minecraft.world.effect.MobEffect loadMobEffect(net.minecraft.nbt.CompoundTag tag) {
		// NeoForge-specific mob effect loading
		if (tag.contains("id", 8)) {
			ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
			if (id != null) {
				return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(id);
			}
		}
		return null;
	}

	public static net.minecraft.nbt.CompoundTag saveMobEffect(net.minecraft.world.effect.MobEffect effect) {
		var tag = new net.minecraft.nbt.CompoundTag();
		var key = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
		if (key != null) {
			tag.putString("id", key.toString());
		}
		return tag;
	}

	// ── Loot Pools Codec ──────────────────────────────────

	public static com.mojang.serialization.Codec<java.util.List<net.minecraft.world.level.storage.loot.LootPool>> lootPoolsCodec() {
		return net.minecraft.world.level.storage.loot.LootPool.CODEC.listOf();
	}

	// ── Creative Tabs Check ───────────────────────────────

	public static void onCheckCreativeTabs(Object... args) {
		// NeoForge-specific creative tab checking — no-op
	}

	// ── Chunk Unload ──────────────────────────────────────

	public static void onChunkUnload(net.minecraft.world.level.chunk.ChunkAccess chunk) {
		// Notify block entities in this chunk so they can clean up caches/capabilities
		if (chunk instanceof net.minecraft.world.level.chunk.LevelChunk levelChunk) {
			for (net.minecraft.world.level.block.entity.BlockEntity be : levelChunk.getBlockEntities().values()) {
				be.onChunkUnloaded();
			}
		}
	}

	// ── Player Enchant Item ───────────────────────────────

	public static void onPlayerEnchantItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack, int cost) {
		// NeoForge fires EnchantmentLevelSetEvent — no-op on Forge
	}

	// ── Resolve Lookup ────────────────────────────────────

	@Nullable
	public static <T> Object resolveLookup(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key) {
		// Try to resolve from the current server's registry access
		var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			var reg = server.registryAccess().registry(key);
			return reg.orElse(null);
		}
		return null;
	}

	// ── Shears Harvest Block ──────────────────────────────

	public static boolean tryDispenseShearsHarvestBlock(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack stack) {
		// Try to harvest the block using Forge's IForgeShearable system
		net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
		net.minecraft.world.level.block.Block block = state.getBlock();
		if (block instanceof net.minecraftforge.common.IForgeShearable shearable) {
			if (shearable.isShearable(stack, level, pos)) {
				java.util.List<net.minecraft.world.item.ItemStack> drops = shearable.onSheared(null, stack, level, pos, 0);
				for (net.minecraft.world.item.ItemStack drop : drops) {
					net.minecraft.world.level.block.Block.popResource(level, pos, drop);
				}
				level.removeBlock(pos, false);
				stack.hurtAndBreak(1, level instanceof net.minecraft.server.level.ServerLevel sl ? sl : null, null, (item) -> {});
				return true;
			}
		}
		return false;
	}
}
