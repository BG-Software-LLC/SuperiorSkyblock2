package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.nms.NMSWorld;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.generator.IslandsGeneratorImpl;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.spawners.MobSpawnerAbstractNotifier;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.world.KeyBlocksCache;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.world.WorldEditSessionImpl;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockDoubleStep;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import net.minecraft.server.v1_8_R3.WorldBorder;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.generator.ChunkGenerator;

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
            worldBorder = new WorldBorder();

            worldBorder.setWarningDistance(0);

            worldBorder.world = worldServer;
            worldBorder.setSize((islandSize * 2) + 1);

            World.Environment environment = world.getEnvironment();

            Location center = island.getCenter(environment);

            if (environment == World.Environment.NETHER) {
                worldBorder.setCenter(center.getX() * 8, center.getZ() * 8);
            } else {
                worldBorder.setCenter(center.getX(), center.getZ());
            }

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
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        // Doesn't exist
        return null;
    }

    @Override
    public void setBlock(Location location, int combinedId) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
        Chunk chunk = ((CraftWorld) location.getWorld()).getHandle().getChunkAtWorldCoords(blockPosition);
        return new byte[]{
                (byte) chunk.getBrightness(EnumSkyBlock.SKY, blockPosition),
                (byte) chunk.getBrightness(EnumSkyBlock.BLOCK, blockPosition),
        };
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if (tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = new NBTTagCompound();
        tileEntity.b(tileEntityCompound);

        tileEntityCompound.remove("x");
        tileEntityCompound.remove("y");
        tileEntityCompound.remove("z");

        return CompoundTag.fromNBT(tileEntityCompound);
    }

    @Override
    public boolean isWaterLogged(org.bukkit.block.Block block) {
        Material blockType = block.getType();
        return blockType == Material.WATER || blockType == Material.STATIONARY_WATER;
    }

    @Override
    public PistonPushReaction getPistonReaction(org.bukkit.block.Block block) {
        Block nmsBlock = CraftMagicNumbers.getBlock(block);
        return PistonPushReaction.values()[nmsBlock.getMaterial().getPushReaction()];
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        Location blockLocation = block.getLocation();
        IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(
                blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ()));
        net.minecraft.server.v1_8_R3.Block nmsBlock = blockData.getBlock();

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
    }

    @Override
    public void setSignLines(SignChangeEvent signChangeEvent, String[] lines) {
        // Not implemented - only needed for Paper 1.16+
    }

    @Override
    public void playGeneratorSound(Location location) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        world.makeSound(x + 0.5D, y + 0.5D, z + 0.5D, "random.fizz", 0.5F,
                2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; i++)
            world.addParticle(EnumParticle.SMOKE_LARGE, x + Math.random(), y + 1.2D, z + Math.random(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, net.minecraft.server.v1_8_R3.Block.getCombinedId(world.getType(blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Block.StepSound stepSound = world.getType(blockPosition).getBlock().stepSound;
        world.makeSound(blockPosition.getX() + 0.5F, blockPosition.getY() + 0.5F, blockPosition.getZ() + 0.5F,
                stepSound.getPlaceSound(), (stepSound.getVolume1() + 1.0F) / 2.0F, stepSound.getVolume2() * 0.8F);
    }

    @Override
    public int getMinHeight(World world) {
        return 0;
    }

    @Override
    public void removeAntiXray(World bukkitWorld) {
        // Doesn't exist in this version.
    }

    @Override
    public ChunkGenerator createGenerator(SuperiorSkyblockPlugin plugin) {
        return new IslandsGeneratorImpl(plugin);
    }

    @Override
    public WorldEditSession createEditSession(World world) {
        return new WorldEditSessionImpl(((CraftWorld) world).getHandle());
    }

}
