package net.neoforged.neoforge.common;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.ForgeHooks;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nullable;

public class CommonHooks {
	private CommonHooks() {}

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
}
