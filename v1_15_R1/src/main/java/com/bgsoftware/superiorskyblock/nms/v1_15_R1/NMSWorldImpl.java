package com.bgsoftware.superiorskyblock.nms.v1_15_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.v1_15_R1.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.blocks.ICachedBlock;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import net.minecraft.server.v1_15_R1.BiomeBase;
import net.minecraft.server.v1_15_R1.BiomeStorage;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.BlockProperties;
import net.minecraft.server.v1_15_R1.BlockPropertySlabType;
import net.minecraft.server.v1_15_R1.BlockStateBoolean;
import net.minecraft.server.v1_15_R1.BlockStateEnum;
import net.minecraft.server.v1_15_R1.BlockStateInteger;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.EnumSkyBlock;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IBlockState;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.LightEngine;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_15_R1.SoundCategory;
import net.minecraft.server.v1_15_R1.SoundEffectType;
import net.minecraft.server.v1_15_R1.TagsBlock;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_15_R1.TileEntitySign;
import net.minecraft.server.v1_15_R1.WorldBorder;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class NMSWorldImpl implements NMSWorld {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "f", "g");
    private static final ReflectField<BiomeStorage> BIOME_STORAGE = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid", BiomeStorage.class, "biome");

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (net.minecraft.server.v1_15_R1.Block.getCombinedId(blockData) >> 12 & 15);

        Location location = new Location(
                Bukkit.getWorld(chunkSnapshot.getWorldName()),
                (chunkSnapshot.getX() << 4) + x,
                y,
                (chunkSnapshot.getZ() << 4) + z
        );

        return Key.of(Key.of(type, data), location);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner creatureSpawner) {
        Location location = creatureSpawner.getLocation();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) ((CraftWorld) location.getWorld()).getHandle().getTileEntity(blockPosition);
        return mobSpawner == null ? 0 : mobSpawner.getSpawner().spawnDelay;
    }

    @Override
    public void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay) {
        Location location = creatureSpawner.getLocation();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) ((CraftWorld) location.getWorld()).getHandle().getTileEntity(blockPosition);
        if (mobSpawner != null)
            mobSpawner.getSpawner().spawnDelay = spawnDelay;
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if (!plugin.getSettings().isWorldBorders())
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            Player player = superiorPlayer.asPlayer();
            World world = superiorPlayer.getWorld();

            if (world == null || player == null)
                return;

            WorldServer worldServer = ((CraftWorld) superiorPlayer.getWorld()).getHandle();

            WorldBorder worldBorder;

            if (disabled || island == null || (!plugin.getSettings().getSpawn().isWorldBorder() && island.isSpawn())) {
                worldBorder = worldServer.getWorldBorder();
            } else {
                worldBorder = new WorldBorder();

                worldBorder.world = worldServer;
                worldBorder.setSize((island.getIslandSize() * 2) + 1);

                org.bukkit.World.Environment environment = world.getEnvironment();

                Location center = island.getCenter(environment);
                worldBorder.setCenter(center.getX(), center.getZ());

                switch (superiorPlayer.getBorderColor()) {
                    case GREEN:
                        worldBorder.transitionSizeBetween(worldBorder.getSize() - 0.1D, worldBorder.getSize(), Long.MAX_VALUE);
                        break;
                    case RED:
                        worldBorder.transitionSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 1.0D, Long.MAX_VALUE);
                        break;
                }
            }

            PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder,
                    PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome) {
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

        BiomeStorage biomeStorage = BIOME_STORAGE.get(biomeGrid);
        BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(biomeStorage);

        if (biomeBases == null)
            return;

        Arrays.fill(biomeBases, biomeBase);
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for (BlockData blockData : blockDataList) {
            NMSUtils.setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getClonedTileEntity());
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        NMSUtils.setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition,
                plugin.getNMSAlgorithms().getCombinedId(material, data), null, null);
        NMSUtils.sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return new NMSCachedBlock(block);
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = world.getType(blockPosition);
        CompoundTag compoundTag = null;

        for (Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.getStateMap().entrySet()) {
            if (compoundTag == null)
                compoundTag = new CompoundTag();

            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = entry.getKey().a();

            if (keyClass.equals(BlockStateBoolean.class)) {
                value = new ByteTag((Boolean) entry.getValue() ? (byte) 1 : 0);
            } else if (keyClass.equals(BlockStateInteger.class)) {
                BlockStateInteger key = (BlockStateInteger) entry.getKey();
                value = new IntArrayTag(new int[]{(Integer) entry.getValue(), key.min, key.max});
            } else {
                BlockStateEnum<?> key = (BlockStateEnum<?>) entry.getKey();
                name = BlockStatesMapper.getBlockStateName(key);
                value = new StringTag(((Enum<?>) entry.getValue()).name());
            }

            compoundTag.setTag(name, value);
        }

        return compoundTag;
    }

    @Override
    public byte[] getLightLevels(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        LightEngine lightEngine = ((CraftWorld) location.getWorld()).getHandle().e();
        return new byte[]{
                location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL ? 0 : (byte) lightEngine.a(EnumSkyBlock.SKY).b(blockPosition),
                (byte) lightEngine.a(EnumSkyBlock.BLOCK).b(blockPosition)
        };
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if (tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = tileEntity.save(new NBTTagCompound());

        tileEntityCompound.remove("x");
        tileEntityCompound.remove("y");
        tileEntityCompound.remove("z");

        return CompoundTag.fromNBT(tileEntityCompound);
    }

    @Override
    public boolean isWaterLogged(org.bukkit.block.Block block) {
        if (block.getType().name().contains("WATER"))
            return true;

        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        return blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        IBlockData blockData = ((CraftBlock) block).getNMS();
        net.minecraft.server.v1_15_R1.Block nmsBlock = blockData.getBlock();

        // Checks for double slabs
        if ((nmsBlock.a(TagsBlock.SLABS) || nmsBlock.a(TagsBlock.WOODEN_SLABS)) &&
                blockData.get(BlockProperties.aD) == BlockPropertySlabType.DOUBLE) {
            return 2;
        }

        return 1;
    }

    @Override
    public void placeSign(Island island, Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if (tileEntity instanceof TileEntitySign) {
            TileEntitySign tileEntitySign = (TileEntitySign) tileEntity;
            String[] lines = new String[4];
            System.arraycopy(CraftSign.revertComponents(tileEntitySign.lines), 0, lines, 0, lines.length);
            String[] strippedLines = new String[4];
            for (int i = 0; i < 4; i++)
                strippedLines[i] = StringUtils.stripColors(lines[i]);

            IChatBaseComponent[] newLines;

            if (BlocksLogic.handleSignPlace(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
        }
    }

    @Override
    public void setSignLines(SignChangeEvent signChangeEvent, String[] lines) {

    }

    @Override
    public void playGeneratorSound(Location location) {
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        world.triggerEffect(1501, blockPosition, 0);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        SoundEffectType soundeffecttype = world.getType(blockPosition).r();
        world.playSound(null, blockPosition, soundeffecttype.e(), SoundCategory.BLOCKS, (soundeffecttype.a() + 1.0F) / 2.0F, soundeffecttype.b() * 0.8F);
    }

}
