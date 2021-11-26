package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
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
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionManager;
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
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public final class NMSMappings {

    public static IBlockData getType(ChunkSection chunkSection, int x, int y, int z) {
        return chunkSection.a(x, y, z);
    }

    public static IBlockData getType(WorldServer worldServer, BlockPosition blockPosition) {
        return chunkSection.a(x, y, z);
    }

    public static int getX(BlockPosition blockPosition) {
        return blockPosition.u();
    }

    public static int getY(BlockPosition blockPosition) {
        return blockPosition.v();
    }

    public static int getZ(BlockPosition blockPosition) {
        return blockPosition.w();
    }

    public static int getX(BaseBlockPosition baseBlockPosition) {

    }

    public static int getY(BaseBlockPosition baseBlockPosition) {

    }

    public static int getZ(BaseBlockPosition baseBlockPosition) {

    }

    public static Block getBlock(IBlockData blockData) {

    }

    public static int getYPosition(ChunkSection chunkSection) {

    }

    public static boolean isTagged(Tag.e<?> tag, Block block) {

    }

    public static <T extends Comparable<T>> T get(IBlockData blockData, IBlockState<T> blockState) {

    }

    public static <T extends Comparable<T>, V extends T> IBlockData set(IBlockData blockData,
                                                                        IBlockState<T> blockState, V value) {

    }

    public static ChunkCoordIntPair getPos(Chunk chunk) {

    }

    public static WorldServer getWorld(Chunk chunk) {

    }

    public static int getMaxBuildHeight(WorldServer worldServer) {

    }

    public static int getMinBuildHeight(WorldServer worldServer) {

    }

    public static LevelEntityGetter<Entity> getEntities(WorldServer worldServer) {

    }

    public static AxisAlignedBB getBoundingBox(Entity entity) {

    }

    public static void setRemoved(Entity entity, Entity.RemovalReason removalReason) {

    }

    public static ChunkSection[] getSections(IChunkAccess chunkAccess) {

    }

    public static Map<BlockPosition, TileEntity> getTileEntities(ProtoChunk protoChunk) {

    }

    public static Map<BlockPosition, TileEntity> getTileEntities(Chunk chunk) {

    }

    public static void setTileEntity(WorldServer worldServer, TileEntity tileEntity) {

    }

    public static IRegistryCustom getCustomRegistry(WorldServer worldServer) {

    }

    public static void sendPacket(PlayerConnection playerConnection, Packet<?> packet) {

    }

    public static boolean hasKeyOfType(NBTTagCompound nbtTagCompound, String key, int type) {

    }

    public static int[] getIntArray(NBTTagCompound nbtTagCompound, String key) {

    }

    public static <T> int getId(IRegistry<T> registry, T value) {

    }

    public static void setIntArray(NBTTagCompound nbtTagCompound, String key, int[] value) {

    }

    public static void markDirty(Chunk chunk) {

    }

    public static void removeTileEntity(WorldServer worldServer, BlockPosition blockPosition) {

    }

    public static ChunkProviderServer getChunkProvider(WorldServer worldServer) {

    }

    public static void set(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {

    }

    public static void buildBase(CustomChunkGenerator customChunkGenerator, RegionLimitedWorldAccess region,
                                 IChunkAccess chunk) {

    }

    public static void setByte(NBTTagCompound nbtTagCompound, String key, byte value) {

    }

    public static DataPaletteBlock<Block> getBlocks(ChunkSection chunkSection) {

    }

    public static NBTTagCompound getCompound(NBTTagList nbtTagList, int index) {

    }


    public static NBTTagList getList(NBTTagCompound nbtTagCompound, String key, int type) {

    }

    public static byte getByte(NBTTagCompound nbtTagCompound, String key) {

    }

    public static LightEngine getLightEngine(WorldServer worldServer) {

    }

    public static long asLong(BlockPosition blockPosition) {

    }

    public static Chunk getChunkAt(ChunkProviderServer chunkProviderServer, int x, int z, boolean load) {

    }

    public static Random getRandom(Entity entity) {

    }

    public static DragonControllerManager getDragonControllerManager(EntityEnderDragon entityEnderDragon) {

    }

    public static DragonControllerPhase<? extends IDragonController> getControllerPhase(IDragonController dragonController) {

    }

    public static float getXRot(Entity entity) {

    }

    public static float getYRot(Entity entity) {

    }

    public static void setXRot(Entity entity, float rotation) {

    }

    public static BlockPosition getHighestBlockYAt(World world, HeightMap.Type type, BlockPosition blockPosition) {

    }

    public static Vec3D getPositionVector(Entity entityEnderDragon) {

    }

    public static WorldDataServer getWorldData(WorldServer worldServer) {

    }

    public static GeneratorSettings getGeneratorSettings(WorldDataServer worldDataServer) {

    }

    public static long getSeed(GeneratorSettings generatorSettings) {

    }

    public static void setBoolean(NBTTagCompound nbtTagCompound, String key, boolean value) {

    }

    public static void setVisible(BossBattleServer bossBattleServer, boolean visible) {

    }

    public static Collection<EntityPlayer> getPlayers(BossBattleServer bossBattleServer) {

    }

    public static void removeTicket(ChunkProviderServer chunkProviderServer, TicketType<?> ticketType,
                                    ChunkCoordIntPair chunkCoordIntPair, int i, Unit unit) {

    }

    public static void addTicket(ChunkProviderServer chunkProviderServer, TicketType<?> ticketType,
                                 ChunkCoordIntPair chunkCoordIntPair, int i, Unit unit) {

    }

    public static Chunk getChunkAt(WorldServer worldServer, int x, int z) {

    }

    public static IChunkAccess getChunkAt(WorldServer worldServer, int x, int z, ChunkStatus chunkStatus, boolean load) {

    }

    public static BlockPosition getPosition(TileEntity tileEntity) {

    }

    public static BlockPosition getPosition(ShapeDetectorBlock shapeDetectorBlock) {

    }

    public static UUID getUniqueID(Entity entity) {

    }

    public static void setProgress(BossBattleServer bossBattleServer, float progress) {

    }

    public static int floor(double value) {

    }

    public static void triggerEffect(WorldServer worldServer, int i, BlockPosition blockPosition, int j) {

    }

    public static void setTypeUpdate(WorldServer worldServer, BlockPosition blockPosition, IBlockData blockData) {

    }

    public static IBlockData getBlockData(Block block) {

    }

    public static int getSeaLevel(WorldServer worldServer) {

    }

    public static BlockPosition down(BlockPosition blockPosition) {

    }

    public static BlockPosition up(BlockPosition blockPosition, int i) {

    }

    public static float getHealth(Entity entity) {

    }

    public static float getMaxHealth(Entity entity) {

    }

    public static boolean hasCustomName(Entity entity) {

    }

    public static IChatBaseComponent getScoreboardDisplayName(Entity entity) {

    }

    public static Entity getEntity(WorldServer worldServer, UUID uuid) {

    }

    public static BlockPosition getChunkCoordinates(Entity entity) {

    }

    public static void setInvulnerable(Entity entity, boolean invulnerable) {

    }

    public static void setBeamTarget(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition) {

    }

    public static void removePlayer(BossBattleServer bossBattleServer, EntityPlayer entityPlayer) {

    }

    public static PlayerChunk.State getState(Chunk chunk) {

    }

    public static boolean isAtLeast(PlayerChunk.State first, PlayerChunk.State second) {

    }

    public static void addPlayer(BossBattleServer bossBattleServer, EntityPlayer entityPlayer) {

    }

    public static void setControllerPhase(DragonControllerManager dragonControllerManager,
                                          DragonControllerPhase<?> dragonControllerPhase) {

    }

    public static void setPositionRotation(Entity entity, double x, double y, double z, float yaw, float pitch) {

    }

    public static void addEntity(World world, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {

    }

    public static void addEntity(World world, Entity entity) {

    }

    public static void addParticle(World world, ParticleParam parameters, double x, double y, double z,
                                   double velocityX, double velocityY, double velocityZ) {

    }

    public static double locX(Entity entity) {

    }

    public static double locY(Entity entity) {

    }

    public static double locZ(Entity entity) {

    }

    public static void setHealth(Entity entity, float health) {

    }

    public static EnderDragonBattle getEnderDragonBattle(EntityEnderDragon entityEnderDragon) {

    }

    public static PathEntity findPath(EntityEnderDragon entityEnderDragon, int from, int to, PathPoint pathNode) {

    }

    public static boolean isInvisible(Entity entity) {

    }

    public static Vec3D getMot(Entity entity) {

    }

    public static boolean isBreedItem(EntityAnimal entityAnimal, ItemStack itemStack) {

    }

    public static WorldServer getWorldServer(MinecraftServer server, ResourceKey<World> resourceKey) {

    }

    public static GameProfile getProfile(EntityPlayer entityPlayer) {

    }


    public static PropertyMap getProperties(GameProfile gameProfile) {

    }

    public static UUID getThrower(EntityItem entityItem) {

    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {

    }

    public static NBTTagCompound save(ItemStack itemStack, NBTTagCompound nbtTagCompound) {

    }

    public static void setTag(ItemStack itemStack, NBTTagCompound nbtTagCompound) {

    }

    public static void save(Entity entity, NBTTagCompound nbtTagCompound) {

    }

    public static boolean hasKey(NBTTagCompound nbtTagCompound, String key) {

    }

    public static void setString(NBTTagCompound nbtTagCompound, String key, String value) {

    }

    public static String getKey(MinecraftKey minecraftKey) {

    }

    public static boolean addEntitySerialized(WorldServer worldServer, Entity entity) {

    }

    public static NBTTagCompound read(PlayerChunkMap playerChunkMap, ChunkCoordIntPair chunkCoordIntPair) {

    }

    public static NBTTagCompound saveChunk(WorldServer worldServer, IChunkAccess chunkAccess) {

    }

    public static NBTTagCompound getChunkData(PlayerChunkMap playerChunkMap, ResourceKey<WorldDimension> resourcekey,
                                              Supplier<WorldPersistentData> supplier, NBTTagCompound nbttagcompound,
                                              ChunkCoordIntPair pos, GeneratorAccess generatoraccess) throws IOException {

    }

    public static WorldPersistentData getWorldPersistentData(WorldServer worldServer) {

    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {

    }

    public static long pair(ChunkCoordIntPair chunkCoordIntPair) {

    }

    public static PlayerChunk getVisibleChunk(PlayerChunkMap playerChunkMap, long pair) {

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

    }

    public static Material getMaterial(IBlockData blockData) {

    }

    public static boolean isLiquid(Material material) {

    }

    public static void setTypeAndData(WorldServer worldServer, BlockPosition blockPosition, IBlockData blockData, int i) {

    }

    public static void setType(Chunk chunk, BlockPosition blockPosition, IBlockData blockData, boolean flag, boolean flag2) {

    }

    public static int getSectionIndex(Chunk chunk, int y) {

    }

    public static void setType(ChunkSection chunkSection, int x, int y, int z, IBlockData state, boolean lock) {

    }

    public static void setNeedsSaving(Chunk chunk, boolean needsSaving) {

    }

    public static void setInt(NBTTagCompound nbtTagCompound, String key, int value) {

    }

    public static TileEntity getTileEntity(WorldServer worldServer, BlockPosition blockPosition) {

    }

    public static void load(TileEntity tileEntity, NBTTagCompound nbtTagCompound) {

    }

    public static String getName(IBlockState<?> blockState) {

    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {

    }

    public static WorldBorder getWorldBorder(WorldServer worldServer) {

    }

    public static void setSize(WorldBorder worldBorder, double size) {

    }

    public static void setCenter(WorldBorder worldBorder, double x, double z) {

    }

    public static void transitionSizeBetween(WorldBorder worldBorder, double startSize, double endSize, long time) {

    }

    public static double getSize(WorldBorder worldBorder) {

    }

    public static Chunk getChunkAtWorldCoords(WorldServer worldServer, BlockPosition blockPosition) {

    }

    public static Map<IBlockState<?>, Comparable<?>> getStateMap(IBlockData blockData) {

    }

    public static NBTTagCompound save(TileEntity tileEntity, NBTTagCompound nbtTagCompound) {

    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {

    }

    public static SoundEffectType getStepSound(IBlockData blockData) {

    }

    public static SoundEffect getPlaceSound(SoundEffectType soundEffectType) {

    }

    public static float getVolume(SoundEffectType soundEffectType) {

    }

    public static float getPitch(SoundEffectType soundEffectType) {

    }

    public static void playSound(WorldServer worldServer, EntityHuman player, BlockPosition pos, SoundEffect sound,
                                 SoundCategory category, float volume, float pitch) {

    }

}
