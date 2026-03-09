package net.neoforged.neoforge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

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
}
