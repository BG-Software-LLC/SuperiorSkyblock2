package com.bgsoftware.superiorskyblock.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectField;
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
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms.NMSCachedBlock;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.generator.IslandsGeneratorImpl;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.spawners.TickingSpawnerBlockEntityNotifier;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.vibration.IslandVibrationUser;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.world.ChunkReaderImpl;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.world.WorldEditSessionImpl;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.world.SignType;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectField<List<TickingBlockEntity>> LEVEL_BLOCK_ENTITY_TICKERS = initializeLevelBlockEntityTickersField();
    private static final ReflectField<VibrationSystem.User> SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER = new ReflectField<VibrationSystem.User>(
            SculkSensorBlockEntity.class, VibrationSystem.User.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();
    private static final ReflectField<Object> CHUNK_PACKET_BLOCK_CONTROLLER = new ReflectField<>(Level.class,
            Object.class, "chunkPacketBlockController")
            .removeFinal();

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
        SpawnerBlockEntity spawnerBlockEntity = NMSUtils.getBlockEntityAt(location, SpawnerBlockEntity.class);
        if (spawnerBlockEntity == null)
            return;

        ServerLevel serverLevel = (ServerLevel) spawnerBlockEntity.getLevel();

        List<TickingBlockEntity> blockEntityTickers = LEVEL_BLOCK_ENTITY_TICKERS.isValid() ?
                LEVEL_BLOCK_ENTITY_TICKERS.get(serverLevel) : serverLevel.blockEntityTickers;

        Iterator<TickingBlockEntity> blockEntityTickersIterator = blockEntityTickers.iterator();
        List<TickingBlockEntity> tickersToAdd = new LinkedList<>();

        while (blockEntityTickersIterator.hasNext()) {
            TickingBlockEntity tickingBlockEntity = blockEntityTickersIterator.next();
            if (tickingBlockEntity.getPos().equals(spawnerBlockEntity.getBlockPos()) &&
                    !(tickingBlockEntity instanceof TickingSpawnerBlockEntityNotifier)) {
                blockEntityTickersIterator.remove();
                tickersToAdd.add(new TickingSpawnerBlockEntityNotifier(spawnerBlockEntity, tickingBlockEntity, delayChangeCallback));
            }
        }

        if (!tickersToAdd.isEmpty())
            blockEntityTickers.addAll(tickersToAdd);
    }

    @Override
    public void replaceSculkSensorListener(Island island, Location location) {
        SculkSensorBlockEntity sculkSensorBlockEntity = NMSUtils.getBlockEntityAt(location, SculkSensorBlockEntity.class);
        if (sculkSensorBlockEntity == null || sculkSensorBlockEntity.getVibrationUser() instanceof IslandVibrationUser)
            return;

        SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER.set(sculkSensorBlockEntity, new IslandVibrationUser(island, sculkSensorBlockEntity));
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

        ServerLevel serverLevel = ((CraftWorld) world).getHandle();

        WorldBorder worldBorder;

        if (disabled || island == null || (!plugin.getSettings().getSpawn().isWorldBorder() && island.isSpawn())) {
            worldBorder = serverLevel.getWorldBorder();
        } else {
            Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
            if (dimension == null)
                return;

            Location center = island.getCenter(dimension);

            worldBorder = new WorldBorder();
            worldBorder.world = serverLevel;
            worldBorder.setWarningBlocks(0);
            worldBorder.setCenter(center.getX(), center.getZ());

            switch (superiorPlayer.getBorderColor()) {
                case BLUE -> {
                    worldBorder.setSize((islandSize * 2) + 1D);
                }
                case GREEN -> {
                    worldBorder.setSize((islandSize * 2) + 1.001D);
                    worldBorder.lerpSizeBetween(worldBorder.getSize() - 0.001D, worldBorder.getSize(), Long.MAX_VALUE);
                }
                case RED -> {
                    worldBorder.setSize((islandSize * 2) + 1D);
                    worldBorder.lerpSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 0.001D, Long.MAX_VALUE);
                }
            }
        }

        ClientboundInitializeBorderPacket initializeBorderPacket = new ClientboundInitializeBorderPacket(worldBorder);
        ((CraftPlayer) player).getHandle().connection.send(initializeBorderPacket);
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            BlockState blockState =
                    NMSUtils.setBlock(serverLevel.getChunkAt(blockPos), blockPos, combinedId, null, null);

            if (blockState != null) {
                serverLevel.getChunkSource().blockChanged(blockPos);
                if (CHUNK_PACKET_BLOCK_CONTROLLER.isValid()) {
                    serverLevel.chunkPacketBlockController.onBlockChange(serverLevel, blockPos, blockState, Blocks.AIR.defaultBlockState(), 530, 512);
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

        BlockData blockData = block.getBlockData();

        return blockData instanceof BubbleColumn ||
                (blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged());
    }

    @Override
    public SignType getSignType(Object sign) {
        if (sign instanceof WallSign)
            return SignType.WALL_SIGN;
        else if (sign instanceof Sign)
            return SignType.STANDING_SIGN;
        else if (sign instanceof HangingSign)
            return SignType.HANGING_SIGN;
        else if (sign instanceof WallHangingSign)
            return SignType.HANGING_WALL_SIGN;
        else
            return SignType.UNKNOWN;
    }

    @Override
    public PistonPushReaction getPistonReaction(org.bukkit.block.Block block) {
        BlockState blockState = ((CraftBlock) block).getNMS();
        return PistonPushReaction.values()[blockState.getPistonPushReaction().ordinal()];
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block bukkitBlock) {
        return getDefaultAmount(((CraftBlock) bukkitBlock).getNMS());
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.BlockState bukkitBlockState) {
        return getDefaultAmount(((CraftBlockState) bukkitBlockState).getHandle());
    }

    private int getDefaultAmount(BlockState blockState) {
        Block block = blockState.getBlock();
        return NMSUtils.isDoubleBlock(block, blockState) ? 2 : 1;
    }

    @Override
    public boolean canPlayerSuffocate(org.bukkit.block.Block bukkitBlock) {
        return !bukkitBlock.isPassable();
    }

    @Override
    public void placeSign(Island island, Location location) {
        SignBlockEntity signBlockEntity = NMSUtils.getBlockEntityAt(location, SignBlockEntity.class);
        if (signBlockEntity == null)
            return;

        String[] lines = new String[4];
        Component[] messages = signBlockEntity.getFrontText().getMessages(false);
        System.arraycopy(CraftSign.revertComponents(messages), 0, lines, 0, lines.length);
        String[] strippedLines = new String[4];
        for (int i = 0; i < 4; i++)
            strippedLines[i] = Formatters.STRIP_COLOR_FORMATTER.format(lines[i]);

        Component[] newLines;

        IslandSigns.Result result = IslandSigns.handleSignPlace(island.getOwner(), location, strippedLines, false);
        if (result.isCancelEvent()) {
            newLines = CraftSign.sanitizeLines(strippedLines);
        } else {
            newLines = CraftSign.sanitizeLines(lines);
        }

        System.arraycopy(newLines, 0, messages, 0, 4);
    }

    @Override
    public void playGeneratorSound(Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();

        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            serverLevel.levelEvent(1501, blockPos, 0);
        }
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set(block.getX(), block.getY(), block.getZ());
            serverLevel.levelEvent(null, 2001, blockPos, Block.getId(serverLevel.getBlockState(blockPos)));
        }
    }

    @Override
    public void playPlaceSound(Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        try (ObjectsPools.Wrapper<BlockPos.MutableBlockPos> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            BlockPos.MutableBlockPos blockPos = wrapper.getHandle();
            blockPos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            SoundType soundType = serverLevel.getBlockState(blockPos).getSoundType();

            serverLevel.playSound(null, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        }
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return world.getMinHeight();
    }

    @Override
    public void removeAntiXray(org.bukkit.World bukkitWorld) {
        if (!CHUNK_PACKET_BLOCK_CONTROLLER.isValid())
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        CHUNK_PACKET_BLOCK_CONTROLLER.set(serverLevel, ChunkPacketBlockController.NO_OPERATION_INSTANCE);
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

    private static ReflectField<List<TickingBlockEntity>> initializeLevelBlockEntityTickersField() {
        ReflectField<List<TickingBlockEntity>> field = new ReflectField<>(
                Level.class, List.class, Modifier.PROTECTED | Modifier.FINAL, 1);

        if (!field.isValid())
            field = new ReflectField<>(Level.class, List.class, Modifier.PUBLIC | Modifier.FINAL, 1);

        return field;
    }

}
