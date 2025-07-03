package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockStateBoolean;
import net.minecraft.server.v1_16_R3.BlockStateInteger;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.DataPaletteBlock;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EnumSkyBlock;
import net.minecraft.server.v1_16_R3.GameProfileSerializer;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IBlockState;
import net.minecraft.server.v1_16_R3.LightEngine;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagFloat;
import net.minecraft.server.v1_16_R3.NibbleArray;
import net.minecraft.server.v1_16_R3.SectionPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkReaderImpl implements ChunkReader {

    private static final DataPaletteBlock<IBlockData> EMPTY_BLOCKS = new ChunkSection(0).getBlocks();
    private static final byte[] EMPTY_LIGHT = new byte[2048];

    private final int x;
    private final int z;

    private final Map<BlockPosition, CompoundTag> tileEntities = new HashMap<>();
    private final List<CachedEntity> entities = new LinkedList<>();
    private final DataPaletteBlock<IBlockData>[] blockids;
    private final byte[][] skylight;
    private final byte[][] emitlight;

    public ChunkReaderImpl(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        ChunkCoordIntPair chunkCoords = chunk.getPos();
        this.x = chunkCoords.x;
        this.z = chunkCoords.z;

        ChunkSection[] chunkSections = chunk.getSections();
        this.blockids = new DataPaletteBlock[chunkSections.length];
        this.skylight = new byte[chunkSections.length][];
        this.emitlight = new byte[chunkSections.length][];

        LightEngine lightEngine = chunk.world.getChunkProvider().getLightEngine();

        for (int i = 0; i < this.blockids.length; ++i) {
            ChunkSection chunkSection = chunkSections[i];

            if (chunkSection == null) {
                this.blockids[i] = EMPTY_BLOCKS;
                this.skylight[i] = EMPTY_LIGHT;
                this.emitlight[i] = EMPTY_LIGHT;
            } else {
                this.blockids[i] = copyDataPalette(chunkSection.getBlocks());

                NibbleArray skyLightArray = lightEngine.a(EnumSkyBlock.SKY).a(SectionPosition.a(this.x, i, this.z));
                if (skyLightArray == null) {
                    this.skylight[i] = EMPTY_LIGHT;
                } else {
                    this.skylight[i] = new byte[2048];
                    if (!skyLightArray.c())
                        System.arraycopy(skyLightArray.asBytes(), 0, this.skylight[i], 0, 2048);
                }

                NibbleArray emitLightArray = lightEngine.a(EnumSkyBlock.BLOCK).a(SectionPosition.a(this.x, i, this.z));
                if (emitLightArray == null) {
                    this.emitlight[i] = EMPTY_LIGHT;
                } else {
                    this.emitlight[i] = new byte[2048];
                    if (!emitLightArray.c())
                        System.arraycopy(emitLightArray.asBytes(), 0, this.emitlight[i], 0, 2048);
                }
            }
        }

        chunk.getTileEntities().forEach((blockPosition, tileEntity) -> {
            NBTTagCompound tileEntityCompound = tileEntity.save(new NBTTagCompound());

            tileEntityCompound.remove("x");
            tileEntityCompound.remove("y");
            tileEntityCompound.remove("z");

            InventoryHolder inventoryHolder = tileEntity.getOwner();
            if (inventoryHolder != null)
                tileEntityCompound.setString("inventoryType", inventoryHolder.getInventory().getType().name());

            tileEntities.put(blockPosition, CompoundTag.fromNBT(tileEntityCompound));
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
        return CraftMagicNumbers.getMaterial(getBlockData(x, y, z).getBlock());
    }

    @Override
    public short getData(int x, int y, int z) {
        return CraftMagicNumbers.toLegacyData(getBlockData(x, y, z));
    }

    @Override
    @Nullable
    public CompoundTag getTileEntity(int x, int y, int z) {
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.d((this.x << 4) + x, y, (this.z << 4) + z);
            return this.tileEntities.get(blockPosition);
        }
    }

    @Override
    @Nullable
    public CompoundTag readBlockStates(int x, int y, int z) {
        IBlockData blockData = getBlockData(x, y, z);

        if (blockData.getStateMap().isEmpty())
            return null;

        CompoundTag compoundTag = new CompoundTag();

        for (Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.getStateMap().entrySet()) {
            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = BlockStatesMapper.getBlockStateName(entry.getKey());

            if (keyClass.equals(BlockStateBoolean.class)) {
                value = new ByteTag((Boolean) entry.getValue() ? (byte) 1 : 0);
            } else if (keyClass.equals(BlockStateInteger.class)) {
                BlockStateInteger key = (BlockStateInteger) entry.getKey();
                value = new IntArrayTag(new int[]{(Integer) entry.getValue(), key.min, key.max});
            } else {
                value = new StringTag(((Enum<?>) entry.getValue()).name());
            }

            compoundTag.setTag(name, value);
        }

        return compoundTag;
    }

    @Override
    public byte[] getLightLevels(int x, int y, int z) {
        int off = (y & 15) << 7 | z << 3 | x >> 1;
        int skyLightLevel = this.skylight[y >> 4][off] >> ((x & 1) << 2) & 15;
        int emitLightLevel = this.emitlight[y >> 4][off] >> ((x & 1) << 2) & 15;
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

    private IBlockData getBlockData(int x, int y, int z) {
        return this.blockids[y >> 4].a(x, y & 15, z);
    }

    private static DataPaletteBlock<IBlockData> copyDataPalette(DataPaletteBlock<IBlockData> original) {
        NBTTagCompound data = new NBTTagCompound();
        original.a(data, "Palette", "BlockStates");

        DataPaletteBlock<IBlockData> blockids = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE, Block.REGISTRY_ID,
                GameProfileSerializer::c, GameProfileSerializer::a, Blocks.AIR.getBlockData());
        blockids.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));

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
            this.x = entity.locX();
            this.y = entity.locY();
            this.z = entity.locZ();
            this.yaw = entity.getBukkitYaw();
            this.pitch = entity.pitch;
            this.entityType = entity.getBukkitEntity().getType();

            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            entity.save(nbtTagCompound);
            nbtTagCompound.set("Yaw", NBTTagFloat.a(entity.yaw));
            nbtTagCompound.set("Pitch", NBTTagFloat.a(entity.pitch));

            this.entityTag = CompoundTag.fromNBT(nbtTagCompound);
        }

    }

}
