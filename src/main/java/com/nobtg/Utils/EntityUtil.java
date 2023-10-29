package com.nobtg.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.nobtg.Utils.Extends.RealGui;
import com.nobtg.Utils.Extends.RealServerPlayer;
import com.nobtg.Utils.Other.JSCoreMod;
import com.nobtg.RealSwordMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public abstract class EntityUtil {
    public static float getHealth(LivingEntity living, float val) {
        if (GetterAndSetters.getRealEntities().contains(living)) return Util.getAutoCombat(living) ? val : 20.0F;
        else if (Util.IsRealDead(living)) return 0.0F;
        return val;
    }

    public static Level getLevel(Entity entity) {
        return (Level) Util.getField(entity, Entity.class, "level", "f_19853_");
    }

    public static ServerLevel getServerLevel(ServerPlayer player) {
        return (ServerLevel) getLevel(player);
    }

    @SuppressWarnings("unchecked")
    public static @NotNull PlayerList newList(PlayerList list) {
        return new PlayerList((MinecraftServer) Util.getField(list, PlayerList.class, "server", "f_11195_"), (LayeredRegistryAccess<RegistryLayer>) Util.getField(list, PlayerList.class, "registries", "f_243858_"), (PlayerDataStorage) Util.getField(list, PlayerList.class, "playerIo", "f_11204_"), (int) Util.getField(list, PlayerList.class, "maxPlayers", "f_11193_")) {
            public @NotNull ServerPlayer getPlayerForLogin(@NotNull GameProfile p_215625_) {
                return EntityUtil.getPlayerForLogin(p_215625_, this);
            }

            public @NotNull ServerPlayer respawn(@NotNull ServerPlayer old, boolean p_11238_) {
                return EntityUtil.respawn(old, p_11238_, this);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static @NotNull ServerPlayer getPlayerForLogin(@NotNull GameProfile p_215625_, PlayerList playerList) {
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(p_215625_);
        List<ServerPlayer> list = Lists.newArrayList();
        for (int i = 0; i < ((List<ServerPlayer>) Util.getField(playerList, PlayerList.class, "players", "f_11196_")).size(); ++i) {
            ServerPlayer serverplayer = ((List<ServerPlayer>) Util.getField(playerList, PlayerList.class, "players", "f_11196_")).get(i);
            if (serverplayer.getUUID().equals(uuid)) list.add(serverplayer);
        }
        ServerPlayer newPlayer = ((Map<UUID, ServerPlayer>) Util.getField(playerList, PlayerList.class, "playersByUUID", "f_11197_")).get(p_215625_.getId());
        if (newPlayer != null && !list.contains(newPlayer)) list.add(newPlayer);
        for (ServerPlayer player : list)
            player.connection.disconnect(Component.translatable("multiplayer.disconnect.duplicate_login"));
        return new RealServerPlayer(playerList.getServer(), playerList.getServer().overworld(), p_215625_);
    }

    @SuppressWarnings("unchecked")
    public static @NotNull ServerPlayer respawn(@NotNull ServerPlayer old, boolean p_11238_, PlayerList list) {
        ((List<ServerPlayer>) Util.getField(list, PlayerList.class, "players", "f_11196_")).remove(old);
        getServerLevel(old).removePlayerImmediately(old, Entity.RemovalReason.DISCARDED);
        BlockPos blockpos = old.getRespawnPosition();
        float f = old.getRespawnAngle();
        boolean flag = old.isRespawnForced();
        ServerLevel serverlevel = list.getServer().getLevel(old.getRespawnDimension());
        Optional<Vec3> optional;
        if (serverlevel != null && blockpos != null)
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, p_11238_);
        else optional = Optional.empty();
        ServerLevel ServerLevel1 = serverlevel != null && optional.isPresent() ? serverlevel : list.getServer().overworld();
        ServerPlayer serverplayer = new RealServerPlayer(list.getServer(), ServerLevel1, old.getGameProfile());
        serverplayer.connection = old.connection;
        serverplayer.restoreFrom(old, p_11238_);
        serverplayer.setId(old.getId());
        serverplayer.setMainArm(old.getMainArm());
        for (String s : old.getTags())
            serverplayer.addTag(s);
        boolean flag2 = false;
        if (optional.isPresent()) {
            BlockState blockstate = ServerLevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = optional.get();
            float f1;
            if (!blockstate.is(BlockTags.BEDS) && !flag1) f1 = f;
            else {
                Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
                f1 = (float) Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI) - 90.0D);
            }
            serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
            serverplayer.setRespawnPosition(ServerLevel1.dimension(), blockpos, f, flag, false);
            flag2 = !p_11238_ && flag1;
        } else if (blockpos != null)
            serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        while (!ServerLevel1.noCollision(serverplayer) && serverplayer.getY() < (double) ServerLevel1.getMaxBuildHeight())
            serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
        byte b0 = (byte) (p_11238_ ? 1 : 0);
        LevelData leveldata = getLevel(serverplayer).getLevelData();
        serverplayer.connection.send(new ClientboundRespawnPacket(getLevel(serverplayer).dimensionTypeId(), getLevel(serverplayer).dimension(), BiomeManager.obfuscateSeed(getServerLevel(serverplayer).getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), getLevel(serverplayer).isDebug(), getServerLevel(serverplayer).isFlat(), b0, serverplayer.getLastDeathLocation(), serverplayer.getPortalCooldown()));
        serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
        serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(ServerLevel1.getSharedSpawnPos(), ServerLevel1.getSharedSpawnAngle()));
        serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
        list.sendLevelInfo(serverplayer, ServerLevel1);
        list.sendPlayerPermissionLevel(serverplayer);
        ServerLevel1.addRespawnedPlayer(serverplayer);
        ((List<ServerPlayer>) Util.getField(list, PlayerList.class, "players", "f_11196_")).add(serverplayer);
        ((Map<UUID, ServerPlayer>) Util.getField(list, PlayerList.class, "playersByUUID", "f_11197_")).put(serverplayer.getUUID(), serverplayer);
        serverplayer.initInventoryMenu();
        serverplayer.setHealth(serverplayer.getHealth());
        ForgeEventFactory.firePlayerRespawnEvent(serverplayer, p_11238_);
        if (flag2)
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1.0F, 1.0F, ServerLevel1.getRandom().nextLong()));
        if (GetterAndSetters.getRealEntities().contains(old)) GetterAndSetters.getRealEntities().add(serverplayer);
        if (GetterAndSetters.getRemoveEntities().contains(old)) GetterAndSetters.getRemoveEntities().add(serverplayer);
        if (GetterAndSetters.getUnRealEntities().contains(old)) GetterAndSetters.getUnRealEntities().add(serverplayer);
        return serverplayer;
    }

    @SuppressWarnings("unchecked")
    public static <T extends EntityAccess> void Remove_Entity(Entity entity, boolean force) {
        try {
            if (entity instanceof Player) return;
            if (Util.getField(entity, Entity.class, "removalReason", "f_146795_") == null)
                Util.fieldSetField(entity, Entity.class, "removalReason", Entity.RemovalReason.KILLED, "f_146795_");
            if (Util.getField(entity, Entity.class, "removalReason", "f_146795_") != null && ((Entity.RemovalReason) Util.getField(entity, Entity.class, "removalReason", "f_146795_")).shouldDestroy())
                entity.stopRiding();
            entity.getPassengers().forEach(Entity::stopRiding);
            if (!force)
                ((EntityInLevelCallback) Util.getField(entity, Entity.class, "levelCallback", "f_146801_")).onRemove(Entity.RemovalReason.KILLED);
            else {
                if (getLevel(entity) instanceof ServerLevel serverLevel) {
                    EntityTickList newList = new EntityTickList();
                    ((EntityTickList) Util.getField(serverLevel, ServerLevel.class, "entityTickList", "f_143243_")).forEach(newList::add);
                    Util.fieldSetField(serverLevel, ServerLevel.class, "entityTickList", newList, "f_143243_");
                    ((EntityTickList) Util.getField(serverLevel, ServerLevel.class, "entityTickList", "f_143243_")).remove(entity);
                    serverLevel.getChunkSource().removeEntity(entity);
                    if (entity instanceof Mob mob) {
                        if ((boolean) Util.getField(serverLevel, ServerLevel.class, "isUpdatingNavigations", "f_200893_"))
                            net.minecraft.Util.logAndPauseIfInIde("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
                        Set<Mob> newList1 = new ObjectOpenHashSet<>();
                        newList1.addAll(((Set<Mob>) Util.getField(serverLevel, ServerLevel.class, "navigatingMobs", "f_143246_")));
                        Util.fieldSetField(serverLevel, ServerLevel.class, "navigatingMobs", newList1, "f_143246_");
                        ((Set<Mob>) Util.getField(serverLevel, ServerLevel.class, "navigatingMobs", "f_143246_")).remove(mob);
                    }
                    if (entity.isMultipartEntity()) for (PartEntity<?> part : entity.getParts())
                        if (part != null)
                            ((Int2ObjectMap<PartEntity<?>>) Util.getField(serverLevel, ServerLevel.class, "dragonParts", "f_143247_")).remove(part.getId());
                    entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
                    Util.fieldSetField(entity, Entity.class, "isAddedToWorld", false, "isAddedToWorld");
                    PersistentEntitySectionManager<Entity> manager = (PersistentEntitySectionManager<Entity>) Util.getField(serverLevel, ServerLevel.class, "entityManager", "f_143244_");
                    EntityLookup<T> newAccess = new EntityLookup<>();
                    ((EntityLookup<T>) Util.getField(manager, PersistentEntitySectionManager.class, "visibleEntityStorage", "f_157494_")).getAllEntities().forEach(newAccess::add);
                    Util.fieldSetField(manager, PersistentEntitySectionManager.class, "visibleEntityStorage", newAccess, "f_157494_");
                    ((EntityLookup<T>) Util.getField(manager, PersistentEntitySectionManager.class, "visibleEntityStorage", "f_157494_")).remove((T) entity);
                    serverLevel.getScoreboard().entityRemoved(entity);
                    Set<UUID> newKnownUuids = Sets.newHashSet();
                    newKnownUuids.addAll((Set<UUID>) Util.getField(manager, PersistentEntitySectionManager.class, "knownUuids", "f_157491_"));
                    Util.fieldSetField(manager, PersistentEntitySectionManager.class, "knownUuids", newKnownUuids, "f_157491_");
                    ((Set<UUID>) Util.getField(manager, PersistentEntitySectionManager.class, "knownUuids", "f_157491_")).remove(entity.getUUID());
                }
                if (Minecraft.getInstance().level != null) {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (entity.isAlwaysTicking()) {
                        EntityTickList newList = new EntityTickList();
                        ((EntityTickList) Util.getField(level, ClientLevel.class, "tickingEntities", "f_171630_")).forEach(newList::add);
                        Util.fieldSetField(level, ClientLevel.class, "tickingEntities", newList, "f_171630_");
                        ((EntityTickList) Util.getField(level, ClientLevel.class, "tickingEntities", "f_171630_")).remove(entity);
                    }
                    entity.unRide();
                    Util.fieldSetField(entity, Entity.class, "isAddedToWorld", false, "isAddedToWorld");
                    if (entity.isMultipartEntity()) for (PartEntity<?> part : entity.getParts())
                        if (part != null)
                            ((Int2ObjectMap<PartEntity<?>>) Util.getField(level, ClientLevel.class, "partEntities", "partEntities")).remove(part.getId());
                    TransientEntitySectionManager<Entity> manager = (TransientEntitySectionManager<Entity>) Util.getField(level, ClientLevel.class, "entityStorage", "f_171631_");
                    EntityLookup<T> newAccess = new EntityLookup<>();
                    ((EntityLookup<T>) Util.getField(manager, TransientEntitySectionManager.class, "entityStorage", "f_157637_")).getAllEntities().forEach(newAccess::add);
                    Util.fieldSetField(manager, TransientEntitySectionManager.class, "entityStorage", newAccess, "f_157637_");
                    ((EntityLookup<T>) Util.getField(manager, TransientEntitySectionManager.class, "entityStorage", "f_157637_")).remove((T) entity);
                }
            }
            entity.invalidateCaps();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void Attack_Entity(Player player, Entity entity, boolean un_reg) {
        if (GetterAndSetters.getRealEntities().contains(entity)) return;
        if (player.isShiftKeyDown() && un_reg) {
            GetterAndSetters.getAllUnRealEntities().add(entity.getEncodeId());
            Remove_Entity(entity, true);
        }
        if (entity instanceof LivingEntity living) {
            DamageSource source = living.getClass().getName().startsWith("net.minecraft") ? entity.damageSources().playerAttack(player) : entity.damageSources().fellOutOfWorld();
            living.hurt(source, Float.POSITIVE_INFINITY);
            GetterAndSetters.getService().schedule(() -> {
                GetterAndSetters.getUnRealEntities().add(entity);
                GetterAndSetters.getService().schedule(() -> {
                    if (getLevel(living) instanceof ServerLevel serverLevel && living.killedEntity(serverLevel, living) && living.isAlive()) {
                        living.gameEvent(GameEvent.ENTITY_DIE);
                        Util.fieldMethod(living, LivingEntity.class, "dropAllDeathLoot", new Object[]{source}, "m_6668_", DamageSource.class);
                        Util.fieldMethod(living, LivingEntity.class, "createWitherRose", new Object[]{living}, "m_21268_", LivingEntity.class);
                    }
                    if (!(entity instanceof Player)) Remove_Entity(entity, false);
                }, living.isAlive() ? 0 : 1600, TimeUnit.MILLISECONDS);
            }, living.getHealth() <= 0.0F ? 1600 : 0, TimeUnit.MILLISECONDS);
        } else {
            GetterAndSetters.getUnRealEntities().add(entity);
            Remove_Entity(entity, false);
        }
    }

    @JSCoreMod
    public static void getRealSword(@NotNull LivingEntity entity) {
        if ((entity.getMainHandItem().getItem() == RealSwordMod.REAL_SWORD.get() || entity.getOffhandItem().getItem() == RealSwordMod.REAL_SWORD.get()) && !GetterAndSetters.getRemoveEntities().contains(entity))
            GetterAndSetters.getRealEntities().add(entity);
    }

    @JSCoreMod
    public static void getRealSword(@NotNull Player player) {
        if (player.getInventory().contains(RealSwordMod.REAL_SWORD.get().getDefaultInstance()) && !GetterAndSetters.getRemoveEntities().contains(player)) {
            GetterAndSetters.getRealEntities().add(player);
            player.getAbilities().mayfly = true;
        } else if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.gameMode.getGameModeForPlayer().updatePlayerAbilities(player.getAbilities());
        if (GetterAndSetters.getRealEntities().contains(player)) {
            if (Util.getAutoCombat(player)) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                List<LivingEntity> entities = getLevel(player).getEntitiesOfClass(LivingEntity.class, new AABB(x - 10.0, y - 10.0, z - 10.0, x + 10.0, y + 10.0, z + 10.0));
                entities.remove(player);
                entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(x, y, z)));
                for (LivingEntity target : entities) {
                    if (target.isAlive() || target instanceof Slime) {
                        double angle = Math.atan2(target.getZ() - z, target.getX() - x);
                        double moveX = target.getX() - 3.3 * Math.cos(angle);
                        double moveZ = target.getZ() - 3.3 * Math.sin(angle);
                        player.moveTo(moveX, target.getY(), moveZ);
                        player.attack(target);
                    }
                }
            }
            if (Util.getAutoCombat(player))
                player.getInventory().selected = IntStream.range(0, player.getInventory().items.size()).filter(e -> player.getInventory().items.get(e).getItem() == RealSwordMod.REAL_SWORD.get().getDefaultInstance().getItem()).findFirst().orElse(-1);
            if (!(player.getTags().contains(RealSwordMod.MOD_ID + " Thread " + player.getStringUUID() + " " + player.getDisplayName()))) {
                Util.getThread(player).start();
                Util.fieldSetField(Minecraft.getInstance(), Minecraft.class, "gui", Minecraft.getInstance().player == player ? new RealGui(Minecraft.getInstance()) : Minecraft.getInstance().gui, "f_91065_");
                player.getTags().add(RealSwordMod.MOD_ID + " Thread " + player.getStringUUID() + " " + player.getDisplayName());
            }
        }
    }

    public static boolean Check(Entity entity) {
        return entity != null && GetterAndSetters.getRealEntities().contains(entity);
    }
}
