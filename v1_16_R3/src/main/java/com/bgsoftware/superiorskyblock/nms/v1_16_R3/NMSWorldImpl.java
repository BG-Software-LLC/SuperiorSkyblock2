package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.listener.SignsListener;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.generator.IslandsGeneratorImpl;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.spawners.MobSpawnerAbstractNotifier;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertySlabType;
import net.minecraft.server.v1_16_R3.BlockStateBoolean;
import net.minecraft.server.v1_16_R3.BlockStateEnum;
import net.minecraft.server.v1_16_R3.BlockStateInteger;
import net.minecraft.server.v1_16_R3.BlockStepAbstract;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.EnumSkyBlock;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IBlockState;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.LightEngine;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R3.SoundCategory;
import net.minecraft.server.v1_16_R3.SoundEffectType;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.TileEntitySign;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldBorder;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectMethod<Object> LINES_SIGN_CHANGE_EVENT = new ReflectMethod<>(SignChangeEvent.class, "lines");
    private static final ReflectMethod<Float> SOUND_VOLUME = new ReflectMethod<>(SoundEffectType.class, "a");
    private static final ReflectMethod<Float> SOUND_PITCH = new ReflectMethod<>(SoundEffectType.class, "b");
    private static final ReflectField<Object> CHUNK_PACKET_BLOCK_CONTROLLER = new ReflectField<>(World.class,
            Object.class, "chunkPacketBlockController").removeFinal();
    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<SignsListener> signsListener;

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.signsListener = plugin.getListener(SignsListener.class);
    }

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (Block.getCombinedId(blockData) >> 12 & 15);

        Location location = new Location(
                Bukkit.getWorld(chunkSnapshot.getWorldName()),
                (chunkSnapshot.getX() << 4) + x,
                y,
                (chunkSnapshot.getZ() << 4) + z
        );

        return KeyImpl.of(KeyImpl.of(type, data), location);
    }

    @Override
    public void listenSpawner(CreatureSpawner creatureSpawner, IntFunction<Integer> delayChangeCallback) {
        Location location = creatureSpawner.getLocation();
        org.bukkit.World world = location.getWorld();

        if (world == null)
            return;

        WorldServer worldServer = ((CraftWorld) world).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntity mobSpawner = worldServer.getTileEntity(blockPosition);

        if (!(mobSpawner instanceof TileEntityMobSpawner))
            return;

        MobSpawnerAbstract mobSpawnerAbstract = ((TileEntityMobSpawner) mobSpawner).getSpawner();

        if (!(mobSpawnerAbstract instanceof MobSpawnerAbstractNotifier)) {
            MobSpawnerAbstractNotifier mobSpawnerAbstractNotifier = new MobSpawnerAbstractNotifier(mobSpawnerAbstract, delayChangeCallback);
            MOB_SPAWNER_ABSTRACT.set(mobSpawner, mobSpawnerAbstractNotifier);
            mobSpawnerAbstractNotifier.updateDelay();
        }
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if (!plugin.getSettings().isWorldBorders())
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            Player player = superiorPlayer.asPlayer();
            org.bukkit.World world = superiorPlayer.getWorld();

            if (world == null || player == null)
                return;

            WorldServer worldServer = ((CraftWorld) superiorPlayer.getWorld()).getHandle();

            WorldBorder worldBorder;

            if (disabled || island == null || (!plugin.getSettings().getSpawn().isWorldBorder() && island.isSpawn())) {
                worldBorder = worldServer.getWorldBorder();
            } else {
                worldBorder = new WorldBorder();

                worldBorder.setWarningDistance(0);

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
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<SchematicBlock> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        for (SchematicBlock blockData : blockDataList) {
            NMSUtils.setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getTileEntityData());
        }
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        NMSUtils.setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, combinedId, null, null);
        NMSUtils.sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    @Override
    public ICachedBlock cacheBlock(org.bukkit.block.Block block) {
        return new NMSCachedBlock(block);
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        net.minecraft.server.v1_16_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = world.getType(blockPosition);
        CompoundTag compoundTag = null;

        for (Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.getStateMap().entrySet()) {
            if (compoundTag == null)
                compoundTag = new CompoundTag();

            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = entry.getKey().getName();

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
        net.minecraft.server.v1_16_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
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
        if (Materials.isWater(block.getType()))
            return true;

        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        return blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        IBlockData blockData = ((CraftBlock) block).getNMS();
        Block nmsBlock = blockData.getBlock();

        // Checks for double slabs
        if ((nmsBlock.a(TagsBlock.SLABS) || nmsBlock.a(TagsBlock.WOODEN_SLABS)) &&
                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.DOUBLE) {
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
                strippedLines[i] = Formatters.STRIP_COLOR_FORMATTER.format(lines[i]);

            IChatBaseComponent[] newLines;

            if (signsListener.get().shouldReplaceSignLines(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
        }
    }

    @Override
    public void setSignLines(SignChangeEvent signChangeEvent, String[] lines) {
        if (LINES_SIGN_CHANGE_EVENT.isValid()) {
            for (int i = 0; i < lines.length; i++)
                //noinspection deprecation
                signChangeEvent.setLine(i, lines[i]);
        }
    }

    @Override
    public void playGeneratorSound(Location location) {
        net.minecraft.server.v1_16_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        world.triggerEffect(1501, blockPosition, 0);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        net.minecraft.server.v1_16_R3.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_16_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        SoundEffectType soundEffectType = world.getType(blockPosition).getStepSound();

        float volume = SOUND_VOLUME.isValid() ? SOUND_VOLUME.invoke(soundEffectType) : soundEffectType.getVolume();
        float pitch = SOUND_PITCH.isValid() ? SOUND_PITCH.invoke(soundEffectType) : soundEffectType.getPitch();

        world.playSound(null, blockPosition, soundEffectType.getPlaceSound(),
                SoundCategory.BLOCKS, (volume + 1.0F) / 2.0F, pitch * 0.8F);
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
    public ChunkGenerator createGenerator(SuperiorSkyblockPlugin plugin) {
        return new IslandsGeneratorImpl(plugin);
    }

}
