package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockFlowerPot;
import net.minecraft.server.v1_15_R1.BlockLeaves;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkProviderServer;
import net.minecraft.server.v1_15_R1.ChunkSection;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumColor;
import net.minecraft.server.v1_15_R1.GameRules;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NonNullList;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PlayerChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityBanner;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_15_R1.TileEntitySign;
import net.minecraft.server.v1_15_R1.TileEntitySkull;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSBlocks_v1_15_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Method outsideOfRangeMethod;

    static {
        try {
            outsideOfRangeMethod = PlayerChunkMap.class.getDeclaredMethod("isOutsideOfRange", ChunkCoordIntPair.class);
            outsideOfRangeMethod.setAccessible(true);
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
            blockData = blockData.set(BlockLeaves.PERSISTENT, true);

        if(blockData.getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) {
            world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = location.getBlockY() >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4);

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, blockData, false);

        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
        chunkProviderServer.getLightEngine().a(blockPosition);
        chunkProviderServer.flagDirty(blockPosition);

        if(blockType != BlockType.BLOCK && blockType != BlockType.FLOWER_POT) {
            TileEntity tileEntity = world.getTileEntity(blockPosition);

            switch (blockType) {
                case BANNER:
                    //noinspection unchecked
                    setTileEntityBanner(tileEntity, (DyeColor) args[0], (List<Pattern>) args[1]);
                    break;
                case INVENTORY_HOLDER:
                    setTileEntityInventoryHolder(tileEntity, (org.bukkit.inventory.ItemStack[]) args[0]);
                    break;
                case SKULL:
                    setTileEntitySkull(tileEntity, null, (BlockFace) args[1], (String) args[2]);
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

        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, 65535);

        Location location = new Location(bukkitChunk.getWorld(), bukkitChunk.getX() << 4, 128, bukkitChunk.getZ() << 4);

        for(Entity player : bukkitChunk.getWorld().getNearbyEntities(location, 60, 128, 60)) {
            if(player instanceof Player)
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutMapChunk);
        }
    }

    @Override
    public void refreshLight(org.bukkit.Chunk chunk) {

    }

    @Override
    public org.bukkit.inventory.ItemStack getFlowerPot(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        BlockFlowerPot blockFlowerPot = (BlockFlowerPot) world.getType(blockPosition).getBlock();
        Block flower = (Block) Fields.BLOCK_FLOWER_POT_CONTENT.get(blockFlowerPot);
        ItemStack itemStack = new ItemStack(flower.getItem(), 1);
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
        tileEntityBanner.color = EnumColor.fromColorIndex(dyeColor.getDyeData());
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
            //noinspection unchecked
            NonNullList<ItemStack> items = (NonNullList<ItemStack>) field.get(tileEntityInventoryHolder);
            for(int i = 0; i < items.size() && i < contents.length; i++){
                items.set(i, CraftItemStack.asNMSCopy(contents[i]));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void setTileEntityFlowerPot(Object objectTileEntityFlowerPot, org.bukkit.inventory.ItemStack bukkitFlower) {

    }

    @Override
    @SuppressWarnings("deprecation")
    public void setTileEntitySkull(Object objectTileEntitySkull, SkullType skullType, BlockFace rotation, String owner) {
        TileEntitySkull tileEntitySkull = (TileEntitySkull) objectTileEntitySkull;

        if(owner != null && !owner.isEmpty()){
            GameProfile gameProfile = MinecraftServer.getServer().getUserCache().getProfile(owner);
            if(gameProfile != null)
                tileEntitySkull.setGameProfile(gameProfile);
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
        //noinspection deprecation, OptionalGetWithoutIsPresent
        mobSpawner.setMobName(EntityTypes.a(spawnedType.getName()).get());
    }

    @Override
    public int tickWorld(org.bukkit.World world, int random) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        int globalRandomTickSpeed = worldServer.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        List<PlayerChunk> activeChunks = new ArrayList<>();
        List<Pair<BlockPosition, IBlockData>> blocksToTick = new ArrayList<>();

        ChunkProviderServer chunkProvider = worldServer.getChunkProvider();

        PlayerChunkMap playerChunkMap = chunkProvider.playerChunkMap;

        try{
            activeChunks.addAll(playerChunkMap.visibleChunks.values());
        }catch(Throwable ignored){}

        for(PlayerChunk playerChunk : activeChunks){
            Optional<Chunk> optional = playerChunk.b().getNow(PlayerChunk.UNLOADED_CHUNK).left();

            if (!optional.isPresent())
                continue;

            Chunk chunk = optional.get();
            playerChunk.a(chunk);
            ChunkCoordIntPair chunkCoord = playerChunk.i();

            try {
                if ((Boolean) outsideOfRangeMethod.invoke(playerChunkMap, chunkCoord))
                    continue;
            }catch(Exception ex){
                continue;
            }

            Island island = plugin.getGrid().getIslandAt(chunk.bukkitChunk);

            int chunkRandomTickSpeed = (int) (globalRandomTickSpeed * (island == null ? 0 : island.getCropGrowthMultiplier() - 1));
            int chunkX = chunkCoord.d();
            int chunkZ = chunkCoord.e();

            if (chunkRandomTickSpeed > 0) {
                ChunkSection[] chunkSections = chunk.getSections();
                int i1 = chunkSections.length;
                for(ChunkSection chunkSection : chunkSections){
                    if (chunkSection != Chunk.a && chunkSection.d()) {
                        for(int i = 0; i < chunkRandomTickSpeed; i++) {
                            random = random * 3 + 1013904223;
                            int factor = random >> 2;
                            int x = factor & 15;
                            int z = factor >> 8 & 15;
                            int y = factor >> 16 & 15;
                            IBlockData blockData = chunkSection.getType(x, y, z);
                            if (blockData.q() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(blockData.getBlock()).name())) {
                                blocksToTick.add(new Pair<>(new BlockPosition(x + chunkX, y + chunkSection.getYPosition(), z + chunkZ), blockData));
                            }
                        }
                    }
                }
            }
        }

        Executor.sync(() -> blocksToTick.forEach(pair ->
                pair.getValue().b(worldServer, pair.getKey(), ThreadLocalRandom.current())));

        return random;
    }

}
