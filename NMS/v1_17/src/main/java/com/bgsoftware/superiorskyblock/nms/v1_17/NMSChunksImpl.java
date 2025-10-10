package com.bgsoftware.superiorskyblock.nms.v1_17;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.nms.v1_17.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_17.utils.NMSUtilsVersioned;
import com.bgsoftware.superiorskyblock.nms.v1_17.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
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
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMSChunksImpl extends com.bgsoftware.superiorskyblock.nms.v1_17.AbstractNMSChunks {

    private static final ReflectField<Biome[]> BIOME_BASE_ARRAY = new ReflectField<>(
            ChunkBiomeContainer.class, Biome[].class, "f");
    private static final ReflectField<ChunkBiomeContainer> CHUNK_BIOME_CONTAINER = new ReflectField<>(
            LevelChunk.class, ChunkBiomeContainer.class, Modifier.PRIVATE, 1);

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected NMSUtils.ChunkCallback getBiomesChunkCallback(org.bukkit.block.Biome bukkitBiome, Collection<Player> playersToUpdate) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.SET_BIOME, false) {
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
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                if (!chunkCompound.contains("Level", 10))
                    return;

                CompoundTag unloadedChunk = chunkCompound.getCompound("Level");

                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();
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
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getDeleteChunkCallback(Runnable onFinish) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.DELETE_CHUNK, false) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                Arrays.fill(levelChunk.getSections(), LevelChunk.EMPTY_SECTION);

                removeEntities(levelChunk);
                removeBlockEntities(levelChunk);
                removeBlocks(levelChunk);
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                if (!chunkCompound.contains("Level", 10))
                    return;

                CompoundTag unloadedChunk = chunkCompound.getCompound("Level");

                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                ListTag sectionsList = new ListTag();
                ListTag tileEntities = new ListTag();

                unloadedChunk.put("Sections", sectionsList);
                unloadedChunk.put("TileEntities", tileEntities);
                unloadedChunk.put("Entities", new ListTag());

                if (!(serverLevel.generator instanceof IslandsGenerator)) {
                    ChunkPos chunkPos = unloadedChunkCompound.chunkPos();

                    ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkPos, serverLevel);


                    try {
                        NMSUtilsVersioned.buildSurfaceForChunk(serverLevel, serverLevel.generator, protoChunk);
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
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getCalculateChunkCallback(CompletableFuture<List<CalculatedChunk>> completableFuture,
                                                               Synchronized<Chunk2ObjectMap<CalculatedChunk>> unloadedChunksCache,
                                                               List<CalculatedChunk> allCalculatedChunks) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.BLOCKS_RECALCULATE, true) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                ChunkPos chunkPos = levelChunk.getPos();
                ChunkPosition chunkPosition = ChunkPosition.of(levelChunk.level.getWorld(), chunkPos.x, chunkPos.z, false);
                allCalculatedChunks.add(calculateChunk(chunkPosition, levelChunk.level, levelChunk.getSections()));

                latchCountDown();
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                if (!chunkCompound.contains("Level", 10))
                    return;

                ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                CompoundTag unloadedChunk = chunkCompound.getCompound("Level");

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

                ChunkPosition chunkPosition = unloadedChunkCompound.chunkPosition();

                CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, serverLevel, levelChunkSections);
                allCalculatedChunks.add(calculatedChunk);
                unloadedChunksCache.write(m -> m.put(chunkPosition, calculatedChunk));

                latchCountDown();
            }

            @Override
            public void onFinish() {
                completableFuture.complete(allCalculatedChunks);
            }
        };
    }

    @Override
    protected NMSUtils.ChunkCallback getEntitiesChunkCallback(KeyMap<Counter> chunkEntities,
                                                              List<NMSUtils.UnloadedChunkCompound> unloadedChunkCompounds,
                                                              CompletableFuture<KeyMap<Counter>> completableFuture) {
        return new NMSUtils.ChunkCallback(ChunkLoadReason.ENTITIES_RECALCULATE, true) {
            @Override
            public void onLoadedChunk(LevelChunk levelChunk) {
                for (org.bukkit.entity.Entity bukkitEntity : new CraftChunk(levelChunk).getEntities()) {
                    if (!BukkitEntities.canBypassEntityLimit(bukkitEntity))
                        chunkEntities.computeIfAbsent(Keys.of(bukkitEntity), i -> new Counter(0)).inc(1);
                }

                latchCountDown();
            }

            @Override
            public void onUnloadedChunk(NMSUtils.UnloadedChunkCompound unloadedChunkCompound) {
                CompoundTag chunkCompound = unloadedChunkCompound.chunkCompound();
                if (!chunkCompound.contains("Level", 10))
                    return;

                unloadedChunkCompounds.add(unloadedChunkCompound);

                latchCountDown();
            }

            @Override
            public void onFinish() {
                BukkitExecutor.ensureMain(() -> {
                    for (NMSUtils.UnloadedChunkCompound unloadedChunkCompound : unloadedChunkCompounds) {
                        ListTag entitiesTag = unloadedChunkCompound.chunkCompound().getCompound("Level")
                                .getList("Entities", 10);
                        ServerLevel serverLevel = unloadedChunkCompound.serverLevel();

                        for (Tag entityTag : entitiesTag) {
                            EntityType<?> entityType = EntityType.by((CompoundTag) entityTag).orElse(null);
                            if (entityType == null)
                                continue;

                            Entity fakeEntity = EntityType.create((CompoundTag) entityTag, serverLevel).orElse(null);
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
        };
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
