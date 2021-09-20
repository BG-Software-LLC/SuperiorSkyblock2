package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.blocks.ICachedBlock;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockDoubleStep;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.SoundCategory;
import net.minecraft.server.v1_12_R1.SoundEffectType;
import net.minecraft.server.v1_12_R1.SoundEffects;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.TileEntitySign;
import net.minecraft.server.v1_12_R1.WorldBorder;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;

public final class NMSWorldImpl implements NMSWorld {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid", BiomeBase[].class, "biome");

    @Override
    @SuppressWarnings("deprecation")
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        Material type = Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y, z));
        short data = (short) chunkSnapshot.getBlockData(x, y, z);

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
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) ((CraftWorld) location.getWorld())
                .getTileEntityAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return mobSpawner.getSpawner().spawnDelay;
    }

    @Override
    public void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay) {
        Location location = creatureSpawner.getLocation();
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) ((CraftWorld) location.getWorld())
                .getTileEntityAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        mobSpawner.getSpawner().spawnDelay = spawnDelay;
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if (!plugin.getSettings().isWorldBorders())
                return;

            Player player = superiorPlayer.asPlayer();
            World world = superiorPlayer.getWorld();

            if (world == null || player == null)
                return;

            WorldServer worldServer = ((CraftWorld) superiorPlayer.getWorld()).getHandle();

            WorldBorder worldBorder;

            if (!superiorPlayer.hasWorldBorderEnabled() || island == null ||
                    (!plugin.getSettings().getSpawn().isWorldBorder() && island.isSpawn())) {
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

            PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome) {
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

        BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(biomeGrid);

        if (biomeBases == null)
            return;

        Arrays.fill(biomeBases, biomeBase);
    }

    @Override
    public Object getBlockData(Block block) {
        // Doesn't exist
        return null;
    }

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for (BlockData blockData : blockDataList) {
            NMSUtils.setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getClonedTileEntity());
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        //noinspection deprecation
        int combinedId = material.getId() + (data << 12);
        NMSUtils.setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, combinedId, null);

        NMSUtils.sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return new NMSCachedBlock(block);
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        // Doesn't exist
        return null;
    }

    @Override
    public byte[] getLightLevels(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        return new byte[]{
                (byte) world.getBrightness(EnumSkyBlock.SKY, blockPosition),
                (byte) world.getBrightness(EnumSkyBlock.BLOCK, blockPosition),
        };
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
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
    public boolean isWaterLogged(Block block) {
        Material blockType = block.getType();
        return blockType == Material.WATER || blockType == Material.STATIONARY_WATER;
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        Location blockLocation = block.getLocation();
        IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(
                blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ()));
        net.minecraft.server.v1_12_R1.Block nmsBlock = blockData.getBlock();

        // Checks for double slabs
        if (nmsBlock instanceof BlockDoubleStep) {
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
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        world.a(null, blockPosition, SoundEffects.dE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; i++)
            world.addParticle(EnumParticle.SMOKE_LARGE, x + Math.random(), y + 1.2D, z + Math.random(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, net.minecraft.server.v1_12_R1.Block.getCombinedId(world.getType(blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        SoundEffectType soundeffecttype = world.getType(blockPosition).getBlock().getStepSound();
        world.a(null, blockPosition, soundeffecttype.e(), SoundCategory.BLOCKS, (soundeffecttype.a() + 1.0F) / 2.0F, soundeffecttype.b() * 0.8F);
    }


}
