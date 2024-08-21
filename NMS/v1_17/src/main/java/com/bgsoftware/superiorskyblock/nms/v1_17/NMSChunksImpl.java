package com.bgsoftware.superiorskyblock.nms.v1_17;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_17.chunks.CropsBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_17.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<Biome[]> BIOME_BASE_ARRAY = new ReflectField<>(
            ChunkBiomeContainer.class, Biome[].class, "f");
    private static final ReflectField<ChunkBiomeContainer> CHUNK_BIOME_CONTAINER = new ReflectField<>(
            LevelChunk.class, ChunkBiomeContainer.class, Modifier.PRIVATE, 1);

    private static final boolean hasStatesIterator = new ReflectMethod<>(PalettedContainer.class,
            "forEachLocation", PalettedContainer.CountConsumer.class).isValid();

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        KeyBlocksCache.cacheAllBlocks();
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, org.bukkit.block.Biome bukkitBiome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Registry<Biome> biomesRegistry = levelChunk.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
                Biome biome = CraftBlock.biomeToBiomeBase(biomesRegistry, bukkitBiome);

                ChunkPos chunkPos = levelChunk.getPos();
                Biome[] biomes = BIOME_BASE_ARRAY.get(levelChunk.getBiomes());

                if (biomes == null)
                    throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkPos.x + "," + chunkPos.z + ").");

                Arrays.fill(biomes, biome);
                levelChunk.setUnsaved(true);

                ClientboundForgetLevelChunkPacket forgetLevelChunkPacket = new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z);
                //noinspection deprecation
                ClientboundLevelChunkPacket levelChunkPacket = new ClientboundLevelChunkPacket(levelChunk);
                ClientboundLightUpdatePacket lightUpdatePacket = new ClientboundLightUpdatePacket(chunkPos,
                        levelChunk.level.getLightEngine(), null, null, true);

                playersToUpdate.forEach(player -> {
                    ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                    serverPlayer.connection.send(forgetLevelChunkPacket);
                    serverPlayer.connection.send(lightUpdatePacket);
                    serverPlayer.connection.send(levelChunkPacket);
                });
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, CompoundTag unloadedChunk) {
                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                Registry<Biome> biomesRegistry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
                Biome biome = CraftBlock.biomeToBiomeBase(biomesRegistry, bukkitBiome);

                int[] biomes = unloadedChunk.contains("Biomes", 11) ? unloadedChunk.getIntArray("Biomes") : new int[256];
                Arrays.fill(biomes, biomesRegistry.getId(biome));
                unloadedChunk.putIntArray("Biomes", biomes);
            }

            @Override
            public void onFinish() {
                // Do nothing.
            }
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, @Nullable Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> island.markChunkEmpty(chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ(), false));

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Arrays.fill(levelChunk.getSections(), LevelChunk.EMPTY_SECTION);

                removeEntities(levelChunk);

                new HashSet<>(levelChunk.getBlockEntities().keySet()).forEach(levelChunk.getLevel()::removeBlockEntity);
                levelChunk.getBlockEntities().clear();

                removeBlocks(levelChunk);
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, CompoundTag unloadedChunk) {
                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                ListTag sectionsList = new ListTag();
                ListTag tileEntities = new ListTag();

                unloadedChunk.put("Sections", sectionsList);
                unloadedChunk.put("TileEntities", tileEntities);
                unloadedChunk.put("Entities", new ListTag());

                if (!(serverLevel.generator instanceof IslandsGenerator)) {
                    ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkPos, serverLevel);

                    try {
                        CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(serverLevel,
                                serverLevel.getChunkSource().getGenerator(), serverLevel.generator);

                        WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(protoChunk),
                                ChunkStatus.SURFACE, 0);

                        customChunkGenerator.buildSurface(region, protoChunk);
                    } catch (Exception ignored) {
                    }

                    LevelLightEngine lightEngine = serverLevel.getLightEngine();
                    LevelChunkSection[] levelChunkSections = protoChunk.getSections();

                    for (int i = lightEngine.getMinLightSection(); i < lightEngine.getMaxLightSection(); ++i) {
                        for (LevelChunkSection levelChunkSection : levelChunkSections) {
                            if (levelChunkSection != LevelChunk.EMPTY_SECTION && levelChunkSection.bottomBlockY() >> 4 == i) {
                                CompoundTag sectionCompound = new CompoundTag();
                                sectionCompound.putByte("Y", (byte) (i & 255));
                                levelChunkSection.getStates().write(sectionCompound, "Palette", "BlockStates");
                                sectionsList.add(sectionCompound);
                            }
                        }
                    }

                    for (BlockPos blockEntityPos : protoChunk.getBlockEntitiesPos()) {
                        CompoundTag blockEntityCompound = protoChunk.getBlockEntityNbt(blockEntityPos);
                        if (blockEntityCompound != null)
                            tileEntities.add(blockEntityCompound);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (onFinish != null)
                    onFinish.run();
            }
        });
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                                    Synchronized<Chunk2ObjectMap<CalculatedChunk>> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkPosition> chunkPositionsToCalculate = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.readAndGet(m -> m.get(chunkPosition));
            if (cachedCalculatedChunk != null) {
                allCalculatedChunks.add(cachedCalculatedChunk);
                chunkPositionsIterator.remove();
            } else {
                chunkPositionsToCalculate.add(chunkPosition);
            }
        }

        if (chunkPositions.isEmpty())
            return CompletableFuture.completedFuture(allCalculatedChunks);

        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();

        NMSUtils.runActionOnChunks(chunkPositions, false, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                ChunkPos chunkPos = levelChunk.getPos();
                ChunkPosition chunkPosition = ChunkPosition.of(levelChunk.level.getWorld(), chunkPos.x, chunkPos.z);
                allCalculatedChunks.add(calculateChunk(chunkPosition, levelChunk.getSections()));
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, CompoundTag unloadedChunk) {
                ListTag sectionsList = unloadedChunk.getList("Sections", 10);
                LevelChunkSection[] levelChunkSections = new LevelChunkSection[sectionsList.size()];

                for (int i = 0; i < sectionsList.size(); ++i) {
                    CompoundTag sectionCompound = sectionsList.getCompound(i);
                    byte yPosition = sectionCompound.getByte("Y");
                    if (sectionCompound.contains("Palette", 9) && sectionCompound.contains("BlockStates", 12)) {
                        //noinspection deprecation
                        levelChunkSections[i] = new LevelChunkSection(yPosition);
                        levelChunkSections[i].getStates().read(sectionCompound.getList("Palette", 10),
                                sectionCompound.getLongArray("BlockStates"));
                    }
                }

                CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, levelChunkSections);
                allCalculatedChunks.add(calculatedChunk);
                unloadedChunksCache.write(m -> m.put(chunkPosition, calculatedChunk));
            }

            @Override
            public void onFinish() {
                completableFuture.complete(allCalculatedChunks);
            }
        });

        return completableFuture;
    }

    @Override
    public CompletableFuture<KeyMap<Counter>> calculateChunkEntities(Collection<ChunkPosition> chunkPositions) {
        CompletableFuture<KeyMap<Counter>> completableFuture = new CompletableFuture<>();

        KeyMap<Counter> chunkEntities = KeyMaps.createArrayMap(KeyIndicator.ENTITY_TYPE);
        List<Pair<ServerLevel, ListTag>> unloadedEntityTags = new LinkedList<>();

        NMSUtils.runActionOnEntityChunks(chunkPositions, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                for (org.bukkit.entity.Entity bukkitEntity : new CraftChunk(levelChunk).getEntities()) {
                    if (!BukkitEntities.canBypassEntityLimit(bukkitEntity))
                        chunkEntities.computeIfAbsent(Keys.of(bukkitEntity), i -> new Counter(0)).inc(1);
                }
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, CompoundTag entityData) {
                ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                unloadedEntityTags.add(new Pair<>(serverLevel, entityData.getList("Entities", 10)));
            }

            @Override
            public void onFinish() {
                BukkitExecutor.sync(() -> {
                    for (Pair<ServerLevel, ListTag> worldUnloadedEntityTagsPair : unloadedEntityTags) {
                        for (Tag entityTag : worldUnloadedEntityTagsPair.getValue()) {
                            EntityType<?> entityType = EntityType.by((CompoundTag) entityTag).orElse(null);
                            if (entityType == null)
                                continue;

                            Entity fakeEntity = EntityType.create((CompoundTag) entityTag, worldUnloadedEntityTagsPair.getKey()).orElse(null);
                            if (fakeEntity != null) {
                                fakeEntity.valid = false;
                                if (BukkitEntities.canBypassEntityLimit(fakeEntity.getBukkitEntity()))
                                    continue;
                            }

                            Key entityKey = Keys.of(org.bukkit.Registry.ENTITY_TYPE.get(
                                    CraftNamespacedKey.fromMinecraft(EntityType.getKey(entityType))));

                            chunkEntities.computeIfAbsent(entityKey, k -> new Counter(0)).inc(1);
                        }
                    }

                    completableFuture.complete(chunkEntities);
                });
            }
        });

        return completableFuture;
    }

    @Override
    public void injectChunkSections(org.bukkit.Chunk chunk) {
        // No implementation
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(levelChunk.getSections()).allMatch(chunkSection ->
                chunkSection == LevelChunk.EMPTY_SECTION || chunkSection.isEmpty());
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
        ChunkAccess chunkAccess = serverLevel.getChunkSource().getChunk(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunkAccess instanceof LevelChunk levelChunk ? levelChunk.getBukkitChunk() : null;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;


        if (stop) {
            CropsBlockEntity cropsBlockEntity = CropsBlockEntity.remove(ChunkPos.asLong(chunk.getX(), chunk.getZ()));
            if (cropsBlockEntity != null)
                cropsBlockEntity.remove();
        } else {
            LevelChunk levelChunk = ((CraftChunk) chunk).getHandle();
            CropsBlockEntity.create(island, levelChunk);
        }
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier) {
        if (chunkPositions.isEmpty()) return;
        CropsBlockEntity.forEachChunk(chunkPositions, cropsBlockEntity ->
                cropsBlockEntity.setCropGrowthMultiplier(newCropGrowthMultiplier));
    }

    @Override
    public void shutdown() {
        List<CompletableFuture<Void>> pendingTasks = NMSUtils.getPendingChunkActions();

        if (pendingTasks.isEmpty())
            return;

        Log.info("Waiting for chunk tasks to complete.");

        CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public List<Location> getBlockEntities(Chunk chunk) {
        LevelChunk levelChunk = ((CraftChunk) chunk).getHandle();
        List<Location> blockEntities = new LinkedList<>();

        World bukkitWorld = chunk.getWorld();

        levelChunk.getBlockEntities().keySet().forEach(blockPos ->
                blockEntities.add(new Location(bukkitWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ())));

        return blockEntities;
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, LevelChunkSection[] chunkSections) {
        KeyMap<Counter> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        List<Location> spawnersLocations = new LinkedList<>();

        for (LevelChunkSection levelChunkSection : chunkSections) {
            if (levelChunkSection != null && !levelChunkSection.isEmpty()) {
                if (hasStatesIterator) {
                    levelChunkSection.getStates().forEachLocation((blockState, locationKey) -> {
                        int x = locationKey & 0xF;
                        int y = (locationKey >> 8) & 0xF;
                        int z = (locationKey >> 4) & 0xF;
                        calculateChunkInternal(blockState, x, y, z, chunkPosition, levelChunkSection, blockCounts, spawnersLocations);
                    });
                } else for (BlockPos blockPos : BlockPos.betweenClosed(0, 0, 0, 15, 15, 15)) {
                    BlockState blockState = levelChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    calculateChunkInternal(blockState, blockPos.getX(), blockPos.getY(), blockPos.getZ(), chunkPosition,
                            levelChunkSection, blockCounts, spawnersLocations);
                }
            }
        }

        return new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
    }

    private static void calculateChunkInternal(BlockState blockState, int x, int y, int z, ChunkPosition chunkPosition,
                                               LevelChunkSection levelChunkSection, KeyMap<Counter> blockCounts,
                                               List<Location> spawnersLocations) {
        Block block = blockState.getBlock();

        if (block == Blocks.AIR)
            return;

        Location location = new Location(chunkPosition.getWorld(),
                (chunkPosition.getX() << 4) + x,
                levelChunkSection.bottomBlockY() + y,
                (chunkPosition.getZ() << 4) + z);

        int blockAmount = 1;

        if (NMSUtils.isDoubleBlock(block, blockState)) {
            blockAmount = 2;
            blockState = blockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }

        Key blockKey = Keys.of(KeyBlocksCache.getBlockKey(blockState.getBlock()), location);
        blockCounts.computeIfAbsent(blockKey, b -> new Counter(0)).inc(blockAmount);
        if (block == Blocks.SPAWNER) {
            spawnersLocations.add(location);
        }
    }

    private static void removeEntities(LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        ServerLevel serverLevel = levelChunk.level;

        int chunkWorldCoordX = chunkPos.x << 4;
        int chunkWorldCoordZ = chunkPos.z << 4;


        AABB chunkBounds = new AABB(chunkWorldCoordX, serverLevel.getMinBuildHeight(), chunkWorldCoordZ,
                chunkWorldCoordX + 15, serverLevel.getMaxBuildHeight(), chunkWorldCoordZ + 15);

        Iterator<Entity> chunkEntities;

        try {
            chunkEntities = levelChunk.entities.iterator();
        } catch (Throwable ex) {
            List<Entity> worldEntities = new LinkedList<>();
            serverLevel.getEntities().get(chunkBounds, worldEntities::add);
            chunkEntities = worldEntities.iterator();
        }

        while (chunkEntities.hasNext()) {
            Entity entity = chunkEntities.next();
            if (!(entity instanceof net.minecraft.world.entity.player.Player))
                entity.setRemoved(Entity.RemovalReason.DISCARDED);
        }
    }

    private static void removeBlocks(LevelChunk levelChunk) {
        ServerLevel serverLevel = levelChunk.level;

        ChunkGenerator bukkitGenerator = serverLevel.getWorld().getGenerator();

        if (bukkitGenerator != null && !(bukkitGenerator instanceof IslandsGenerator)) {
            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                    serverLevel.getChunkSource().getGenerator(),
                    bukkitGenerator);

            WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(levelChunk),
                    ChunkStatus.SURFACE, 0);

            try {
                chunkGenerator.buildSurface(region, levelChunk);
            } catch (ClassCastException error) {
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(levelChunk.getPos(), serverLevel);
                chunkGenerator.buildSurface(region, protoChunk);

                // Load chunk sections from proto chunk to the actual chunk
                for (int i = 0; i < protoChunk.getSections().length && i < levelChunk.getSections().length; ++i) {
                    levelChunk.getSections()[i] = protoChunk.getSections()[i];
                }

                // Load biomes from proto chunk to the actual chunk
                if (protoChunk.getBiomes() != null)
                    CHUNK_BIOME_CONTAINER.set(levelChunk, protoChunk.getBiomes());

                // Load tile entities from proto chunk to the actual chunk
                protoChunk.getBlockEntities().forEach(((blockPosition, tileEntity) -> levelChunk.setBlockEntity(tileEntity)));
            }
        }
    }

}
