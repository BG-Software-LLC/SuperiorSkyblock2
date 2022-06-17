package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.border.WorldBorder;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.entity.LevelEntityGetter;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.lighting.LightEngine;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.storage.WorldPersistentData;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class WorldServer extends MappedObject<net.minecraft.server.level.WorldServer> {

    public WorldServer(net.minecraft.server.level.WorldServer handle) {
        super(handle);
    }

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

    public BlockData getType(BlockPosition blockPosition) {
        return new BlockData(handle.a_(blockPosition.getHandle()));
    }

    public LevelEntityGetter<Entity> getEntities() {
        return new LevelEntityGetter<>(handle.I());
    }

    public void setTickingBlockEntity(TickingBlockEntity tickingBlockEntity) {
        handle.a(tickingBlockEntity);
    }

    public IRegistry<BiomeBase> getBiomeRegistry() {
        return handle.t().d(IRegistry.aR);
    }

    public void removeTileEntity(BlockPosition blockPosition) {
        handle.m(blockPosition.getHandle());
    }

    public ChunkProviderServer getChunkProvider() {
        return new ChunkProviderServer(handle.k());
    }

    public LightEngine getLightEngine() {
        return new LightEngine(handle.l_());
    }

    public ChunkAccess getChunkAt(int x, int z) {
        return new ChunkAccess(handle.d(x, z));
    }

    public void triggerEffect(int i, BlockPosition blockPosition, int j) {
        handle.c(i, blockPosition.getHandle(), j);
    }

    public void addEntity(com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        handle.addFreshEntity(entity.getHandle(), spawnReason);
    }

    public void addEntity(net.minecraft.world.entity.Entity entity) {
        handle.b(entity);
    }

    public boolean addEntitySerialized(com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.Entity entity) {
        return handle.c(entity.getHandle());
    }

    public WorldPersistentData getWorldPersistentData() {
        return handle.u();
    }

    public void setTypeAndData(BlockPosition blockPosition, BlockData blockData, int i) {
        handle.a(blockPosition.getHandle(), blockData.getHandle(), i);
    }

    public int getSectionIndex(int y) {
        return handle.e(y);
    }

    public int getSectionIndexFromSectionY(int y) {
        return handle.f(y);
    }

    public int getSectionYFromSectionIndex(int index) {
        return handle.g(index);
    }

    public TileEntity getTileEntity(BlockPosition blockPosition) {
        return TileEntity.ofNullable(handle.c_(blockPosition.getHandle()));
    }

    public WorldBorder getWorldBorder() {
        return new WorldBorder(handle.p_());
    }

    public ChunkAccess getChunkAtWorldCoords(BlockPosition blockPosition) {
        return new ChunkAccess(handle.l(blockPosition.getHandle()));
    }

    public void playSound(EntityHuman entityHuman, BlockPosition pos, SoundEffect sound, SoundCategory category,
                          float volume, float pitch) {
        handle.a(entityHuman, pos.getHandle(), sound, category, volume, pitch);
    }

    public GameRules getGameRules() {
        return new GameRules(handle.X());
    }

    public int getSectionsAmount() {
        return handle.ah();
    }

    public BlockPosition getHighestBlockYAt(HeightMap.Type type, BlockPosition blockPosition) {
        return new BlockPosition(handle.a(type, blockPosition.getHandle()));
    }

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

    public void gameEvent(EntityHuman entityHuman, int i, BlockPosition blockPosition, int j) {
        handle.a(entityHuman, i, blockPosition.getHandle(), j);
    }

    public Random getRandom() {
        return handle.w;
    }

    public StructureManager getStructureManager() {
        return new StructureManager(handle.a());
    }

    public EnderDragonBattle getEnderDragonBattle() {
        return handle.F();
    }

}
