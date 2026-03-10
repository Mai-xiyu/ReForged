package net.neoforged.neoforge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/** Proxy: NeoForge EventHooks — utility for checking/posting events */
public final class EventHooks {
    private EventHooks() {}

    public static boolean doPlayerHarvestCheck(Player player, BlockState state, BlockGetter level, BlockPos pos) {
        return ForgeEventFactory.doPlayerHarvestCheck(player, state, player.hasCorrectToolForDrops(state));
    }

    public static float getBreakSpeed(Player player, BlockState state, float original, BlockPos pos) {
        return ForgeEventFactory.getBreakSpeed(player, state, original, pos);
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }

    public static int getItemBurnTime(ItemStack itemStack, int burnTime, @Nullable RecipeType<?> recipeType) {
        return ForgeEventFactory.getItemBurnTime(itemStack, burnTime, recipeType);
    }

    public static int getExperienceDrop(LivingEntity entity, Player attackingPlayer, int originalExperience) {
        return ForgeEventFactory.getExperienceDrop(entity, attackingPlayer, originalExperience);
    }

    public static Component getPlayerDisplayName(Player player, Component username) {
        return ForgeEventFactory.getPlayerDisplayName(player, username);
    }

    public static Component getPlayerTabListDisplayName(Player player) {
        return ForgeEventFactory.getPlayerTabListDisplayName(player);
    }

    public static BlockState fireFluidPlaceBlockEvent(LevelAccessor level, BlockPos pos, BlockPos liquidPos, BlockState state) {
        return ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, liquidPos, state);
    }

    public static ItemTooltipEvent onItemTooltip(ItemStack itemStack, @Nullable Player entityPlayer, List<Component> list, TooltipFlag flags, Item.TooltipContext context) {
        return new ItemTooltipEvent(ForgeEventFactory.onItemTooltip(itemStack, entityPlayer, list, flags));
    }

    /**
     * @deprecated 保持 NeoForge 签名兼容，实际仍委托给 Forge。
     */
    @Deprecated(forRemoval = true)
    public static int onItemUseStart(LivingEntity entity, ItemStack item, int duration) {
        return ForgeEventFactory.onItemUseStart(entity, item, duration);
    }

    public static int onItemUseStart(LivingEntity entity, ItemStack item, InteractionHand hand, int duration) {
        return ForgeEventFactory.onItemUseStart(entity, item, duration);
    }

    public static int onItemUseTick(LivingEntity entity, ItemStack item, int duration) {
        return ForgeEventFactory.onItemUseTick(entity, item, duration);
    }

    public static boolean onUseItemStop(LivingEntity entity, ItemStack item, int duration) {
        return ForgeEventFactory.onUseItemStop(entity, item, duration);
    }

    public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
        return ForgeEventFactory.onItemUseFinish(entity, item, duration, result);
    }

    public static void onStartEntityTracking(Entity entity, Player player) {
        ForgeEventFactory.onStartEntityTracking(entity, player);
    }

    public static void onStopEntityTracking(Entity entity, Player player) {
        ForgeEventFactory.onStopEntityTracking(entity, player);
    }

    public static void firePlayerLoadingEvent(Player player, File playerDirectory, String uuidString) {
        ForgeEventFactory.firePlayerLoadingEvent(player, playerDirectory, uuidString);
    }

    public static void firePlayerSavingEvent(Player player, File playerDirectory, String uuidString) {
        ForgeEventFactory.firePlayerSavingEvent(player, playerDirectory, uuidString);
    }

    public static void firePlayerLoadingEvent(Player player, PlayerDataStorage playerFileData, String uuidString) {
        ForgeEventFactory.firePlayerLoadingEvent(player, playerFileData, uuidString);
    }

    @Nullable
    public static BlockState onToolUse(BlockState originalState, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        return ForgeEventFactory.onToolUse(originalState, context, ToolAction.get(itemAbility.name()), simulate);
    }

    public static BonemealEvent fireBonemealEvent(@Nullable Player player, Level level, BlockPos pos, BlockState state, ItemStack stack) {
        BonemealEvent event = new BonemealEvent(player, level, pos, state, stack);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static PlayLevelSoundEvent.AtEntity onPlaySoundAtEntity(Entity entity, Holder<SoundEvent> name, SoundSource category, float volume, float pitch) {
        return new PlayLevelSoundEvent.AtEntity(ForgeEventFactory.onPlaySoundAtEntity(entity, name, category, volume, pitch));
    }

    public static PlayLevelSoundEvent.AtPosition onPlaySoundAtPosition(Level level, double x, double y, double z, Holder<SoundEvent> name, SoundSource category, float volume, float pitch) {
        return new PlayLevelSoundEvent.AtPosition(ForgeEventFactory.onPlaySoundAtPosition(level, x, y, z, name, category, volume, pitch));
    }

    public static int onItemExpire(ItemEntity entity) {
        return ForgeEventFactory.onItemExpire(entity, entity.getItem());
    }

    public static boolean canMountEntity(Entity entityMounting, Entity entityBeingMounted, boolean isMounting) {
        return ForgeEventFactory.canMountEntity(entityMounting, entityBeingMounted, isMounting);
    }

    public static void onPlayerWakeup(Player player, boolean wakeImmediately, boolean updateLevel) {
        ForgeEventFactory.onPlayerWakeup(player, wakeImmediately, updateLevel);
    }

    public static boolean onPlayerSpawnSet(Player player, ResourceKey<Level> levelKey, BlockPos pos, boolean forced) {
        return ForgeEventFactory.onPlayerSpawnSet(player, levelKey, pos, forced);
    }

    public static void onPlayerClone(Player player, Player oldPlayer, boolean wasDeath) {
        ForgeEventFactory.onPlayerClone(player, oldPlayer, wasDeath);
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return ForgeEventFactory.onExplosionStart(level, explosion);
    }

    public static boolean onCreateWorldSpawn(Level level, ServerLevelData settings) {
        return ForgeEventFactory.onCreateWorldSpawn(level, settings);
    }

    public static float onLivingHeal(LivingEntity entity, float amount) {
        return ForgeEventFactory.onLivingHeal(entity, amount);
    }

    public static boolean onPotionAttemptBrew(NonNullList<ItemStack> stacks) {
        return ForgeEventFactory.onPotionAttemptBrew(stacks);
    }

    public static void onPotionBrewed(NonNullList<ItemStack> brewingItemStacks) {
        ForgeEventFactory.onPotionBrewed(brewingItemStacks);
    }

    public static void onPlayerBrewedPotion(Player player, ItemStack stack) {
        ForgeEventFactory.onPlayerBrewedPotion(player, stack);
    }

    public static InteractionResultHolder<ItemStack> onArrowNock(ItemStack item, Level level, Player player, InteractionHand hand, boolean hasAmmo) {
        return ForgeEventFactory.onArrowNock(item, level, player, hand, hasAmmo);
    }

    public static int onArrowLoose(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo) {
        return ForgeEventFactory.onArrowLoose(stack, level, player, charge, hasAmmo);
    }

    public static boolean onLivingDeath(LivingEntity entity, DamageSource source) {
        return ForgeEventFactory.onLivingDeath(entity, source);
    }

    public static boolean onEntityStruckByLightning(Entity entity, LightningBolt bolt) {
        return ForgeEventFactory.onEntityStruckByLightning(entity, bolt);
    }

    public static boolean onAnimalTame(Animal animal, Player tamer) {
        return ForgeEventFactory.onAnimalTame(animal, tamer);
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> list, double diameter) {
        ForgeEventFactory.onExplosionDetonate(level, explosion, list, diameter);
    }

    public static int onEnchantmentLevelSet(Level level, BlockPos pos, int enchantRow, int power, ItemStack itemStack, int enchantmentLevel) {
        return ForgeEventFactory.onEnchantmentLevelSet(level, pos, enchantRow, power, itemStack, enchantmentLevel);
    }

    public static boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state) {
        return ForgeEventFactory.onEntityDestroyBlock(entity, pos, state);
    }

    public static boolean canEntityGrief(Level level, @Nullable Entity entity) {
        return ForgeEventFactory.getMobGriefingEvent(level, entity);
    }

    public static void fireChunkTicketLevelUpdated(ServerLevel level, long chunkPos, int oldTicketLevel, int newTicketLevel, @Nullable ChunkHolder chunkHolder) {
        ForgeEventFactory.fireChunkTicketLevelUpdated(level, chunkPos, oldTicketLevel, newTicketLevel, chunkHolder);
    }

    public static void fireChunkWatch(ServerPlayer player, LevelChunk chunk, ServerLevel level) {
        ForgeEventFactory.fireChunkWatch(player, chunk, level);
    }

    public static void fireChunkUnWatch(ServerPlayer player, ChunkPos chunkPos, ServerLevel level) {
        ForgeEventFactory.fireChunkUnWatch(player, chunkPos, level);
    }

    public static boolean onPistonMovePre(Level level, BlockPos pos, net.minecraft.core.Direction direction, boolean extending) {
        return ForgeEventFactory.onPistonMovePre(level, pos, direction, extending);
    }

    public static boolean onPistonMovePost(Level level, BlockPos pos, net.minecraft.core.Direction direction, boolean extending) {
        return ForgeEventFactory.onPistonMovePost(level, pos, direction, extending);
    }

    public static List<PreparableReloadListener> onResourceReload(ReloadableServerResources serverResources, HolderLookup.Provider registries, net.minecraft.core.RegistryAccess registryAccess) {
        return ForgeEventFactory.onResourceReload(serverResources, registries, registryAccess);
    }

    public static List<PreparableReloadListener> onResourceReload(ReloadableServerResources serverResources, net.minecraft.core.RegistryAccess registryAccess) {
        return ForgeEventFactory.onResourceReload(serverResources, registryAccess);
    }

    public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandBuildContext context) {
        ForgeEventFactory.onCommandRegister(dispatcher, environment, context);
    }

    public static void firePlayerChangedDimensionEvent(Player player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
        ForgeEventFactory.onPlayerChangedDimension(player, fromDim, toDim);
    }

    public static void firePlayerLoggedIn(Player player) {
        ForgeEventFactory.firePlayerLoggedIn(player);
    }

    public static void firePlayerLoggedOut(Player player) {
        ForgeEventFactory.firePlayerLoggedOut(player);
    }

    public static void firePlayerRespawnEvent(ServerPlayer player, boolean fromEndFight) {
        ForgeEventFactory.firePlayerRespawnEvent(player, fromEndFight);
    }

    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        ForgeEventFactory.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    public static void firePlayerSmeltedEvent(Player player, ItemStack smelted) {
        ForgeEventFactory.firePlayerSmeltedEvent(player, smelted);
    }

    public static boolean onPermissionChanged(GameProfile gameProfile, int newLevel, PlayerList playerList) {
        return ForgeEventFactory.onPermissionChanged(gameProfile, newLevel, playerList);
    }

    public static Vec3 getExplosionKnockback(Level level, Explosion explosion, Entity entity, Vec3 initialVelocity) {
        ExplosionKnockbackEvent event = new ExplosionKnockbackEvent(level, explosion, entity, initialVelocity);
        NeoForge.EVENT_BUS.post(event);
        return event.getKnockbackVelocity();
    }

    public static boolean onProjectileImpact(Projectile projectile, HitResult ray) {
        ProjectileImpactEvent event = new ProjectileImpactEvent(projectile, ray);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return true;
        }
        return ForgeEventFactory.onProjectileImpact(projectile, ray);
    }

    public static MobSplitEvent onMobSplit(Mob parent, List<Mob> children) {
        MobSplitEvent event = new MobSplitEvent(parent, children);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static EntityTeleportEvent.EnderEntity onEnderTeleport(LivingEntity entity, double targetX, double targetY, double targetZ) {
        return new EntityTeleportEvent.EnderEntity(ForgeEventFactory.onEnderTeleport(entity, targetX, targetY, targetZ));
    }

    public static EntityTeleportEvent.EnderPearl onEnderPearlLand(ServerPlayer entity, double targetX, double targetY, double targetZ, ThrownEnderpearl pearlEntity, float attackDamage, HitResult hitResult) {
        return new EntityTeleportEvent.EnderPearl(ForgeEventFactory.onEnderPearlLand(entity, targetX, targetY, targetZ, pearlEntity, attackDamage, hitResult));
    }

    public static EntityTeleportEvent.ChorusFruit onChorusFruitTeleport(LivingEntity entity, double targetX, double targetY, double targetZ) {
        return new EntityTeleportEvent.ChorusFruit(ForgeEventFactory.onChorusFruitTeleport(entity, targetX, targetY, targetZ));
    }

    // ====== Block Placement / Notification ======

    public static boolean onMultiBlockPlace(@Nullable Entity entity, List<net.minecraftforge.common.util.BlockSnapshot> blockSnapshots, Direction direction) {
        return ForgeEventFactory.onMultiBlockPlace(entity, blockSnapshots, direction);
    }

    public static boolean onBlockPlace(@Nullable Entity entity, net.minecraftforge.common.util.BlockSnapshot blockSnapshot, Direction direction) {
        return ForgeEventFactory.onBlockPlace(entity, blockSnapshot, direction);
    }

    public static net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent onNeighborNotify(Level level, BlockPos pos, BlockState state, EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
        return ForgeEventFactory.onNeighborNotify(level, pos, state, notifiedSides, forceRedstoneUpdate);
    }

    // ====== Mob Spawning / Despawning ======

    public static boolean checkSpawnPlacements(EntityType<?> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, boolean defaultResult) {
        return ForgeEventFactory.checkSpawnPlacements(entityType, level, spawnType, pos, random, defaultResult);
    }

    public static boolean checkSpawnPosition(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType) {
        return ForgeEventFactory.checkSpawnPosition(mob, level, spawnType);
    }

    public static boolean checkSpawnPositionSpawner(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, SpawnData spawnData, BaseSpawner spawner) {
        return ForgeEventFactory.checkSpawnPositionSpawner(mob, level, spawnType, spawnData, spawner);
    }

    @Nullable
    public static SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        return ForgeEventFactory.onFinalizeSpawn(mob, level, difficulty, spawnType, spawnData);
    }

    public static boolean checkMobDespawn(Mob entity) {
        var result = ForgeEventFactory.canEntityDespawn(entity, (ServerLevelAccessor) entity.level());
        return result == net.minecraftforge.eventbus.api.Event.Result.ALLOW;
    }

    public static int getMaxSpawnClusterSize(Mob entity) {
        return ForgeEventFactory.getMaxSpawnPackSize(entity);
    }

    public static WeightedRandomList<MobSpawnSettings.SpawnerData> getPotentialSpawns(LevelAccessor level, MobCategory category, BlockPos pos, WeightedRandomList<MobSpawnSettings.SpawnerData> oldList) {
        return ForgeEventFactory.getPotentialSpawns(level, category, pos, oldList);
    }

    // ====== Tick Events ======

    public static boolean fireEntityTickPre(Entity entity) {
        EntityTickEvent.Pre event = new EntityTickEvent.Pre(entity);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void fireEntityTickPost(Entity entity) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(entity));
    }

    public static void firePlayerTickPre(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
    }

    public static void firePlayerTickPost(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
    }

    public static void fireLevelTickPre(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(level, haveTime));
    }

    public static void fireLevelTickPost(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(level, haveTime));
    }

    public static void fireServerTickPre(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerTickEvent.Pre(server, haveTime));
    }

    public static void fireServerTickPost(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerTickEvent.Post(server, haveTime));
    }

    // ====== Player Events ======

    public static int fireItemPickupPre(ItemEntity itemEntity, Player player) {
        return ForgeEventFactory.onItemPickup(itemEntity, player);
    }

    public static void fireItemPickupPost(ItemEntity itemEntity, Player player, ItemStack copy) {
        ForgeEventFactory.firePlayerItemPickupEvent(player, itemEntity, copy);
    }

    public static void onPlayerFall(Player player, float distance, float multiplier) {
        ForgeEventFactory.onPlayerFall(player, distance, multiplier);
    }

    public static void onAdvancementEarnedEvent(Player player, net.minecraft.advancements.AdvancementHolder advancementHolder) {
        ForgeEventFactory.onAdvancementEarned(player, advancementHolder);
    }

    public static void onAdvancementProgressedEvent(Player player, net.minecraft.advancements.AdvancementHolder advancementHolder, net.minecraft.advancements.AdvancementProgress advancementProgress, String criterion, boolean grant) {
        if (grant) {
            ForgeEventFactory.onAdvancementGrant(player, advancementHolder, advancementProgress, criterion);
        } else {
            ForgeEventFactory.onAdvancementRevoke(player, advancementHolder, advancementProgress, criterion);
        }
    }

    // ====== World / Level ======

    public static long onSleepFinished(ServerLevel level, long newTime, long minTime) {
        return ForgeEventFactory.onSleepFinished(level, newTime, minTime);
    }

    public static boolean canCreateFluidSource(Level level, BlockPos pos, BlockState state) {
        return ForgeEventFactory.canCreateFluidSource(level, pos, state, true);
    }

    public static Optional<PortalShape> onTrySpawnPortal(LevelAccessor level, BlockPos pos, Optional<PortalShape> shape) {
        return ForgeEventFactory.onTrySpawnPortal(level, pos, shape);
    }

    public static BlockGrowFeatureEvent fireBlockGrowFeature(LevelAccessor level, RandomSource randomSource, BlockPos pos, @Nullable Holder<ConfiguredFeature<?, ?>> holder) {
        var forgeEvent = ForgeEventFactory.blockGrowFeature(level, randomSource, pos, holder);
        return new BlockGrowFeatureEvent(forgeEvent);
    }

    // ====== Entity / Effects ======

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, java.util.function.Consumer<Integer> timer) {
        return ForgeEventFactory.canLivingConvert(entity, outcome, timer);
    }

    public static void onLivingConvert(LivingEntity original, LivingEntity result) {
        ForgeEventFactory.onLivingConvert(original, result);
    }

    public static EntityTeleportEvent.TeleportCommand onEntityTeleportCommand(Entity entity, double targetX, double targetY, double targetZ) {
        return new EntityTeleportEvent.TeleportCommand(ForgeEventFactory.onEntityTeleportCommand(entity, targetX, targetY, targetZ));
    }

    public static EntityTeleportEvent.SpreadPlayersCommand onEntityTeleportSpreadPlayersCommand(Entity entity, double targetX, double targetY, double targetZ) {
        return new EntityTeleportEvent.SpreadPlayersCommand(ForgeEventFactory.onEntityTeleportSpreadPlayersCommand(entity, targetX, targetY, targetZ));
    }

    public static boolean onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance) {
        return ForgeEventFactory.onLivingEffectRemove(entity, effectInstance);
    }

    public static boolean onEffectRemoved(LivingEntity entity, Holder<MobEffect> effect) {
        // Forge takes raw MobEffect, not Holder; extract value
        return ForgeEventFactory.onLivingEffectRemove(entity, effect.value());
    }

    // ====== Loot / Creative ======

    @Nullable
    public static LootTable loadLootTable(net.minecraft.resources.ResourceLocation name, LootTable table) {
        return ForgeEventFactory.onLoadLootTable(name, table);
    }

    public static void onCreativeModeTabBuildContents(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.DisplayItemsGenerator originalGenerator, CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        ForgeHooks.onCreativeModeTabBuildContents(tab, tabKey, originalGenerator, params, output);
    }

    // ====== Misc ======

    public static void onPlayerSpawnPhantoms(ServerPlayer player, ServerLevel level, BlockPos pos) {
        ForgeEventFactory.onPlayerSpawnPhantom(player, 1);
    }

    public static boolean onEntityJoinLevel(Entity entity, Level level) {
        return ForgeEventFactory.onEntityJoinLevel(entity, level);
    }

    public static boolean onEntityLeaveLevel(Entity entity, Level level) {
        return ForgeEventFactory.onEntityLeaveLevel(entity, level);
    }

    public static void onLevelLoad(Level level) {
        ForgeEventFactory.onLevelLoad(level);
    }

    public static void onLevelUnload(Level level) {
        ForgeEventFactory.onLevelUnload(level);
    }

    public static void onLevelSave(Level level) {
        ForgeEventFactory.onLevelSave(level);
    }

    public static void onTagsUpdated(RegistryAccess registryAccess, boolean fromClientPacket, boolean isIntegratedServerConnection) {
        ForgeEventFactory.onTagsUpdated(registryAccess, fromClientPacket, isIntegratedServerConnection);
    }

    // ── Sleep Events ──────────────────────────────────────

    public static boolean canEntityContinueSleeping(LivingEntity sleeper, @Nullable net.minecraft.world.entity.player.Player.BedSleepingProblem problem) {
        // NeoForge fires CanContinueSleepingEvent — delegate to Forge's SleepingLocationCheckEvent
        return problem == null; // Allow sleeping if no problem
    }

    public static com.mojang.datafixers.util.Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> canPlayerStartSleeping(ServerPlayer player, BlockPos pos, @Nullable com.mojang.datafixers.util.Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, net.minecraft.util.Unit> vanillaResult) {
        // NeoForge fires CanPlayerSleepEvent — Forge has PlayerSleepInBedEvent
        if (vanillaResult != null) return vanillaResult;
        return com.mojang.datafixers.util.Either.right(net.minecraft.util.Unit.INSTANCE);
    }

    // ── Phantom Spawning ──────────────────────────────────

    public static net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent firePlayerSpawnPhantoms(ServerPlayer player, ServerLevel level, BlockPos pos) {
        net.minecraft.world.Difficulty difficulty = level.getCurrentDifficultyAt(pos).getDifficulty();
        var event = new net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent(player, 1 + level.random.nextInt(difficulty.getId() + 1));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
        return event;
    }

    // ── Spawner Finalize ──────────────────────────────────

    @SuppressWarnings("deprecation")
    public static net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent finalizeMobSpawnSpawner(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, Object spawner, boolean def) {
        var event = new net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent(mob, level, mob.getX(), mob.getY(), mob.getZ(), difficulty, spawnType, spawnData);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled() && def) {
            mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData());
        }
        return event;
    }

    // ── Chunk Sent ────────────────────────────────────────

    public static void fireChunkSent(ServerPlayer entity, net.minecraft.world.level.chunk.LevelChunk chunk, ServerLevel level) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkWatchEvent.Sent(entity, chunk, level));
    }

    // ── Player Heart Type ─────────────────────────────────

    public static Object firePlayerHeartTypeEvent(Player player, Object heartType) {
        // NeoForge fires PlayerHeartTypeEvent — return as-is on Forge
        // HeartType is package-private in Gui, so we use Object
        return heartType;
    }

    // ── Player Respawn Position ───────────────────────────

    public static net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent firePlayerRespawnPositionEvent(ServerPlayer player, net.minecraft.world.level.portal.DimensionTransition dimensionTransition, boolean fromEndFight) {
        // Extract fields from DimensionTransition to match our 5-param constructor
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = player.level().dimension();
        net.minecraft.core.BlockPos pos = player.getRespawnPosition();
        float angle = player.getRespawnAngle();
        return new net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent(player, dim, pos, angle, fromEndFight);
    }

    // ── Enchantment Level Events ──────────────────────────

    public static int getEnchantmentLevelSpecific(int level, ItemStack stack, Holder<net.minecraft.world.item.enchantment.Enchantment> ench) {
        // NeoForge fires GetEnchantmentLevelEvent — return original level on Forge
        return level;
    }

    public static net.minecraft.world.item.enchantment.ItemEnchantments getAllEnchantmentLevels(net.minecraft.world.item.enchantment.ItemEnchantments enchantments, ItemStack stack, Object lookup) {
        // NeoForge fires GetEnchantmentLevelEvent — return as-is on Forge
        return enchantments;
    }

    // ── Custom Spawners ───────────────────────────────────

    public static java.util.List<net.minecraft.world.level.CustomSpawner> getCustomSpawners(ServerLevel serverLevel, java.util.List<net.minecraft.world.level.CustomSpawner> customSpawners) {
        // NeoForge fires ModifyCustomSpawnersEvent — return original list on Forge
        return customSpawners;
    }

    // ── Entity Size ───────────────────────────────────────

    public static net.neoforged.neoforge.event.entity.EntityEvent.Size getEntitySizeForge(Entity entity, net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions size) {
        var evt = new net.neoforged.neoforge.event.entity.EntityEvent.Size(entity);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static net.neoforged.neoforge.event.entity.EntityEvent.Size getEntitySizeForge(Entity entity, net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions oldSize, net.minecraft.world.entity.EntityDimensions newSize) {
        var evt = new net.neoforged.neoforge.event.entity.EntityEvent.Size(entity);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    // ── Alter Ground ──────────────────────────────────────

    public static Object alterGround(Object ctx, java.util.List<BlockPos> positions, Object provider) {
        // NeoForge fires AlterGroundEvent — return original provider on Forge
        return provider;
    }

    // ── Stat Award ────────────────────────────────────────

    public static net.neoforged.neoforge.event.StatAwardEvent onStatAward(Player player, net.minecraft.stats.Stat<?> stat, int value) {
        var event = new net.neoforged.neoforge.event.StatAwardEvent(player, stat, value);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
