package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeStorage;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertySlabType;
import net.minecraft.server.v1_16_R3.BlockStepAbstract;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.LightEngineThreaded;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.util.UnsafeList;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "h");
    private static final ReflectField<Collection<Entity>[]> ENTITY_SLICE_ARRAY = new ReflectField<>(
            Chunk.class, null, "entitySlices");

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        KeyBlocksCache.cacheAllBlocks();
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(Chunk chunk) {
                ChunkCoordIntPair chunkCoords = chunk.getPos();
                BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

                if (biomeBases == null)
                    throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.x + "," + chunkCoords.z + ").");

                IRegistry<BiomeBase> biomeBaseRegistry = chunk.world.r().b(IRegistry.ay);
                BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

                Arrays.fill(biomeBases, biomeBase);
                chunk.markDirty();

                LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) chunk.world.e();

                PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
                //noinspection deprecation
                PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

                PacketPlayOutLightUpdate lightUpdatePacket = new PacketPlayOutLightUpdate(chunkCoords, lightEngineThreaded, true);

                playersToUpdate.forEach(player -> {
                    PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                    playerConnection.sendPacket(unloadChunkPacket);
                    playerConnection.sendPacket(lightUpdatePacket);
                    playerConnection.sendPacket(mapChunkPacket);
                });
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, NBTTagCompound unloadedChunk) {
                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                IRegistry<BiomeBase> biomeBaseRegistry = worldServer.r().b(IRegistry.ay);
                BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

                int[] biomes = unloadedChunk.hasKeyOfType("Biomes", 11) ? unloadedChunk.getIntArray("Biomes") : new int[256];
                Arrays.fill(biomes, biomeBaseRegistry.a(biomeBase));
                unloadedChunk.setIntArray("Biomes", biomes);
            }

            @Override
            public void onFinish() {
                // Do nothing.
            }
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> island.markChunkEmpty(chunkPosition.getWorld(),
                chunkPosition.getX(), chunkPosition.getZ(), false));

        NMSUtils.runActionOnChunks(chunkPositions, true, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(Chunk chunk) {
                Arrays.fill(chunk.getSections(), Chunk.a);
                removeEntities(chunk);

                new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
                chunk.tileEntities.clear();

                removeBlocks(chunk);
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, NBTTagCompound unloadedChunk) {
                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                NBTTagList sectionsList = new NBTTagList();
                NBTTagList tileEntities = new NBTTagList();

                unloadedChunk.set("Sections", sectionsList);
                unloadedChunk.set("TileEntities", tileEntities);
                unloadedChunk.set("Entities", new NBTTagList());

                if (!(worldServer.generator instanceof IslandsGenerator)) {
                    ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());

                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);

                    try {
                        CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer,
                                worldServer.getChunkProvider().chunkGenerator, worldServer.generator);
                        customChunkGenerator.buildBase(null, protoChunk);
                    } catch (Exception ignored) {
                    }

                    ChunkSection[] chunkSections = protoChunk.getSections();

                    for (int i = -1; i < 17; ++i) {
                        int chunkSectionIndex = i;
                        ChunkSection chunkSection = Arrays.stream(chunkSections).filter(_chunkPosition ->
                                        _chunkPosition != null && _chunkPosition.getYPosition() >> 4 == chunkSectionIndex)
                                .findFirst().orElse(Chunk.a);

                        if (chunkSection != Chunk.a) {
                            NBTTagCompound sectionCompound = new NBTTagCompound();
                            sectionCompound.setByte("Y", (byte) (i & 255));
                            chunkSection.getBlocks().a(sectionCompound, "Palette", "BlockStates");
                            sectionsList.add(sectionCompound);
                        }
                    }

                    for (BlockPosition tilePosition : protoChunk.c()) {
                        NBTTagCompound tileCompound = protoChunk.i(tilePosition);
                        if (tileCompound != null)
                            tileEntities.add(tileCompound);
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
                                                                    Map<ChunkPosition, CalculatedChunk> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkPosition> chunkPositionsToCalculate = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.get(chunkPosition);
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

        NMSUtils.runActionOnChunks(chunkPositionsToCalculate, false, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(Chunk chunk) {
                ChunkPosition chunkPosition = ChunkPosition.of(chunk.getWorld().getWorld(), chunk.getPos().x, chunk.getPos().z);
                allCalculatedChunks.add(calculateChunk(chunkPosition, chunk.getSections()));
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, NBTTagCompound unloadedChunk) {
                NBTTagList sectionsList = unloadedChunk.getList("Sections", 10);
                ChunkSection[] chunkSections = new ChunkSection[sectionsList.size()];

                for (int i = 0; i < sectionsList.size(); ++i) {
                    NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                    byte yPosition = sectionCompound.getByte("Y");
                    if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                        //noinspection deprecation
                        chunkSections[i] = new ChunkSection(yPosition << 4);
                        chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                    }
                }

                CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, chunkSections);
                allCalculatedChunks.add(calculatedChunk);
                unloadedChunksCache.put(chunkPosition, calculatedChunk);
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
        List<Pair<WorldServer, NBTTagList>> unloadedEntityTags = new LinkedList<>();

        NMSUtils.runActionOnChunks(chunkPositions, false, new NMSUtils.ChunkCallback() {
            @Override
            public void onLoadedChunk(Chunk chunk) {
                for (org.bukkit.entity.Entity bukkitEntity : chunk.getBukkitChunk().getEntities()) {
                    if (!BukkitEntities.canBypassEntityLimit(bukkitEntity))
                        chunkEntities.computeIfAbsent(Keys.of(bukkitEntity), i -> new Counter(0)).inc(1);
                }
            }

            @Override
            public void onUnloadedChunk(ChunkPosition chunkPosition, NBTTagCompound entityData) {
                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                unloadedEntityTags.add(new Pair<>(worldServer, entityData.getList("Entities", 10)));
            }

            @Override
            public void onFinish() {
                BukkitExecutor.sync(() -> {
                    for (Pair<WorldServer, NBTTagList> worldUnloadedEntityTagsPair : unloadedEntityTags) {
                        for (NBTBase entityTag : worldUnloadedEntityTagsPair.getValue()) {
                            EntityTypes<?> entityType = EntityTypes.a((NBTTagCompound) entityTag).orElse(null);
                            if (entityType == null)
                                continue;

                            Entity fakeEntity = EntityTypes.a((NBTTagCompound) entityTag, worldUnloadedEntityTagsPair.getKey()).orElse(null);
                            if (fakeEntity != null) {
                                fakeEntity.valid = false;
                                if (BukkitEntities.canBypassEntityLimit(fakeEntity.getBukkitEntity()))
                                    continue;
                            }

                            Key entityKey = Keys.of(Registry.ENTITY_TYPE.get(
                                    CraftNamespacedKey.fromMinecraft(EntityTypes.getName(entityType))));

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
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection -> chunkSection == null || chunkSection.c());
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProvider()
                .getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(
                    ChunkCoordIntPair.pair(chunk.getX(), chunk.getZ()));
            World world = cropsTickingTileEntity == null ? null : cropsTickingTileEntity.getWorld();
            if (cropsTickingTileEntity != null && world != null)
                world.tileEntityListTick.remove(cropsTickingTileEntity);
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier) {
        if (chunkPositions.isEmpty()) return;
        CropsTickingTileEntity.forEachChunk(chunkPositions, cropsTickingTileEntity ->
                cropsTickingTileEntity.setCropGrowthMultiplier(newCropGrowthMultiplier));
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
    public List<Location> getBlockEntities(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        List<Location> blockEntities = new LinkedList<>();

        org.bukkit.World bukkitWorld = bukkitChunk.getWorld();

        chunk.getTileEntities().keySet().forEach(blockPosition ->
                blockEntities.add(new Location(bukkitWorld, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ())));

        return blockEntities;
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, ChunkSection[] chunkSections) {
        KeyMap<Counter> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        List<Location> spawnersLocations = new LinkedList<>();

        for (ChunkSection chunkSection : chunkSections) {
            if (chunkSection != null && !chunkSection.c()) {
                for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                    IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                    Block block = blockData.getBlock();
                    if (block != Blocks.AIR) {
                        Location location = new Location(chunkPosition.getWorld(),
                                (chunkPosition.getX() << 4) + bp.getX(),
                                chunkSection.getYPosition() + bp.getY(),
                                (chunkPosition.getZ() << 4) + bp.getZ());

                        int blockAmount = 1;

                        if ((blockData.getBlock().a(TagsBlock.SLABS) || blockData.getBlock().a(TagsBlock.WOODEN_SLABS)) &&
                                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.DOUBLE) {
                            blockAmount = 2;
                            blockData = blockData.set(BlockStepAbstract.a, BlockPropertySlabType.BOTTOM);
                        }

                        Key blockKey = Keys.of(KeyBlocksCache.getBlockKey(blockData.getBlock()), location);
                        blockCounts.computeIfAbsent(blockKey, b -> new Counter(0)).inc(blockAmount);
                        if (block == Blocks.SPAWNER) {
                            spawnersLocations.add(location);
                        }
                    }
                }
            }
        }

        return new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
    }

    private static void removeEntities(Chunk chunk) {
        Collection<Entity>[] entitySlices = null;
        Function<Void, Collection<Entity>> entitySliceCreationFunction = null;

        try {
            entitySlices = chunk.entitySlices;
            entitySliceCreationFunction = v -> new UnsafeList<>();
        } catch (Throwable ex) {
            try {
                entitySlices = ENTITY_SLICE_ARRAY.get(chunk);
                entitySliceCreationFunction = v -> new net.minecraft.server.v1_16_R3.EntitySlice<>(Entity.class);
            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while removing entities from chunk ", chunk.getPos(), ":");
            }
        }

        if (entitySlices != null) {
            for (int i = 0; i < entitySlices.length; i++) {
                entitySlices[i].forEach(entity -> {
                    if (!(entity instanceof EntityHuman))
                        entity.dead = true;
                });
                entitySlices[i] = entitySliceCreationFunction.apply(null);
            }
        }
    }

    private static void removeBlocks(Chunk chunk) {
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        WorldServer worldServer = chunk.world;

        if (worldServer.generator != null && !(worldServer.generator instanceof IslandsGenerator)) {
            CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer,
                    worldServer.getChunkProvider().chunkGenerator, worldServer.generator);
            ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);
            customChunkGenerator.buildBase(null, protoChunk);

            for (int i = 0; i < 16; i++)
                chunk.getSections()[i] = protoChunk.getSections()[i];

            for (Map.Entry<BlockPosition, TileEntity> entry : protoChunk.x().entrySet())
                worldServer.setTileEntity(entry.getKey(), entry.getValue());
        }
    }

}
