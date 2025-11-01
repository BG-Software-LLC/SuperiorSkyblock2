package com.bgsoftware.superiorskyblock.nms.v1_21_9.utils;

import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_9.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_9.utils.TickingBlockList;
import com.google.common.base.Suppliers;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class NMSUtilsVersioned {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component[] COMPONENT_ARRAY_TYPE = new Component[0];

    private static final ReflectField<PersistentEntitySectionManager<Entity>> SERVER_LEVEL_ENTITY_MANAGER = new ReflectField<>(
            ServerLevel.class, PersistentEntitySectionManager.class, Modifier.PUBLIC | Modifier.FINAL, 1);
    private static final ReflectField<SimpleRegionStorage> ENTITY_STORAGE_REGION_STORAGE = new ReflectField<>(
            EntityStorage.class, SimpleRegionStorage.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    public static final PalettedContainerFactory DEFAULT_PALETTED_CONTAINER_FACTORY = PalettedContainerFactory.create(
            MinecraftServer.getServer().registryAccess());

    public static CompoundTag readChunk(ChunkMap chunkMap, ChunkPos chunkPos) {
        return chunkMap.read(chunkPos).join().orElse(null);
    }

    public static CompoundTag getChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ServerLevel serverLevel, ChunkPos chunkPos) {
        return chunkMap.upgradeChunkTag(serverLevel.getTypeKey(), Suppliers.ofInstance(serverLevel.getDataStorage()),
                chunkCompoundTag, Optional.empty(), chunkPos, serverLevel);
    }

    public static void saveChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ChunkPos chunkPos) {
        chunkMap.write(chunkPos, () -> chunkCompoundTag);
    }

    public static BukkitExecutor.NestedTask<Void> runActionOnUnloadedEntityChunks(
            Collection<ChunkPosition> chunks, NMSUtils.ChunkCallback chunkCallback, CountDownLatch countDownLatch) {
        if (SERVER_LEVEL_ENTITY_MANAGER.isValid()) {
            return BukkitExecutor.createTask().runSync(v -> {
                chunks.forEach(chunkPosition -> {
                    ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                    PersistentEntitySectionManager<Entity> entityManager = SERVER_LEVEL_ENTITY_MANAGER.get(serverLevel);
                    SimpleRegionStorage regionStorage = ENTITY_STORAGE_REGION_STORAGE.get(entityManager.permanentStorage);

                    ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                    regionStorage.read(chunkPos).whenComplete((entityDataOptional, error) -> {
                        if (error != null) {
                            countDownLatch.countDown();
                            throw new RuntimeException(error);
                        } else {
                            CompoundTag entityData = entityDataOptional.orElse(null);
                            if (entityData == null) {
                                chunkCallback.onChunkNotExist(chunkPosition);
                            } else {
                                NMSUtils.UnloadedChunkCompound unloadedChunkCompound = new NMSUtils.UnloadedChunkCompound(chunkPosition, entityData);
                                chunkCallback.onUnloadedChunk(unloadedChunkCompound);
                            }
                        }
                    });
                });
            });
        } else {
            return BukkitExecutor.createTask().runAsync(v -> {
                chunks.forEach(chunkPosition -> {
                    ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                    try {
                        MoonriseRegionFileIO.RegionDataController regionDataController =
                                serverLevel.moonrise$getEntityChunkDataController();
                        int chunkX = chunkPosition.getX();
                        int chunkZ = chunkPosition.getZ();
                        MoonriseRegionFileIO.RegionDataController.ReadData readData =
                                regionDataController.readData(chunkX, chunkZ);
                        if(readData != null) {
                            CompoundTag entityData = switch (readData.result()) {
                                case HAS_DATA -> regionDataController.finishRead(chunkX, chunkZ, readData);
                                case SYNC_READ -> readData.syncRead();
                                default -> null;
                            };
                            if (entityData != null) {
                                NMSUtils.UnloadedChunkCompound unloadedChunkCompound = new NMSUtils.UnloadedChunkCompound(chunkPosition, entityData);
                                chunkCallback.onUnloadedChunk(unloadedChunkCompound);
                                return;
                            }
                        }

                        chunkCallback.onChunkNotExist(chunkPosition);
                    } catch (IOException error) {
                        countDownLatch.countDown();
                        Log.error(error, "An unexpected error occurred while interacting with unloaded chunk ", chunkPosition, ":");
                    }
                });
            });
        }
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, ServerLevel serverLevel) {
        return new ProtoChunk(chunkPos,
                UpgradeData.EMPTY,
                serverLevel,
                DEFAULT_PALETTED_CONTAINER_FACTORY,
                null);
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, LevelChunkSection[] chunkSections,
                                              LevelHeightAccessor levelHeightAccessor, @Nullable ServerLevel serverLevel) {
        return new ProtoChunk(chunkPos, UpgradeData.EMPTY, chunkSections, new ProtoChunkTicks<>(),
                new ProtoChunkTicks<>(), levelHeightAccessor, DEFAULT_PALETTED_CONTAINER_FACTORY, null);
    }

    public static boolean isBlockStateLiquid(BlockState blockState) {
        return blockState.liquid();
    }

    public static boolean isLevelChunkSectionEmpty(LevelChunkSection levelChunkSection) {
        return levelChunkSection.hasOnlyAir();
    }

    public static void loadBlockEntity(BlockEntity blockEntity, CompoundTag compoundTag) {
        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(() -> "block_entity@" + blockEntity.getBlockPos(), LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, blockEntity.getLevel().registryAccess(), compoundTag);
            blockEntity.loadWithComponents(valueInput);
        }
    }

    public static void relightChunks(ThreadedLevelLightEngine lightEngine, Set<ChunkPos> chunks) {
        lightEngine.starlight$serverRelightChunks(chunks, chunkCallback -> {
        }, completeCallback -> {
        });
    }

    public static void rewriteSignLines(CompoundTag compoundTag) {
        applySignTextLines(compoundTag, "front_text");
        applySignTextLines(compoundTag, "back_text");
        convertLegacySignTextLines(compoundTag);
    }

    public static DimensionType getDimensionTypeFromDimension(Dimension dimension) {
        ResourceKey<LevelStem> resourceKey;
        switch (dimension.getEnvironment()) {
            case NETHER -> resourceKey = LevelStem.NETHER;
            case THE_END -> resourceKey = LevelStem.END;
            default -> resourceKey = LevelStem.OVERWORLD;
        }
        HolderLookup.RegistryLookup<LevelStem> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
        return registry.getOrThrow(resourceKey).value().type().value();
    }

    public static void createChunkSections(@Nullable ServerLevel serverLevel,
                                           LevelHeightAccessor levelHeightAccessor,
                                           LevelChunkSection[] chunkSections,
                                           Dimension dimension) {
        Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(IslandUtils.getDefaultWorldBiome(dimension));

        for (int i = 0; i < chunkSections.length; ++i) {
            PalettedContainer<Holder<Biome>> biomesContainer = createBiomesContainer(biome);
            PalettedContainer<BlockState> statesContainer = DEFAULT_PALETTED_CONTAINER_FACTORY.createForBlockStates();
            chunkSections[i] = new LevelChunkSection(statesContainer, biomesContainer);
        }
    }

    public static void buildSurfaceForChunk(ServerLevel serverLevel, ChunkGenerator bukkitGenerator, ChunkAccess chunkAccess) {
        CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(serverLevel,
                serverLevel.getChunkSource().getGenerator(), bukkitGenerator);

        ChunkStep surfaceStep = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.SURFACE);

        // Unsafe: we do not provide chunks cache, even tho it is required.
        // Should be fine in normal flow, as the only method that access the chunks cache
        // is WorldGenRegion#getChunk. Mimic`ing the cache seems to result an error:
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2121
        WorldGenRegion region = new WorldGenRegion(serverLevel, null, surfaceStep, chunkAccess);

        customChunkGenerator.buildSurface(region,
                serverLevel.structureManager().forWorldGenRegion(region),
                serverLevel.getChunkSource().randomState(),
                chunkAccess);
    }

    public static PalettedContainer<BlockState> createEmptyPlattedContainerStates() {
        return DEFAULT_PALETTED_CONTAINER_FACTORY.createForBlockStates();
    }

    public static PalettedContainer<BlockState> copyPalettedContainer(PalettedContainer<BlockState> original) {
        return original.copy();
    }

    public static CompoundTag saveBlockEntity(BlockEntity blockEntity) {
        return blockEntity.saveWithFullMetadata(MinecraftServer.getServer().registryAccess());
    }

    public static CompoundTag saveEntity(Entity entity) {
        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(() -> "cached_entity@" + entity.getUUID(), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, entity.level().registryAccess());
            entity.save(valueOutput);
            return valueOutput.buildResult();
        }
    }

    public static Material getMaterialFromBlock(Block block) {
        return CraftMagicNumbers.getMaterial(block);
    }

    public static ServerPlayer createServerPlayer(ServerLevel serverLevel, GameProfile gameProfile) {
        return new ServerPlayer(MinecraftServer.getServer(), serverLevel, gameProfile, ClientInformation.createDefault());
    }

    public static TickingBlockList getTickingBlockList(LevelChunkSection levelChunkSection) {
        return new TickingBlockList() {
            @Override
            public int size() {
                return levelChunkSection.moonrise$getTickingBlockList().size();
            }

            @Override
            public int getRaw(int index) {
                return (int) levelChunkSection.moonrise$getTickingBlockList().getRaw(index);
            }
        };
    }

    public static void addEntity(ServerLevel serverLevel, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        serverLevel.addFreshEntity(entity, spawnReason);
    }

    public static PropertyMap getProfileProperties(GameProfile gameProfile) {
        return gameProfile.properties();
    }

    public static String getPropertyValue(Property property) {
        return property.value();
    }

    public static EndDragonFight getEndDragonFight(ServerLevel serverLevel) {
        return serverLevel.getDragonFight();
    }

    public static void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.absSnapTo(x, y, z, yaw, pitch);
    }

    public static int getMinSection(ServerLevel serverLevel) {
        return serverLevel.getMinSectionY();
    }

    public static int getMinBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMinY();
    }

    public static int getMaxBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMaxY();
    }

    public static void markUnsaved(LevelChunk levelChunk) {
        levelChunk.markUnsaved();
    }

    public static PalettedContainer<Holder<Biome>> createBiomesContainer(Holder<Biome> biome) {
        Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BIOME);
        Strategy<Holder<Biome>> biomesStrategy = Strategy.createForBiomes(biomesRegistry.asHolderIdMap());
        return new PalettedContainerFactory(
                null,
                null,
                null,
                biomesStrategy,
                biome,
                null,
                null
        ).createForBiomes();
    }

    private static void applySignTextLines(CompoundTag blockEntityCompound, String key) {
        blockEntityCompound.getCompound(key).ifPresent(textCompound -> {
            ListTag messages = textCompound.getListOrEmpty("messages");
            List<Component> textLines = new ArrayList<>();
            for (net.minecraft.nbt.Tag lineTag : messages) {
                try {
                    textLines.add(CraftChatMessage.fromJSON(lineTag.asString().orElseThrow()));
                } catch (JsonParseException error) {
                    textLines.add(CraftChatMessage.fromString(lineTag.asString().orElseThrow())[0]);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (textLines.get(i) == null)
                    textLines.set(i, Component.empty());
            }

            Component[] textLinesArray = textLines.toArray(COMPONENT_ARRAY_TYPE);

            SignText signText = new SignText(textLinesArray, textLinesArray, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(nbt -> blockEntityCompound.put(key, nbt));
        });
    }

    private static void convertLegacySignTextLines(CompoundTag blockEntityCompound) {
        Component[] signLines = new Component[4];
        Arrays.fill(signLines, Component.empty());
        boolean hasAnySignLines = false;
        // We try to convert old text sign lines
        for (int i = 1; i <= 4; ++i) {
            if (blockEntityCompound.contains("SSB.Text" + i)) {
                String signLine = blockEntityCompound.getString("SSB.Text" + i).orElse(null);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromString(signLine)[0];
                    hasAnySignLines = true;
                }
            } else {
                String signLine = blockEntityCompound.getString("Text" + i).orElse(null);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromJSON(signLine);
                    hasAnySignLines = true;
                }
            }
        }

        if (hasAnySignLines) {
            SignText signText = new SignText(signLines, signLines, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(frontTextNBT -> blockEntityCompound.put("front_text", frontTextNBT));
        }
    }

    private NMSUtilsVersioned() {

    }

}
