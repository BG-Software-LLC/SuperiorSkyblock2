package com.bgsoftware.superiorskyblock.nms.v1_17.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.nms.v1_17.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkReaderImpl implements ChunkReader {

    private static final PalettedContainer<BlockState> EMPTY_STATES = new LevelChunkSection(0).getStates();
    private static final byte[] EMPTY_LIGHT = new byte[2048];

    private final ServerLevel serverLevel;
    private final int x;
    private final int z;

    private final Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
    private final List<CachedEntity> entities = new LinkedList<>();
    private final PalettedContainer<BlockState>[] blockids;
    private final byte[][] skylight;
    private final byte[][] emitlight;

    public ChunkReaderImpl(Chunk bukkitChunk) {
        LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();

        this.serverLevel = levelChunk.level;
        ChunkPos chunkPos = levelChunk.getPos();
        this.x = chunkPos.x;
        this.z = chunkPos.z;

        LevelChunkSection[] levelChunkSections = levelChunk.getSections();
        this.blockids = new PalettedContainer[levelChunkSections.length];
        this.skylight = new byte[levelChunkSections.length][];
        this.emitlight = new byte[levelChunkSections.length][];

        LevelLightEngine lightEngine = this.serverLevel.getLightEngine();

        for (int i = 0; i < this.blockids.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSections[i];

            if (LevelChunkSection.isEmpty(levelChunkSection)) {
                this.blockids[i] = EMPTY_STATES;
                this.skylight[i] = EMPTY_LIGHT;
                this.emitlight[i] = EMPTY_LIGHT;
            } else {
                this.blockids[i] = copyPalettedContainer(levelChunkSection.getStates());

                DataLayer skyLightArray = lightEngine.getLayerListener(LightLayer.SKY)
                        .getDataLayerData(SectionPos.of(this.x, i, this.z));
                if (skyLightArray == null) {
                    this.skylight[i] = EMPTY_LIGHT;
                } else {
                    this.skylight[i] = new byte[2048];
                    System.arraycopy(skyLightArray.getIfSet(), 0, this.skylight[i], 0, 2048);
                }

                DataLayer emitLightArray = lightEngine.getLayerListener(LightLayer.BLOCK)
                        .getDataLayerData(SectionPos.of(this.x, i, this.z));
                if (emitLightArray == null) {
                    this.emitlight[i] = EMPTY_LIGHT;
                } else {
                    this.emitlight[i] = new byte[2048];
                    System.arraycopy(emitLightArray.getIfSet(), 0, this.emitlight[i], 0, 2048);
                }
            }
        }

        levelChunk.getBlockEntities().forEach((blockPos, blockEntity) -> {
            net.minecraft.nbt.CompoundTag compoundTag = blockEntity.save(new net.minecraft.nbt.CompoundTag());

            compoundTag.remove("x");
            compoundTag.remove("y");
            compoundTag.remove("z");

            InventoryHolder inventoryHolder = blockEntity.getOwner();
            if (inventoryHolder != null)
                compoundTag.putString("inventoryType", inventoryHolder.getInventory().getType().name());

            blockEntities.put(blockPos, CompoundTag.fromNBT(compoundTag));
        });

        for (org.bukkit.entity.Entity entity : bukkitChunk.getEntities())
            entities.add(new CachedEntity(((CraftEntity) entity).getHandle()));
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public Material getType(int x, int y, int z) {
        return CraftMagicNumbers.getMaterial(getBlockState(x, y, z).getBlock());
    }

    @Override
    public short getData(int x, int y, int z) {
        return CraftMagicNumbers.toLegacyData(getBlockState(x, y, z));
    }

    @Override
    @Nullable
    public CompoundTag getTileEntity(int x, int y, int z) {
        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set((this.x << 4) + x, y, (this.z << 4) + z);
            return this.blockEntities.get(blockPos);
        }
    }

    @Override
    @Nullable
    public CompoundTag readBlockStates(int x, int y, int z) {
        BlockState blockState = getBlockState(x, y, z);

        if (blockState.getValues().isEmpty())
            return null;

        CompoundTag compoundTag = CompoundTag.of();

        blockState.getValues().forEach((property, value) -> {
            String name = PropertiesMapper.getPropertyName(property);

            if (property instanceof BooleanProperty) {
                compoundTag.setByte(name, (Boolean) value ? (byte) 1 : 0);
            } else if (property instanceof IntegerProperty integerProperty) {
                compoundTag.setIntArray(name, new int[]{(Integer) value, integerProperty.min, integerProperty.max});
            } else if (property instanceof EnumProperty<?>) {
                compoundTag.setString(name, ((Enum<?>) value).name());
            }
        });

        return compoundTag;
    }

    @Override
    public byte[] getLightLevels(int x, int y, int z) {
        int off = (y & 15) << 7 | z << 3 | x >> 1;
        int skyLightLevel = this.skylight[this.getSectionIndex(y)][off] >> ((x & 1) << 2) & 15;
        int emitLightLevel = this.emitlight[this.getSectionIndex(y)][off] >> ((x & 1) << 2) & 15;
        return new byte[]{(byte) skyLightLevel, (byte) emitLightLevel};
    }

    @Override
    public void forEachEntity(EntityConsumer consumer) {
        if (entities.isEmpty())
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = wrapper.getHandle();
            for (CachedEntity cachedEntity : entities) {
                location.setX(cachedEntity.x);
                location.setY(cachedEntity.y);
                location.setZ(cachedEntity.z);
                location.setYaw(cachedEntity.yaw);
                location.setPitch(cachedEntity.pitch);
                consumer.apply(cachedEntity.entityType, cachedEntity.entityTag, location);
            }
        }
    }

    private BlockState getBlockState(int x, int y, int z) {
        return this.blockids[this.getSectionIndex(y)].get(x, y & 15, z);
    }

    private int getSectionIndex(int y) {
        int sectionIndex = SectionPos.blockToSectionCoord(y);
        return sectionIndex - this.serverLevel.getMinSection();
    }

    private static PalettedContainer<BlockState> copyPalettedContainer(PalettedContainer<BlockState> original) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        original.write(data, "Palette", "BlockStates");

        PalettedContainer<BlockState> blockids = new PalettedContainer<>(LevelChunkSection.GLOBAL_BLOCKSTATE_PALETTE,
                Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState,
                Blocks.AIR.defaultBlockState(), null, false);
        blockids.read(data.getList("Palette", CraftMagicNumbers.NBT.TAG_COMPOUND), data.getLongArray("BlockStates"));

        return blockids;
    }

    private static class CachedEntity {

        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;
        private final EntityType entityType;
        private final CompoundTag entityTag;

        CachedEntity(Entity entity) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
            this.yaw = entity.getBukkitYaw();
            this.pitch = entity.getXRot();
            this.entityType = entity.getBukkitEntity().getType();

            net.minecraft.nbt.CompoundTag compoundTag = new net.minecraft.nbt.CompoundTag();
            entity.save(compoundTag);
            compoundTag.putFloat("Yaw", entity.getYRot());
            compoundTag.putFloat("Pitch", entity.getXRot());

            this.entityTag = CompoundTag.fromNBT(compoundTag);
        }

    }

}
