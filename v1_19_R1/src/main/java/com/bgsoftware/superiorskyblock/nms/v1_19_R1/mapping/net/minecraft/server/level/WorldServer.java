package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.util.RandomSource;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.entity.Entity;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.GameRules;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.StructureManager;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.entity.LevelEntityGetter;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.storage.WorldPersistentData;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public final class WorldServer extends MappedObject<net.minecraft.server.level.WorldServer> {

    @Remap(classPath = "net.minecraft.core.Registry", name = "BIOME_REGISTRY", type = Remap.Type.FIELD, remappedName = "aR")
    private static final ResourceKey<IRegistry<BiomeBase>> BIOME_REGISTRY = IRegistry.aR;

    public WorldServer(net.minecraft.server.level.WorldServer handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.MinecraftServer",
            name = "getLevel",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static WorldServer getWorldServer(MinecraftServer server, ResourceKey<World> resourceKey) {
        return new WorldServer(server.a(resourceKey));
    }

    @Nullable
    public static WorldServer ofNullable(@Nullable net.minecraft.server.level.WorldServer worldServer) {
        return worldServer == null ? null : new WorldServer(worldServer);
    }

    @Nullable
    public static WorldServer ofNullable(@Nullable World world) {
        return !(world instanceof net.minecraft.server.level.WorldServer) ? null :
                new WorldServer((net.minecraft.server.level.WorldServer) world);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a_")
    public BlockData getType(BlockPosition blockPosition) {
        return new BlockData(handle.a_(blockPosition.getHandle()));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getEntities",
            type = Remap.Type.METHOD,
            remappedName = "F")
    public LevelEntityGetter<net.minecraft.world.entity.Entity> getEntities() {
        return new LevelEntityGetter<>(handle.F());
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "addBlockEntityTicker",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setTickingBlockEntity(TickingBlockEntity tickingBlockEntity) {
        handle.a(tickingBlockEntity);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "registryAccess",
            type = Remap.Type.METHOD,
            remappedName = "s")
    @Remap(classPath = "net.minecraft.core.RegistryAccess",
            name = "registryOrThrow",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public IRegistry<BiomeBase> getBiomeRegistry() {
        return handle.s().d(BIOME_REGISTRY);
    }

    @Remap(classPath = "net.minecraft.core.Registry",
            name = "asHolderIdMap",
            type = Remap.Type.METHOD,
            remappedName = "s")
    public Registry<Holder<BiomeBase>> getBiomeRegistryHolder() {
        return getBiomeRegistry().s();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "removeBlockEntity",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public void removeTileEntity(BlockPosition blockPosition) {
        handle.n(blockPosition.getHandle());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "getChunkSource",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public ChunkProviderServer getChunkProvider() {
        return new ChunkProviderServer(handle.k());
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getLightEngine",
            type = Remap.Type.METHOD,
            remappedName = "l_")
    public LightEngine getLightEngine() {
        return new LightEngine(handle.l_());
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getChunk",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public ChunkAccess getChunkAt(int x, int z) {
        return new ChunkAccess(handle.d(x, z));
    }

    @Remap(classPath = "net.minecraft.world.level.LevelAccessor",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void triggerEffect(int i, BlockPosition blockPosition, int j) {
        handle.c(i, blockPosition.getHandle(), j);
    }

    public void addEntity(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        handle.addFreshEntity(entity.getHandle(), spawnReason);
    }


    @Remap(classPath = "net.minecraft.world.level.LevelWriter",
            name = "addFreshEntity",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void addEntity(net.minecraft.world.entity.Entity entity) {
        handle.b(entity);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "addWithUUID",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public boolean addEntitySerialized(Entity entity) {
        return handle.c(entity.getHandle());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "getDataStorage",
            type = Remap.Type.METHOD,
            remappedName = "t")
    public WorldPersistentData getWorldPersistentData() {
        return handle.t();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "setBlock",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setTypeAndData(BlockPosition blockPosition, BlockData blockData, int i) {
        handle.a(blockPosition.getHandle(), blockData.getHandle(), i);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelHeightAccessor",
            name = "getSectionIndex",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public int getSectionIndex(int y) {
        return handle.e(y);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelHeightAccessor",
            name = "getSectionIndexFromSectionY",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public int getSectionIndexFromSectionY(int y) {
        return handle.f(y);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelHeightAccessor",
            name = "getSectionYFromSectionIndex",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public int getSectionYFromSectionIndex(int index) {
        return handle.g(index);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockEntity",
            type = Remap.Type.METHOD,
            remappedName = "c_")
    public TileEntity getTileEntity(BlockPosition blockPosition) {
        return TileEntity.ofNullable(handle.c_(blockPosition.getHandle()));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getChunkAt",
            type = Remap.Type.METHOD,
            remappedName = "l")
    public ChunkAccess getChunkAtWorldCoords(BlockPosition blockPosition) {
        return new ChunkAccess(handle.l(blockPosition.getHandle()));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "playSound",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void playSound(EntityHuman entityHuman, BlockPosition pos, SoundEffect sound, SoundCategory category,
                          float volume, float pitch) {
        handle.a(entityHuman, pos.getHandle(), sound, category, volume, pitch);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getGameRules",
            type = Remap.Type.METHOD,
            remappedName = "W")
    public GameRules getGameRules() {
        return new GameRules(handle.W());
    }

    @Remap(classPath = "net.minecraft.world.level.LevelHeightAccessor",
            name = "getSectionsCount",
            type = Remap.Type.METHOD,
            remappedName = "ai")
    public int getSectionsAmount() {
        return handle.ai();
    }

    @Remap(classPath = "net.minecraft.world.level.CommonLevelAccessor",
            name = "getHeightmapPos",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public BlockPosition getHighestBlockYAt(HeightMap.Type type, BlockPosition blockPosition) {
        return new BlockPosition(handle.a(type, blockPosition.getHandle()));
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "serverLevelData",
            type = Remap.Type.FIELD,
            remappedName = "N")
    @Remap(classPath = "net.minecraft.world.level.storage.WorldData",
            name = "worldGenSettings",
            type = Remap.Type.METHOD,
            remappedName = "A")
    @Remap(classPath = "net.minecraft.world.level.levelgen.WorldGenSettings",
            name = "seed",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public long getSeed() {
        return handle.N.A().a();
    }

    public ChunkGenerator getBukkitGenerator() {
        return handle.generator;
    }

    public CraftWorld getWorld() {
        return handle.getWorld();
    }


    @Nullable
    public ChunkAccess getChunkIfLoaded(int chunkX, int chunkZ) {
        try {
            return ChunkAccess.ofNullable(handle.getChunkIfLoadedImmediately(chunkX, chunkZ));
        } catch (Throwable ex) {
            return ChunkAccess.ofNullable(handle.getChunkIfLoaded(chunkX, chunkZ));
        }
    }

    public ResourceKey<WorldDimension> getTypeKey() {
        return handle.getTypeKey();
    }

    @Remap(classPath = "net.minecraft.world.level.LevelAccessor",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void levelEvent(EntityHuman entityHuman, int i, BlockPosition blockPosition, int j) {
        handle.a(entityHuman, i, blockPosition.getHandle(), j);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getRandom",
            type = Remap.Type.METHOD,
            remappedName = "r_")
    public RandomSource getRandom() {
        return new RandomSource(handle.r_());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "structureManager",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public StructureManager getStructureManager() {
        return new StructureManager(handle.a());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "dragonFight",
            type = Remap.Type.METHOD,
            remappedName = "C")
    public EnderDragonBattle getEnderDragonBattle() {
        return handle.C();
    }
}
