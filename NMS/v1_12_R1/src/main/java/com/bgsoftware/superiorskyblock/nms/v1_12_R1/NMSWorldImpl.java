package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.generator.IslandsGeneratorImpl;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.spawners.MobSpawnerAbstractNotifier;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.world.ChunkReaderImpl;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.world.WorldEditSessionImpl;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.world.SignType;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockDoubleStep;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.SoundCategory;
import net.minecraft.server.v1_12_R1.SoundEffectType;
import net.minecraft.server.v1_12_R1.SoundEffects;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.TileEntitySign;
import net.minecraft.server.v1_12_R1.WorldBorder;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Modifier;
import java.util.function.IntFunction;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();

    private final SuperiorSkyblockPlugin plugin;

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        int blockId = chunkSnapshot.getBlockTypeId(x, y, z);
        int blockData = chunkSnapshot.getBlockData(x, y, z);
        int combinedId = blockId + (blockData << 12);

        Location location = new Location(
                Bukkit.getWorld(chunkSnapshot.getWorldName()),
                (chunkSnapshot.getX() << 4) + x,
                y,
                (chunkSnapshot.getZ() << 4) + z
        );

        return Keys.of(KeyBlocksCache.getBlockKey(Block.getByCombinedId(combinedId)), location);
    }

    @Override
    public void listenSpawner(Location location, IntFunction<Integer> delayChangeCallback) {
        TileEntityMobSpawner mobSpawner = NMSUtils.getTileEntityAt(location, TileEntityMobSpawner.class);
        if (mobSpawner == null)
            return;

        MobSpawnerAbstract mobSpawnerAbstract = mobSpawner.getSpawner();
        if (mobSpawnerAbstract instanceof MobSpawnerAbstractNotifier)
            return;

        MobSpawnerAbstractNotifier mobSpawnerAbstractNotifier = new MobSpawnerAbstractNotifier(mobSpawnerAbstract, delayChangeCallback);
        MOB_SPAWNER_ABSTRACT.set(mobSpawner, mobSpawnerAbstractNotifier);
        mobSpawnerAbstractNotifier.updateDelay();
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        if (!plugin.getSettings().isWorldBorders())
            return;

        Player player = superiorPlayer.asPlayer();
        World world = superiorPlayer.getWorld();

        if (world == null || player == null)
            return;

        int islandSize = island == null ? 0 : island.getIslandSize();

        boolean disabled = !superiorPlayer.hasWorldBorderEnabled() || islandSize < 0;

        WorldServer worldServer = ((CraftWorld) superiorPlayer.getWorld()).getHandle();

        WorldBorder worldBorder;

        if (disabled || island == null || (!plugin.getSettings().getSpawn().isWorldBorder() && island.isSpawn())) {
            worldBorder = worldServer.getWorldBorder();
        } else {
            Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
            if (dimension == null)
                return;

            Location center = island.getCenter(dimension);

            worldBorder = new WorldBorder();
            worldBorder.world = worldServer;
            worldBorder.setWarningDistance(0);
            worldBorder.setCenter(center.getX(), center.getZ());

            switch (superiorPlayer.getBorderColor()) {
                case BLUE: {
                    worldBorder.setSize((islandSize * 2) + 1D);
                    break;
                }
                case GREEN: {
                    worldBorder.setSize((islandSize * 2) + 1.001D);
                    worldBorder.transitionSizeBetween(worldBorder.getSize() - 0.001D, worldBorder.getSize(), Long.MAX_VALUE);
                    break;
                }
                case RED: {
                    worldBorder.setSize((islandSize * 2) + 1D);
                    worldBorder.transitionSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 0.001D, Long.MAX_VALUE);
                    break;
                }
            }
        }

        PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            NMSUtils.setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, combinedId, null);
            NMSUtils.sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                    new PacketPlayOutBlockChange(world, blockPosition));
        }
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return NMSCachedBlock.obtain(block);
    }

    @Override
    public boolean isWaterLogged(org.bukkit.block.Block block) {
        Material blockType = block.getType();
        return blockType == Material.WATER || blockType == Material.STATIONARY_WATER;
    }

    @Override
    public SignType getSignType(org.bukkit.block.Block block) {
        Material blockType = block.getType();
        return blockType == Material.SIGN_POST ? SignType.STANDING_SIGN :
                blockType == Material.WALL_SIGN ? SignType.WALL_SIGN : SignType.UNKNOWN;
    }

    @Override
    public SignType getSignType(Object sign) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PistonPushReaction getPistonReaction(org.bukkit.block.Block block) {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = worldServer.getType(blockPosition);
        return PistonPushReaction.values()[blockData.o().ordinal()];
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        IBlockData blockData = worldServer.getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        return getDefaultAmount(blockData);
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.BlockState bukkitBlockState) {
        MaterialData materialData = bukkitBlockState.getData();
        // noinspection deprecation
        int combinedId = materialData.getItemType().getId() + (materialData.getData() << 12);
        return getDefaultAmount(Block.getByCombinedId(combinedId));
    }

    private int getDefaultAmount(IBlockData blockData) {
        Block nmsBlock = blockData.getBlock();

        // Checks for double slabs
        if (nmsBlock instanceof BlockDoubleStep) {
            return 2;
        }

        return 1;
    }

    @Override
    public boolean canPlayerSuffocate(org.bukkit.block.Block bukkitBlock) {
        WorldServer worldServer = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
            return worldServer.getType(blockPosition).r();
        }
    }

    @Override
    public void placeSign(Island island, Location location) {
        TileEntitySign tileEntitySign = NMSUtils.getTileEntityAt(location, TileEntitySign.class);
        if (tileEntitySign == null)
            return;

        String[] lines = new String[4];
        System.arraycopy(CraftSign.revertComponents(tileEntitySign.lines), 0, lines, 0, lines.length);
        String[] strippedLines = new String[4];
        for (int i = 0; i < 4; i++)
            strippedLines[i] = Formatters.STRIP_COLOR_FORMATTER.format(lines[i]);

        IChatBaseComponent[] newLines;

        IslandSigns.Result result = IslandSigns.handleSignPlace(island.getOwner(), location, strippedLines, false);
        if (result.isCancelEvent()) {
            newLines = CraftSign.sanitizeLines(strippedLines);
        } else {
            newLines = CraftSign.sanitizeLines(lines);
        }

        System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
    }

    @Override
    public void playGeneratorSound(Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();

            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            blockPosition.c(x, y, z);

            worldServer.a(null, blockPosition, SoundEffects.dE, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (worldServer.random.nextFloat() - worldServer.random.nextFloat()) * 0.8F);

            for (int i = 0; i < 8; i++) {
                worldServer.addParticle(EnumParticle.SMOKE_LARGE,
                        x + Math.random(), y + 1.2D, z + Math.random(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(block.getX(), block.getY(), block.getZ());
            worldServer.a(null, 2001, blockPosition, Block.getCombinedId(worldServer.getType(blockPosition)));
        }
    }

    @Override
    public void playPlaceSound(Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SoundEffectType soundEffectType = worldServer.getType(blockPosition).getBlock().getStepSound();

            worldServer.a(null, blockPosition, soundEffectType.e(), SoundCategory.BLOCKS,
                    (soundEffectType.a() + 1.0F) / 2.0F, soundEffectType.b() * 0.8F);
        }
    }

    @Override
    public int getMinHeight(World world) {
        return 0;
    }

    @Override
    public void removeAntiXray(World world) {
        // Doesn't exist in this version.
    }

    @Override
    public IslandsGenerator createGenerator(Dimension dimension) {
        return new IslandsGeneratorImpl(dimension);
    }

    @Override
    public WorldEditSession createEditSession(World world) {
        return WorldEditSessionImpl.obtain(((CraftWorld) world).getHandle());
    }

    @Override
    public ChunkReader createChunkReader(Chunk chunk) {
        return new ChunkReaderImpl(chunk);
    }

}
