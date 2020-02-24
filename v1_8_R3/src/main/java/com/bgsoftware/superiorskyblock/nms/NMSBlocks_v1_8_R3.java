package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.GameProfile;
import gnu.trove.iterator.TLongShortIterator;
import gnu.trove.map.hash.TLongShortHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockLeaves;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityBanner;
import net.minecraft.server.v1_8_R3.TileEntityFlowerPot;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import net.minecraft.server.v1_8_R3.TileEntitySkull;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
public final class NMSBlocks_v1_8_R3 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Field chunkTickListField;

    static {
        try{
            chunkTickListField = World.class.getDeclaredField("chunkTickList");
            chunkTickListField.setAccessible(true);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setBlock(org.bukkit.Chunk bukkitChunk, Location location, int combinedId, BlockType blockType, Object... args) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(blockData.getBlock() instanceof BlockLeaves)
            blockData = blockData.set(BlockLeaves.DECAYABLE, false);

        if(blockData.getBlock().getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) {
            world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = location.getBlockY() >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, !chunk.world.worldProvider.o());

        int blockX = location.getBlockX() & 15, blockY = location.getBlockY() & 15, blockZ = location.getBlockZ() & 15;

        chunkSection.setType(blockX, blockY, blockZ, blockData);

        if(blockType != BlockType.BLOCK) {
            TileEntity tileEntity = world.getTileEntity(blockPosition);

            switch (blockType) {
                case BANNER:
                    //noinspection unchecked
                    setTileEntityBanner(tileEntity, (DyeColor) args[0], (List<Pattern>) args[1]);
                    break;
                case INVENTORY_HOLDER:
                    setTileEntityInventoryHolder(tileEntity, (org.bukkit.inventory.ItemStack[]) args[0]);
                    break;
                case FLOWER_POT:
                    setTileEntityFlowerPot(tileEntity, (org.bukkit.inventory.ItemStack) args[0]);
                    break;
                case SKULL:
                    setTileEntitySkull(tileEntity, (SkullType) args[0], (BlockFace) args[1], (String) args[2]);
                    break;
                case SIGN:
                    setTileEntitySign(tileEntity, (String[]) args[0]);
                    break;
                case SPAWNER:
                    setTileEntityMobSpawner(tileEntity, (EntityType) args[0]);
                    break;
            }
        }

    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, true, 65535);

        Location location = new Location(bukkitChunk.getWorld(), bukkitChunk.getX() << 4, 128, bukkitChunk.getZ() << 4);

        for(Entity player : bukkitChunk.getWorld().getNearbyEntities(location, 60, 128, 60)) {
            if(player instanceof Player)
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutMapChunk);
        }
    }

    @Override
    public void refreshLight(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        for(int i = 0; i < 16; i++) {
            ChunkSection chunkSection = chunk.getSections()[i];
            if(chunkSection == null) {
                chunkSection = new ChunkSection(i << 4, !chunk.world.worldProvider.o());
                chunk.getSections()[i] = chunkSection;
            }

            if (!chunk.world.worldProvider.o())
                Arrays.fill(chunkSection.getSkyLightArray().a(), (byte) 15);
        }

        chunk.initLighting();
    }

    @Override
    public org.bukkit.inventory.ItemStack getFlowerPot(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) world.getTileEntity(blockPosition);
        ItemStack itemStack = new ItemStack(tileEntityFlowerPot.b(), 1, tileEntityFlowerPot.c());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData =  world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public void setTileEntityBanner(Object objectTileEntityBanner, DyeColor dyeColor, List<Pattern> patterns) {
        TileEntityBanner tileEntityBanner = (TileEntityBanner) objectTileEntityBanner;
        //noinspection deprecation
        tileEntityBanner.color = dyeColor.getDyeData();
        tileEntityBanner.patterns = new NBTTagList();

        for(Pattern pattern : patterns){
            NBTTagCompound compound = new NBTTagCompound();
            //noinspection deprecation
            compound.setInt("Color", pattern.getColor().getDyeData());
            compound.setString("Pattern", pattern.getPattern().getIdentifier());
            tileEntityBanner.patterns.add(compound);
        }
    }

    @Override
    public void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, org.bukkit.inventory.ItemStack[] contents) {
        try{
            Field field = tileEntityInventoryHolder.getClass().getDeclaredField("items");
            field.setAccessible(true);
            ItemStack[] items = (ItemStack[]) field.get(tileEntityInventoryHolder);
            for(int i = 0; i < items.length && i < contents.length; i++){
                items[i] = CraftItemStack.asNMSCopy(contents[i]);
            }
        }catch(Exception ignored){ }
    }

    @Override
    public void setTileEntityFlowerPot(Object objectTileEntityFlowerPot, org.bukkit.inventory.ItemStack bukkitFlower) {
        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) objectTileEntityFlowerPot;
        ItemStack flower = CraftItemStack.asNMSCopy(bukkitFlower);
        tileEntityFlowerPot.a(flower.getItem(), flower.getData());
    }

    @Override
    public void setTileEntitySkull(Object objectTileEntitySkull, SkullType skullType, BlockFace rotation, String owner) {
        TileEntitySkull tileEntitySkull = (TileEntitySkull) objectTileEntitySkull;

        if (skullType == SkullType.PLAYER) {
            if(owner != null) {
                GameProfile gameProfile = MinecraftServer.getServer().getUserCache().getProfile(owner);
                if (gameProfile != null)
                    tileEntitySkull.setGameProfile(gameProfile);
            }
        } else {
            tileEntitySkull.setSkullType(skullType.ordinal() - 1);
        }

        switch(rotation) {
            case NORTH:
                tileEntitySkull.setRotation(0);
                break;
            case EAST:
                tileEntitySkull.setRotation(4);
                break;
            case SOUTH:
                tileEntitySkull.setRotation(8);
                break;
            case WEST:
                tileEntitySkull.setRotation(12);
                break;
            case NORTH_EAST:
                tileEntitySkull.setRotation(2);
                break;
            case NORTH_WEST:
                tileEntitySkull.setRotation(14);
                break;
            case SOUTH_EAST:
                tileEntitySkull.setRotation(6);
                break;
            case SOUTH_WEST:
                tileEntitySkull.setRotation(10);
                break;
            case WEST_NORTH_WEST:
                tileEntitySkull.setRotation(13);
                break;
            case NORTH_NORTH_WEST:
                tileEntitySkull.setRotation(15);
                break;
            case NORTH_NORTH_EAST:
                tileEntitySkull.setRotation(1);
                break;
            case EAST_NORTH_EAST:
                tileEntitySkull.setRotation(3);
                break;
            case EAST_SOUTH_EAST:
                tileEntitySkull.setRotation(5);
                break;
            case SOUTH_SOUTH_EAST:
                tileEntitySkull.setRotation(7);
                break;
            case SOUTH_SOUTH_WEST:
                tileEntitySkull.setRotation(9);
                break;
            case WEST_SOUTH_WEST:
                tileEntitySkull.setRotation(11);
                break;
        }
    }

    @Override
    public void setTileEntitySign(Object objectTileEntitySign, String[] lines) {
        TileEntitySign tileEntitySign = (TileEntitySign) objectTileEntitySign;
        IChatBaseComponent[] newLines = CraftSign.sanitizeLines(lines);
        System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
    }

    @Override
    public void setTileEntityMobSpawner(Object objectTileEntityMobSpawner, EntityType spawnedType) {
        MobSpawnerAbstract mobSpawner = ((TileEntityMobSpawner) objectTileEntityMobSpawner).getSpawner();
        mobSpawner.setMobName(spawnedType.name());
    }

    @Override
    public int tickWorld(org.bukkit.World world, int random) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        int globalRandomTickSpeed = worldServer.getGameRules().c("randomTickSpeed");
        List<Long> activeChunks = new ArrayList<>();
        List<Pair<BlockPosition, IBlockData>> blocksToTick = new ArrayList<>();

        try{
            TLongShortIterator iter = ((TLongShortHashMap) chunkTickListField.get(worldServer)).iterator();
            while(iter.hasNext()){
                iter.advance();
                activeChunks.add(iter.key());
            }
        }catch(Exception ignored){}

        for(long chunkCoord : activeChunks){
            int chunkX = World.keyToX(chunkCoord);
            int chunkZ = World.keyToZ(chunkCoord);

            if (!worldServer.chunkProviderServer.isChunkLoaded(chunkX, chunkZ))
                continue;

            boolean unloadQueueContains;

            try{
                unloadQueueContains = worldServer.chunkProviderServer.unloadQueue.contains(chunkX, chunkZ);
            }catch(Throwable ex){
                LongSet unloadQueue = (LongSet) Fields.CHUNK_PROVIDER_UNLOAD_QUEUE.get(worldServer.chunkProviderServer);
                assert unloadQueue != null;
                unloadQueueContains = unloadQueue.contains(LongHash.toLong(chunkX, chunkZ));
            }

            if(unloadQueueContains)
                continue;

            Chunk chunk = worldServer.getChunkAt(chunkX, chunkZ);

            Island island = plugin.getGrid().getIslandAt(chunk.bukkitChunk);

            int chunkRandomTickSpeed = (int) (globalRandomTickSpeed * (island == null ? 0 : island.getCropGrowthMultiplier() - 1));
            chunkX *= 16;
            chunkZ *= 16;

            if (chunkRandomTickSpeed > 0) {
                ChunkSection[] chunkSections = chunk.getSections();
                int i1 = chunkSections.length;
                for(ChunkSection chunkSection : chunkSections){
                    if (chunkSection != null && chunkSection.shouldTick()) {
                        for(int i = 0; i < chunkRandomTickSpeed; i++) {
                            random = random * 3 + 1013904223;
                            int factor = random >> 2;
                            int x = factor & 15;
                            int z = factor >> 8 & 15;
                            int y = factor >> 16 & 15;
                            IBlockData blockData = chunkSection.getType(x, y, z);
                            Block block = blockData.getBlock();
                            if (block.isTicking() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(block).name())) {
                                blocksToTick.add(new Pair<>(new BlockPosition(x + chunkX, y + chunkSection.getYPosition(), z + chunkZ), blockData));
                            }
                        }
                    }
                }
            }

        }

        Executor.sync(() -> blocksToTick.forEach(pair ->
                pair.getValue().getBlock().a(worldServer, pair.getKey(), pair.getValue(), ThreadLocalRandom.current())));

        return random;
    }

}
