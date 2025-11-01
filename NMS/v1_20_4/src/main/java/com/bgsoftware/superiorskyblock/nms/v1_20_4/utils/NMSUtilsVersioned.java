package com.bgsoftware.superiorskyblock.nms.v1_20_4.utils;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.utils.TickingBlockList;
import com.google.common.base.Suppliers;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class NMSUtilsVersioned {

    private static final Component[] COMPONENT_ARRAY_TYPE = new Component[0];

    private static final ReflectField<PersistentEntitySectionManager<Entity>> SERVER_LEVEL_ENTITY_MANAGER = new ReflectField<>(
            ServerLevel.class, PersistentEntitySectionManager.class, Modifier.PUBLIC | Modifier.FINAL, 1);
    private static final ReflectField<IOWorker> ENTITY_STORAGE_WORKER = new ReflectField<>(
            EntityStorage.class, IOWorker.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    public static CompoundTag readChunk(ChunkMap chunkMap, ChunkPos chunkPos) {
        return chunkMap.read(chunkPos).join().orElse(null);
    }

    public static CompoundTag getChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ServerLevel serverLevel, ChunkPos chunkPos) {
        return chunkMap.upgradeChunkTag(serverLevel.getTypeKey(), Suppliers.ofInstance(serverLevel.getDataStorage()),
                chunkCompoundTag, Optional.empty(), chunkPos, serverLevel);
    }

    public static void saveChunkData(ChunkMap chunkMap, CompoundTag chunkCompoundTag, ChunkPos chunkPos) throws IOException {
        chunkMap.write(chunkPos, chunkCompoundTag);
    }

    public static BukkitExecutor.NestedTask<Void> runActionOnUnloadedEntityChunks(
            Collection<ChunkPosition> chunks, NMSUtils.ChunkCallback chunkCallback, CountDownLatch countDownLatch) {
        if (SERVER_LEVEL_ENTITY_MANAGER.isValid()) {
            return BukkitExecutor.createTask().runSync(v -> {
                chunks.forEach(chunkPosition -> {
                    ServerLevel serverLevel = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                    PersistentEntitySectionManager<Entity> entityManager = SERVER_LEVEL_ENTITY_MANAGER.get(serverLevel);
                    IOWorker worker = ENTITY_STORAGE_WORKER.get(entityManager.permanentStorage);

                    ChunkPos chunkPos = new ChunkPos(chunkPosition.getX(), chunkPosition.getZ());

                    worker.loadAsync(chunkPos).whenComplete((entityDataOptional, error) -> {
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
                        CompoundTag entityData = serverLevel.entityDataControllerNew.readData(
                                chunkPosition.getX(), chunkPosition.getZ());

                        if (entityData == null) {
                            chunkCallback.onChunkNotExist(chunkPosition);
                            return;
                        }

                        NMSUtils.UnloadedChunkCompound unloadedChunkCompound = new NMSUtils.UnloadedChunkCompound(chunkPosition, entityData);
                        chunkCallback.onUnloadedChunk(unloadedChunkCompound);
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
                serverLevel.registryAccess().registryOrThrow(Registries.BIOME),
                null);
    }

    public static ProtoChunk createProtoChunk(ChunkPos chunkPos, LevelChunkSection[] chunkSections,
                                              LevelHeightAccessor levelHeightAccessor, @Nullable ServerLevel serverLevel) {
        Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        return new ProtoChunk(chunkPos, UpgradeData.EMPTY, chunkSections,
                new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), levelHeightAccessor, biomesRegistry, null);
    }

    public static boolean isBlockStateLiquid(BlockState blockState) {
        return blockState.liquid();
    }

    public static boolean isLevelChunkSectionEmpty(LevelChunkSection levelChunkSection) {
        return levelChunkSection.hasOnlyAir();
    }

    public static void loadBlockEntity(BlockEntity blockEntity, CompoundTag compoundTag) {
        blockEntity.loadWithComponents(compoundTag, MinecraftServer.getServer().registryAccess());
    }

    public static void relightChunks(ThreadedLevelLightEngine lightEngine, Set<ChunkPos> chunks) {
        lightEngine.relight(chunks, chunkCallback -> {
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
        Registry<Biome> biomesRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);

        Holder<Biome> biome = CraftBiome.bukkitToMinecraftHolder(IslandUtils.getDefaultWorldBiome(dimension));

        for (int i = 0; i < chunkSections.length; ++i) {
            PalettedContainer<Holder<Biome>> biomesContainer = new PalettedContainer<>(biomesRegistry.asHolderIdMap(),
                    biome, PalettedContainer.Strategy.SECTION_BIOMES);
            PalettedContainer<BlockState> statesContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                    Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);

            chunkSections[i] = new LevelChunkSection(statesContainer, biomesContainer);
        }
    }

    public static void buildSurfaceForChunk(ServerLevel serverLevel, ChunkGenerator bukkitGenerator, ChunkAccess chunkAccess) {
        CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(serverLevel,
                serverLevel.getChunkSource().getGenerator(), bukkitGenerator);

        WorldGenRegion region = new WorldGenRegion(serverLevel, Collections.singletonList(chunkAccess),
                ChunkStatus.SURFACE, 0);

        chunkGenerator.buildSurface(region,
                serverLevel.structureManager().forWorldGenRegion(region),
                serverLevel.getChunkSource().randomState(),
                chunkAccess);
    }

    public static PalettedContainer<BlockState> createEmptyPlattedContainerStates() {
        return new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                PalettedContainer.Strategy.SECTION_STATES);
    }

    public static PalettedContainer<BlockState> copyPalettedContainer(PalettedContainer<BlockState> original) {
        return original.copy();
    }

    public static CompoundTag saveBlockEntity(BlockEntity blockEntity) {
        return blockEntity.saveWithFullMetadata(MinecraftServer.getServer().registryAccess());
    }

    public static CompoundTag saveEntity(Entity entity) {
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        return compoundTag;
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
                return levelChunkSection.tickingList.size();
            }

            @Override
            public int getRaw(int index) {
                return (int) levelChunkSection.tickingList.getRaw(index);
            }
        };
    }

    public static void addEntity(ServerLevel serverLevel, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        serverLevel.addFreshEntity(entity, spawnReason);
    }

    private static void applySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound, String key) {
        if (blockEntityCompound.contains(key)) {
            net.minecraft.nbt.CompoundTag frontText = blockEntityCompound.getCompound(key);
            ListTag messages = frontText.getList("messages", net.minecraft.nbt.Tag.TAG_STRING);
            List<Component> textLines = new ArrayList<>();
            for (net.minecraft.nbt.Tag lineTag : messages) {
                try {
                    textLines.add(CraftChatMessage.fromJSON(lineTag.getAsString()));
                } catch (JsonParseException error) {
                    textLines.add(CraftChatMessage.fromString(lineTag.getAsString())[0]);
                }
            }

            while (textLines.size() < 4)
                textLines.add(Component.empty());

            Component[] textLinesArray = textLines.toArray(COMPONENT_ARRAY_TYPE);

            SignText signText = new SignText(textLinesArray, textLinesArray, DyeColor.BLACK, false);
            SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, signText).result()
                    .ifPresent(frontTextNBT -> blockEntityCompound.put(key, frontTextNBT));
        }
    }

    private static void convertLegacySignTextLines(net.minecraft.nbt.CompoundTag blockEntityCompound) {
        Component[] signLines = new Component[4];
        Arrays.fill(signLines, Component.empty());
        boolean hasAnySignLines = false;
        // We try to convert old text sign lines
        for (int i = 1; i <= 4; ++i) {
            if (blockEntityCompound.contains("SSB.Text" + i)) {
                String signLine = blockEntityCompound.getString("SSB.Text" + i);
                if (!Text.isBlank(signLine)) {
                    signLines[i - 1] = CraftChatMessage.fromString(signLine)[0];
                    hasAnySignLines = true;
                }
            } else {
                String signLine = blockEntityCompound.getString("Text" + i);
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

    public static PropertyMap getProfileProperties(GameProfile gameProfile) {
        return gameProfile.getProperties();
    }

    public static String getPropertyValue(Property property) {
        return property.value();
    }

    public static EndDragonFight getEndDragonFight(ServerLevel serverLevel) {
        return serverLevel.getDragonFight();
    }

    public static void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.absMoveTo(x, y, z, yaw, pitch);
    }

    public static int getMinSection(ServerLevel serverLevel) {
        return serverLevel.getMinSection();
    }

    public static int getMinBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMinBuildHeight();
    }

    public static int getMaxBuildHeight(ServerLevel serverLevel) {
        return serverLevel.getMaxBuildHeight();
    }

    public static void markUnsaved(LevelChunk levelChunk) {
        levelChunk.setUnsaved(true);
    }

    private NMSUtilsVersioned() {

    }

}
