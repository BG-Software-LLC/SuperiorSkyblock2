package com.bgsoftware.superiorskyblock.nms.v1_12_R1.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataPaletteBlock;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NibbleArray;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkReaderImpl implements ChunkReader {

    private static final short[] EMPTY_BLOCKS = new short[4096];
    private static final byte[] EMPTY_DATA = new byte[2048];
    private static final byte[] EMPTY_LIGHT = new byte[2048];

    private final int x;
    private final int z;

    private final Map<BlockPosition, CompoundTag> tileEntities = new HashMap<>();
    private final List<CachedEntity> entities = new LinkedList<>();
    private final short[][] blockids;
    private final byte[][] blockdata;
    private final byte[][] skylight;
    private final byte[][] emitlight;

    public ChunkReaderImpl(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        this.x = chunk.locX;
        this.z = chunk.locZ;

        ChunkSection[] chunkSections = chunk.getSections();
        this.blockids = new short[chunkSections.length][];
        this.blockdata = new byte[chunkSections.length][];
        this.skylight = new byte[chunkSections.length][];
        this.emitlight = new byte[chunkSections.length][];

        for (int i = 0; i < this.blockids.length; ++i) {
            ChunkSection chunkSection = chunkSections[i];

            if (chunkSection == null) {
                this.blockids[i] = EMPTY_BLOCKS;
                this.blockdata[i] = EMPTY_DATA;
                this.skylight[i] = EMPTY_LIGHT;
                this.emitlight[i] = EMPTY_LIGHT;
            } else {
                copyBlockIds(chunkSection.getBlocks(), i);

                if (chunkSection.getSkyLightArray() == null) {
                    skylight[i] = EMPTY_LIGHT;
                } else {
                    skylight[i] = new byte[2048];
                    System.arraycopy(chunkSection.getSkyLightArray().asBytes(), 0, skylight[i], 0, 2048);
                }

                emitlight[i] = new byte[2048];
                System.arraycopy(chunkSection.getEmittedLightArray().asBytes(), 0, emitlight[i], 0, 2048);
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
        return Material.getMaterial(this.getBlockId(x, y, z));
    }

    @Override
    public short getData(int x, int y, int z) {
        int off = (y & 15) << 7 | z << 3 | x >> 1;
        return (short) (this.blockdata[y >> 4][off] >> ((x & 1) << 2) & 15);
    }

    @Override
    @Nullable
    public CompoundTag getTileEntity(int x, int y, int z) {
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c((this.x << 4) + x, y, (this.z << 4) + z);
            return this.tileEntities.get(blockPosition);
        }
    }

    @Override
    @Nullable
    public CompoundTag readBlockStates(int x, int y, int z) {
        // Doesn't exist
        return null;
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

    private int getBlockId(int x, int y, int z) {
        return this.blockids[y >> 4][(y & 15) << 8 | z << 4 | x];
    }

    private void copyBlockIds(DataPaletteBlock palette, int i) {
        byte[] rawIds = new byte[4096];
        NibbleArray data = new NibbleArray();
        palette.exportData(rawIds, data);

        short[] blockids = this.blockids[i] = new short[4096];
        this.blockdata[i] = data.asBytes();

        for (int j = 0; j < 4096; ++j) {
            blockids[j] = (short) (rawIds[j] & 255);
        }
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
            this.x = entity.locX;
            this.y = entity.locY;
            this.z = entity.locZ;
            this.yaw = entity.yaw;
            this.pitch = entity.pitch;
            this.entityType = entity.getBukkitEntity().getType();

            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            entity.c(nbtTagCompound);
            nbtTagCompound.set("Yaw", new NBTTagFloat(entity.yaw));
            nbtTagCompound.set("Pitch", new NBTTagFloat(entity.pitch));

            this.entityTag = CompoundTag.fromNBT(nbtTagCompound);
        }

    }

}
