package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.algorithms.NMSCachedBlock;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.generator.IslandsGeneratorImpl;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.spawners.TileEntityMobSpawnerNotifier;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.ChunkReaderImpl;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.WorldEditSessionImpl;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.world.SignType;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertySlabType;
import net.minecraft.server.v1_16_R3.BlockStepAbstract;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R3.SoundCategory;
import net.minecraft.server.v1_16_R3.SoundEffectType;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.TileEntitySign;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldBorder;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.IntFunction;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectMethod<Float> SOUND_VOLUME = new ReflectMethod<>(SoundEffectType.class, "a");
    private static final ReflectMethod<Float> SOUND_PITCH = new ReflectMethod<>(SoundEffectType.class, "b");
    private static final ReflectField<Object> CHUNK_PACKET_BLOCK_CONTROLLER = new ReflectField<>(World.class,
            Object.class, "chunkPacketBlockController").removeFinal();

    private final SuperiorSkyblockPlugin plugin;

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        Block block = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState().getBlock();
        Location location = new Location(
                Bukkit.getWorld(chunkSnapshot.getWorldName()),
                (chunkSnapshot.getX() << 4) + x,
                y,
                (chunkSnapshot.getZ() << 4) + z
        );

        return Keys.of(KeyBlocksCache.getBlockKey(block), location);
    }

    @Override
    public void listenSpawner(Location location, IntFunction<Integer> delayChangeCallback) {
        TileEntityMobSpawner mobSpawner = NMSUtils.getTileEntityAt(location, TileEntityMobSpawner.class);
        if (mobSpawner == null || mobSpawner instanceof TileEntityMobSpawnerNotifier)
            return;

        World world = mobSpawner.getWorld();

        TileEntityMobSpawnerNotifier tileEntityMobSpawnerNotifier =
                new TileEntityMobSpawnerNotifier(mobSpawner, delayChangeCallback);

        world.removeTileEntity(mobSpawner.getPosition());
        world.setTileEntity(mobSpawner.getPosition(), tileEntityMobSpawnerNotifier);
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        if (!plugin.getSettings().isWorldBorders())
            return;

        Player player = superiorPlayer.asPlayer();
        org.bukkit.World world = superiorPlayer.getWorld();

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

        PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder,
                PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.d(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            IBlockData blockData =
                    NMSUtils.setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, combinedId, null, null);

            if (blockData != null) {
                world.getChunkProvider().flagDirty(blockPosition);
                if (CHUNK_PACKET_BLOCK_CONTROLLER.isValid()) {
                    world.chunkPacketBlockController.onBlockChange(world, blockPosition, blockData, Blocks.AIR.getBlockData(), 530);
                }
            }
        }
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return NMSCachedBlock.obtain(block);
    }

    @Override
    public boolean isWaterLogged(org.bukkit.block.Block block) {
        if (Materials.isWater(block.getType()))
            return true;

        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        return blockData instanceof BubbleColumn ||
                (blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged());
    }

    @Override
    public SignType getSignType(Object sign) {
        if (sign instanceof WallSign)
            return SignType.WALL_SIGN;
        else if (sign instanceof Sign)
            return SignType.STANDING_SIGN;
        else
            return SignType.UNKNOWN;
    }

    @Override
    public PistonPushReaction getPistonReaction(org.bukkit.block.Block block) {
        IBlockData blockData = ((CraftBlock) block).getNMS();
        return PistonPushReaction.values()[blockData.getPushReaction().ordinal()];
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block bukkitBlock) {
        return getDefaultAmount(((CraftBlock) bukkitBlock).getNMS());
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.BlockState bukkitBlockState) {
        return getDefaultAmount(((CraftBlockState) bukkitBlockState).getHandle());
    }

    private int getDefaultAmount(IBlockData blockData) {
        Block nmsBlock = blockData.getBlock();

        // Checks for double slabs
        if ((nmsBlock.a(TagsBlock.SLABS) || nmsBlock.a(TagsBlock.WOODEN_SLABS)) &&
                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.DOUBLE) {
            return 2;
        }

        return 1;
    }

    @Override
    public boolean canPlayerSuffocate(org.bukkit.block.Block bukkitBlock) {
        return !bukkitBlock.isPassable();
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
        World world = ((CraftWorld) location.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(location.getX(), location.getY(), location.getZ());
            world.triggerEffect(1501, blockPosition, 0);
        }
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.c(block.getX(), block.getY(), block.getZ());
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));
        }
    }

    @Override
    public void playPlaceSound(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();

        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPosition.MutableBlockPosition blockPosition = wrapper.getHandle();
            blockPosition.d(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SoundEffectType soundEffectType = world.getType(blockPosition).getStepSound();

            float volume = SOUND_VOLUME.isValid() ? SOUND_VOLUME.invoke(soundEffectType) : soundEffectType.getVolume();
            float pitch = SOUND_PITCH.isValid() ? SOUND_PITCH.invoke(soundEffectType) : soundEffectType.getPitch();

            world.playSound(null, blockPosition, soundEffectType.getPlaceSound(),
                    SoundCategory.BLOCKS, (volume + 1.0F) / 2.0F, pitch * 0.8F);

        }
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        try {
            return world.getMinHeight();
        } catch (Throwable error) {
            return 0;
        }
    }

    @Override
    public void removeAntiXray(org.bukkit.World bukkitWorld) {
        World world = ((CraftWorld) bukkitWorld).getHandle();
        if (CHUNK_PACKET_BLOCK_CONTROLLER.isValid())
            CHUNK_PACKET_BLOCK_CONTROLLER.set(world, ChunkPacketBlockController.NO_OPERATION_INSTANCE);
    }

    @Override
    public IslandsGenerator createGenerator(Dimension dimension) {
        return new IslandsGeneratorImpl(dimension);
    }

    @Override
    public WorldEditSession createEditSession(org.bukkit.World world) {
        return WorldEditSessionImpl.obtain(((CraftWorld) world).getHandle());
    }

    @Override
    public WorldEditSession createPartialEditSession(Dimension dimension) {
        return WorldEditSessionImpl.obtain(dimension);
    }

    @Override
    public ChunkReader createChunkReader(Chunk chunk) {
        return new ChunkReaderImpl(chunk);
    }

}
