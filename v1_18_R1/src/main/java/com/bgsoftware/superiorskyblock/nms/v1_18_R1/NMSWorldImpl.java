package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.blocks.ICachedBlock;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockStepAbstract;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertySlabType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.lighting.LightEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;
import java.util.Map;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

public final class NMSWorldImpl implements NMSWorld {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<IChunkAccess> CHUNK_ACCESS = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid", IChunkAccess.class, "biome");
    private static final ReflectMethod<Object> LINES_SIGN_CHANGE_EVENT = new ReflectMethod<>(
            SignChangeEvent.class, "lines");

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (getCombinedId(blockData) >> 12 & 15);

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
        World world = location.getWorld();

        if (world == null)
            return 0;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) getTileEntity(((CraftWorld) world).getHandle(), blockPosition);
        return mobSpawner == null ? 0 : getSpawner(mobSpawner).c;
    }

    @Override
    public void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay) {
        Location location = creatureSpawner.getLocation();
        World world = location.getWorld();

        if (world == null)
            return;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) getTileEntity(((CraftWorld) world).getHandle(), blockPosition);
        if (mobSpawner != null)
            getSpawner(mobSpawner).c = spawnDelay;
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
                worldBorder = getWorldBorder(worldServer);
            } else {
                worldBorder = new WorldBorder();

                worldBorder.world = worldServer;
                setSize(worldBorder, (island.getIslandSize() * 2) + 1);

                org.bukkit.World.Environment environment = world.getEnvironment();

                Location center = island.getCenter(environment);
                setCenter(worldBorder, center.getX(), center.getZ());
                double worldBorderSize = getSize(worldBorder);

                switch (superiorPlayer.getBorderColor()) {
                    case GREEN -> transitionSizeBetween(worldBorder, worldBorderSize - 0.1D, worldBorderSize, Long.MAX_VALUE);
                    case RED -> transitionSizeBetween(worldBorder, worldBorderSize, worldBorderSize - 1.0D, Long.MAX_VALUE);
                }
            }

            ClientboundInitializeBorderPacket packetPlayOutWorldBorder = new ClientboundInitializeBorderPacket(worldBorder);
            sendPacket(((CraftPlayer) player).getHandle().b, packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome) {
        IChunkAccess chunk = CHUNK_ACCESS.get(biomeGrid);

        if (chunk == null)
            return;

        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(chunk.biomeRegistry, biome);

        ChunkSection[] chunkSections = getSections(chunk);
        for (int i = 0; i < chunkSections.length; ++i) {
            ChunkSection currentSection = chunkSections[i];
            if (currentSection != null) {
                DataPaletteBlock<IBlockData> dataPaletteBlock = getBlocks(currentSection);
                chunkSections[i] = new ChunkSection(getYPosition(currentSection) >> 4, dataPaletteBlock,
                        new DataPaletteBlock<>(chunk.biomeRegistry, biomeBase, DataPaletteBlock.e.e));
            }
        }
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<BlockData> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for (BlockData blockData : blockDataList) {
            NMSUtils.setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getClonedTileEntity());
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        NMSUtils.setBlock(getChunkAtWorldCoords(world, blockPosition), blockPosition,
                plugin.getNMSAlgorithms().getCombinedId(material, data), null, null);

        NMSUtils.sendPacketToRelevantPlayers(world, getX(blockPosition) >> 4, getZ(blockPosition) >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return new NMSCachedBlock(block);
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return null;

        net.minecraft.world.level.World world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = getType(world, blockPosition);
        CompoundTag compoundTag = null;

        for (Map.Entry<IBlockState<?>, Comparable<?>> entry : getStateMap(blockData).entrySet()) {
            if (compoundTag == null)
                compoundTag = new CompoundTag();

            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = getName(entry.getKey());

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
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return new byte[0];

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        LightEngine lightEngine = getLightEngine(((CraftWorld) bukkitWorld).getHandle());
        return new byte[]{
                location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL ? 0 :
                        (byte) lightEngine.a(EnumSkyBlock.a).b(blockPosition),
                (byte) lightEngine.a(EnumSkyBlock.b).b(blockPosition)
        };
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return null;

        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = getTileEntity(world, blockPosition);

        if (tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = save(tileEntity);

        remove(tileEntityCompound, "x");
        remove(tileEntityCompound, "y");
        remove(tileEntityCompound, "z");

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
        Block nmsBlock = getBlock(blockData);

        // Checks for double slabs
        if ((isTagged(TagsBlock.E, nmsBlock) || isTagged(TagsBlock.j, nmsBlock)) &&
                get(blockData, BlockStepAbstract.a) == BlockPropertySlabType.c) {
            return 2;
        }

        return 1;
    }

    @Override
    public void placeSign(Island island, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();
        TileEntity tileEntity = getTileEntity(worldServer, blockPosition);
        if (tileEntity instanceof TileEntitySign tileEntitySign) {
            String[] lines = new String[4];
            System.arraycopy(CraftSign.revertComponents(tileEntitySign.d), 0, lines, 0, lines.length);
            String[] strippedLines = new String[4];
            for (int i = 0; i < 4; i++)
                strippedLines[i] = StringUtils.stripColors(lines[i]);

            IChatBaseComponent[] newLines;

            if (BlocksLogic.handleSignPlace(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.d, 0, 4);
        }
    }

    @Override
    public void setSignLines(SignChangeEvent signChangeEvent, String[] lines) {
        if (LINES_SIGN_CHANGE_EVENT.isValid()) {
            for (int i = 0; i < lines.length; i++)
                signChangeEvent.setLine(i, lines[i]);
        }
    }

    @Override
    public void playGeneratorSound(Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        triggerEffect(world, 1501, blockPosition, 0);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        net.minecraft.world.level.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, getCombinedId(getType(world, blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        SoundEffectType soundEffectType = getStepSound(getType(world, blockPosition));

        playSound(world, null, blockPosition, getPlaceSound(soundEffectType),
                SoundCategory.e, (getVolume(soundEffectType) + 1.0F) / 2.0F, getPitch(soundEffectType) * 0.8F);
    }

    @Override
    public int getMinHeight(World world) {
        return world.getMinHeight();
    }

}
