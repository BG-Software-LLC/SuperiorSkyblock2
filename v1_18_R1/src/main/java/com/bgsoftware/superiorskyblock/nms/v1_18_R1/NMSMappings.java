package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.RegistryBlocks;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.BossBattleServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.Tag;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IChunkProvider;
import net.minecraft.world.level.chunk.storage.ChunkRegionLoader;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.levelgen.GeneratorSettings;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.level.storage.WorldDataServer;
import net.minecraft.world.level.storage.WorldPersistentData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_18_R1.generator.CustomChunkGenerator;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public final class NMSMappings {

    public static IBlockData getType(ChunkSection chunkSection, int x, int y, int z) {
        return chunkSection.a(x, y, z);
    }

    public static IBlockData getType(WorldServer worldServer, BlockPosition blockPosition) {
        return worldServer.a_(blockPosition);
    }

    public static int getX(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.u();
    }

    public static int getY(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.v();
    }

    public static int getZ(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.w();
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static int getYPosition(ChunkSection chunkSection) {
        return chunkSection.g();
    }

    public static <T> boolean isTagged(Tag.e<T> tag, T value) {
        return tag.a(value);
    }

    public static <T extends Comparable<T>> T get(IBlockData blockData, IBlockState<T> blockState) {
        return blockData.c(blockState);
    }

    public static <T extends Comparable<T>, V extends T> IBlockData set(IBlockData blockData,
                                                                        IBlockState<T> blockState, V value) {
        return blockData.a(blockState, value);
    }

    public static ChunkCoordIntPair getPos(IChunkAccess chunk) {
        return chunk.f();
    }

    public static WorldServer getWorld(Chunk chunk) {
        return chunk.q;
    }

    public static int getMaxBuildHeight(WorldServer worldServer) {
        return worldServer.ag();
    }

    public static int getMinBuildHeight(WorldServer worldServer) {
        return worldServer.u_();
    }

    public static LevelEntityGetter<Entity> getEntities(WorldServer worldServer) {
        return worldServer.I();
    }

    public static AxisAlignedBB getBoundingBox(Entity entity) {
        return entity.cw();
    }

    public static void setRemoved(Entity entity, Entity.RemovalReason removalReason) {
        entity.b(removalReason);
    }

    public static ChunkSection[] getSections(IChunkAccess chunkAccess) {
        return chunkAccess.d();
    }

    public static Map<BlockPosition, TileEntity> getTileEntities(IChunkAccess chunkAccess) {
        return chunkAccess.i;

    }

    public static void setTileEntity(WorldServer worldServer, TileEntity tileEntity) {
        worldServer.a(tileEntity);
    }

    public static IRegistryCustom getCustomRegistry(WorldServer worldServer) {
        return worldServer.t();
    }

    public static void sendPacket(PlayerConnection playerConnection, Packet<?> packet) {
        playerConnection.a(packet);
    }

    public static boolean hasKeyOfType(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.b(key, type);
    }

    public static int[] getIntArray(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.n(key);
    }

    public static <T> int getId(IRegistry<T> registry, T value) {
        return registry.a(value);
    }

    public static void setIntArray(NBTTagCompound nbtTagCompound, String key, int[] value) {
        nbtTagCompound.a(key, value);
    }

    public static void removeTileEntity(WorldServer worldServer, BlockPosition blockPosition) {
        worldServer.m(blockPosition);
    }

    public static ChunkProviderServer getChunkProvider(WorldServer worldServer) {
        return worldServer.k();
    }

    public static void set(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {
        nbtTagCompound.a(key, nbtBase);
    }

    public static void buildBase(CustomChunkGenerator customChunkGenerator, RegionLimitedWorldAccess region,
                                 IChunkAccess chunk) {
        customChunkGenerator.a(region, null, chunk);
    }

    public static void setByte(NBTTagCompound nbtTagCompound, String key, byte value) {
        nbtTagCompound.a(key, value);
    }

    public static DataPaletteBlock<IBlockData> getBlocks(ChunkSection chunkSection) {
        return chunkSection.i();
    }

    public static NBTTagCompound getCompound(NBTTagList nbtTagList, int index) {
        return nbtTagList.a(index);
    }


    public static NBTTagList getList(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.c(key, type);
    }

    public static byte getByte(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.f(key);
    }

    public static LightEngine getLightEngine(WorldServer worldServer) {
        return worldServer.l_();
    }

    public static long asLong(BlockPosition blockPosition) {
        return blockPosition.a();
    }

    public static Chunk getChunkAt(IChunkProvider chunkProvider, int x, int z, boolean load) {
        return chunkProvider.a(x, z, load);
    }

    public static Random getRandom(EntityLiving entity) {
        return entity.dK();
    }

    public static DragonControllerManager getDragonControllerManager(EntityEnderDragon entityEnderDragon) {
        return entityEnderDragon.fw();
    }

    public static DragonControllerPhase<? extends IDragonController> getControllerPhase(IDragonController dragonController) {
        return dragonController.i();
    }

    public static float getXRot(Entity entity) {
        return entity.dn();
    }

    public static float getYRot(Entity entity) {
        return entity.dm();
    }

    public static void setXRot(Entity entity, float rotation) {
        entity.p(rotation);
    }

    public static BlockPosition getHighestBlockYAt(World world, HeightMap.Type type, BlockPosition blockPosition) {
        return world.a(type, blockPosition);
    }

    public static Vec3D getPositionVector(Entity entity) {
        return entity.ac();
    }

    public static WorldDataServer getWorldData(WorldServer worldServer) {
        return worldServer.N;
    }

    public static GeneratorSettings getGeneratorSettings(WorldDataServer worldDataServer) {
        return worldDataServer.A();
    }

    public static long getSeed(GeneratorSettings generatorSettings) {
        return generatorSettings.a();
    }

    public static void setBoolean(NBTTagCompound nbtTagCompound, String key, boolean value) {
        nbtTagCompound.a(key, value);
    }

    public static void setVisible(BossBattleServer bossBattleServer, boolean visible) {
        bossBattleServer.d(visible);
    }

    public static Collection<EntityPlayer> getPlayers(BossBattleServer bossBattleServer) {
        return bossBattleServer.h();
    }

    public static <T> void removeTicket(ChunkProviderServer chunkProviderServer, TicketType<T> ticketType,
                                        ChunkCoordIntPair chunkCoordIntPair, int i, T value) {
        chunkProviderServer.b(ticketType, chunkCoordIntPair, i, value);
    }

    public static <T> void addTicket(ChunkProviderServer chunkProviderServer, TicketType<T> ticketType,
                                     ChunkCoordIntPair chunkCoordIntPair, int i, T value) {
        chunkProviderServer.a(ticketType, chunkCoordIntPair, i, value);
    }

    public static Chunk getChunkAt(World world, int x, int z) {
        return world.d(x, z);
    }

    public static IChunkAccess getChunkAt(World world, int x, int z, ChunkStatus chunkStatus, boolean load) {
        return world.a(x, z, chunkStatus, load);
    }

    public static BlockPosition getPosition(TileEntity tileEntity) {
        return tileEntity.p();
    }

    public static BlockPosition getPosition(ShapeDetectorBlock shapeDetectorBlock) {
        return shapeDetectorBlock.d();
    }

    public static UUID getUniqueID(Entity entity) {
        return entity.cm();
    }

    public static void setProgress(BossBattleServer bossBattleServer, float progress) {
        bossBattleServer.a(progress);
    }

    public static int floor(double value) {
        return MathHelper.b(value);
    }

    public static void triggerEffect(GeneratorAccess generatorAccess, int i, BlockPosition blockPosition, int j) {
        generatorAccess.c(i, blockPosition, j);
    }

    public static void setTypeUpdate(World world, BlockPosition blockPosition, IBlockData blockData) {
        world.b(blockPosition, blockData);
    }

    public static IBlockData getBlockData(Block block) {
        return block.n();
    }

    public static int getSeaLevel(World world) {
        return world.m_();
    }

    public static BlockPosition down(BlockPosition blockPosition) {
        return blockPosition.a(EnumDirection.a);
    }

    public static BlockPosition up(BlockPosition blockPosition, int i) {
        return blockPosition.a(EnumDirection.b, i);
    }

    public static float getHealth(EntityLiving entityLiving) {
        return entityLiving.dZ();
    }

    public static float getMaxHealth(EntityLiving entityLiving) {
        return entityLiving.el();
    }

    public static boolean hasCustomName(Entity entity) {
        return entity.Y();
    }

    public static IChatBaseComponent getScoreboardDisplayName(Entity entity) {
        return entity.C_();
    }

    public static Entity getEntity(WorldServer worldServer, UUID uuid) {
        return worldServer.a(uuid);
    }

    public static BlockPosition getChunkCoordinates(Entity entity) {
        return entity.cW();
    }

    public static void setInvulnerable(Entity entity, boolean invulnerable) {
        entity.m(invulnerable);
    }

    public static void setBeamTarget(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition) {
        entityEnderCrystal.a(blockPosition);
    }

    public static void removePlayer(BossBattleServer bossBattleServer, EntityPlayer entityPlayer) {
        bossBattleServer.b(entityPlayer);
    }

    public static PlayerChunk.State getState(Chunk chunk) {
        return chunk.B();
    }

    public static boolean isAtLeast(PlayerChunk.State first, PlayerChunk.State second) {
        return first.a(second);
    }

    public static void addPlayer(BossBattleServer bossBattleServer, EntityPlayer entityPlayer) {
        bossBattleServer.a(entityPlayer);
    }

    public static void setControllerPhase(DragonControllerManager dragonControllerManager,
                                          DragonControllerPhase<?> dragonControllerPhase) {
        dragonControllerManager.a(dragonControllerPhase);
    }

    public static void setPositionRotation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.a(x, y, z, yaw, pitch);
    }

    public static void addEntity(WorldServer world, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        world.addFreshEntity(entity, spawnReason);
    }

    public static void addEntity(World world, Entity entity) {
        world.b(entity);
    }

    public static void addParticle(World world, ParticleParam parameters, double x, double y, double z,
                                   double velocityX, double velocityY, double velocityZ) {
        world.a(parameters, x, y, z, velocityX, velocityY, velocityZ);
    }

    public static double locX(Entity entity) {
        return getPositionVector(entity).b;
    }

    public static double locY(Entity entity) {
        return getPositionVector(entity).c;
    }

    public static double locZ(Entity entity) {
        return getPositionVector(entity).d;
    }

    public static void setHealth(EntityLiving entity, float health) {
        entity.c(health);
    }

    public static EnderDragonBattle getEnderDragonBattle(EntityEnderDragon entityEnderDragon) {
        return entityEnderDragon.fx();
    }

    public static PathEntity findPath(EntityEnderDragon entityEnderDragon, int from, int to, PathPoint pathNode) {
        return entityEnderDragon.a(from, to, pathNode);
    }

    public static boolean isInvisible(Entity entity) {
        return entity.bU();
    }

    public static Vec3D getMot(Entity entity) {
        return entity.da();
    }

    public static boolean isBreedItem(EntityAnimal entityAnimal, ItemStack itemStack) {
        return entityAnimal.n(itemStack);
    }

    public static WorldServer getWorldServer(MinecraftServer server, ResourceKey<World> resourceKey) {
        return server.a(resourceKey);
    }

    public static GameProfile getProfile(EntityHuman entityHuman) {
        return entityHuman.fp();
    }

    public static UUID getThrower(EntityItem entityItem) {
        return entityItem.j();
    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static NBTTagCompound save(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        return itemStack.b(nbtTagCompound);
    }

    public static void setTag(ItemStack itemStack, NBTTagCompound nbtTagCompound) {
        itemStack.c(nbtTagCompound);
    }

    public static void save(Entity entity, NBTTagCompound nbtTagCompound) {
        entity.f(nbtTagCompound);
    }

    public static boolean hasKey(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static void setString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static String getKey(MinecraftKey minecraftKey) {
        return minecraftKey.a();
    }

    public static boolean addEntitySerialized(WorldServer worldServer, Entity entity) {
        return worldServer.c(entity);
    }

    public static NBTTagCompound read(PlayerChunkMap playerChunkMap, ChunkCoordIntPair chunkCoordIntPair)
            throws IOException {
        return playerChunkMap.f(chunkCoordIntPair);
    }

    public static NBTTagCompound saveChunk(WorldServer worldServer, IChunkAccess chunkAccess) {
        return ChunkRegionLoader.a(worldServer, chunkAccess);
    }

    public static NBTTagCompound getChunkData(PlayerChunkMap playerChunkMap, ResourceKey<WorldDimension> resourcekey,
                                              Supplier<WorldPersistentData> supplier, NBTTagCompound nbttagcompound,
                                              ChunkCoordIntPair pos, GeneratorAccess generatoraccess) throws IOException {
        return playerChunkMap.upgradeChunkTag(resourcekey, supplier, nbttagcompound, Optional.empty(), pos, generatoraccess);
    }

    public static WorldPersistentData getWorldPersistentData(WorldServer worldServer) {
        return worldServer.u();
    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.p(key);
    }

    public static long pair(ChunkCoordIntPair chunkCoordIntPair) {
        return chunkCoordIntPair.a();
    }

    public static PlayerChunk getVisibleChunk(PlayerChunkMap playerChunkMap, long pair) {
        return playerChunkMap.n.get(pair);
    }

    public static IBlockData getType(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static int getCombinedId(IBlockData blockData) {
        return Block.i(blockData);
    }

    public static IBlockData getByCombinedId(int combinedId) {
        return Block.a(combinedId);
    }

    public static MinecraftKey getKey(RegistryBlocks<Item> registryBlocks, Item item) {
        return registryBlocks.b(item);
    }

    public static Item getItem(net.minecraft.world.item.ItemStack itemStack) {
        return itemStack.c();
    }

    public static <T extends Comparable<T>> Class<T> getType(IBlockState<T> blockState) {
        return blockState.g();
    }

    public static Material getMaterial(IBlockData blockData) {
        return blockData.c();
    }

    public static boolean isLiquid(Material material) {
        return material.a();
    }

    public static void setTypeAndData(WorldServer worldServer, BlockPosition blockPosition, IBlockData blockData, int i) {
        worldServer.a(blockPosition, blockData, i);
    }

    public static void setType(Chunk chunk, BlockPosition blockPosition, IBlockData blockData, boolean flag, boolean flag2) {
        chunk.setBlockState(blockPosition, blockData, flag, flag2);
    }

    public static int getSectionIndex(LevelHeightAccessor levelHeightAccessor, int y) {
        return levelHeightAccessor.e(y);
    }

    public static void setType(ChunkSection chunkSection, int x, int y, int z, IBlockData state, boolean lock) {
        chunkSection.a(x, y, z, state, lock);
    }

    public static void setNeedsSaving(IChunkAccess chunkAccess, boolean needsSaving) {
        chunkAccess.a(needsSaving);
    }

    public static void setInt(NBTTagCompound nbtTagCompound, String key, int value) {
        nbtTagCompound.a(key, value);
    }

    public static TileEntity getTileEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static void load(TileEntity tileEntity, NBTTagCompound nbtTagCompound) {
        tileEntity.a(nbtTagCompound);
    }

    public static String getName(IBlockState<?> blockState) {
        return blockState.f();
    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {
        return tileEntityMobSpawner.d();
    }

    public static WorldBorder getWorldBorder(World world) {
        return world.p_();
    }

    public static void setSize(WorldBorder worldBorder, double size) {
        worldBorder.a(size);
    }

    public static void setCenter(WorldBorder worldBorder, double x, double z) {
        worldBorder.c(x, z);
    }

    public static void transitionSizeBetween(WorldBorder worldBorder, double startSize, double endSize, long time) {
        worldBorder.a(startSize, endSize, time);
    }

    public static double getSize(WorldBorder worldBorder) {
        return worldBorder.i();
    }

    public static Chunk getChunkAtWorldCoords(World world, BlockPosition blockPosition) {
        return world.l(blockPosition);
    }

    public static Map<IBlockState<?>, Comparable<?>> getStateMap(IBlockData blockData) {
        return blockData.t();
    }

    public static NBTTagCompound save(TileEntity tileEntity) {
        return tileEntity.m();
    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {
        nbtTagCompound.r(key);
    }

    public static SoundEffectType getStepSound(IBlockData blockData) {
        return blockData.p();
    }

    public static SoundEffect getPlaceSound(SoundEffectType soundEffectType) {
        return soundEffectType.e();
    }

    public static float getVolume(SoundEffectType soundEffectType) {
        return soundEffectType.a();
    }

    public static float getPitch(SoundEffectType soundEffectType) {
        return soundEffectType.b();
    }

    public static void playSound(World world, EntityHuman player, BlockPosition pos, SoundEffect sound,
                                 SoundCategory category, float volume, float pitch) {
        world.a(player, pos, sound, category, volume, pitch);
    }

}
