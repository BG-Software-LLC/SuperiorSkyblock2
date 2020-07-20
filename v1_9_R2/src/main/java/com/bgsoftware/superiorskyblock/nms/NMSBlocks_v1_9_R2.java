package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.BiomeBase;
import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockLeaves;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Blocks;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_9_R2.ChunkRegionLoader;
import net.minecraft.server.v1_9_R2.ChunkSection;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.IChunkLoader;
import net.minecraft.server.v1_9_R2.INamableTileEntity;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.PacketPlayOutBlockChange;
import net.minecraft.server.v1_9_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_9_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.TileEntityBanner;
import net.minecraft.server.v1_9_R2.TileEntityBrewingStand;
import net.minecraft.server.v1_9_R2.TileEntityChest;
import net.minecraft.server.v1_9_R2.TileEntityDispenser;
import net.minecraft.server.v1_9_R2.TileEntityFlowerPot;
import net.minecraft.server.v1_9_R2.TileEntityFurnace;
import net.minecraft.server.v1_9_R2.TileEntityHopper;
import net.minecraft.server.v1_9_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_9_R2.TileEntitySign;
import net.minecraft.server.v1_9_R2.TileEntitySkull;
import net.minecraft.server.v1_9_R2.World;
import net.minecraft.server.v1_9_R2.WorldServer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftSign;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_9_R2.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSBlocks_v1_9_R2 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();

    @Override
    public void setBlock(org.bukkit.Chunk bukkitChunk, Location location, int combinedId, CompoundTag statesTag, BlockType blockType, Object... args) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(blockData.getBlock() instanceof BlockLeaves)
            blockData = blockData.set(BlockLeaves.DECAYABLE, false);

        if(blockData.getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) {
            world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = location.getBlockY() >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, !chunk.world.worldProvider.m());

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
                    setTileEntityInventoryHolder(tileEntity, (org.bukkit.inventory.ItemStack[]) args[0], (String) args[1]);
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
    public void setBlock(Location location, Material material, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        //noinspection deprecation
        int combinedId = material.getId() + (data << 12);
        setBlock(location.getChunk(), location, combinedId, null, BlockType.BLOCK);

        AxisAlignedBB bb = new AxisAlignedBB(blockPosition.getX() - 60, 0, blockPosition.getZ() - 60,
                blockPosition.getX() + 60, 256, blockPosition.getZ() + 60);

        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(world, blockPosition);

        for(Entity entity : world.getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutBlockChange);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, 65535);

        AxisAlignedBB bb = new AxisAlignedBB((bukkitChunk.getX() << 4) - 60, 0, (bukkitChunk.getZ() << 4) - 60,
                (bukkitChunk.getX() << 4) + 60, 256, (bukkitChunk.getZ() << 4) + 60);

        for(Entity entity : chunk.getWorld().getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutMapChunk);
        }
    }

    @Override
    public void refreshLight(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        for(int i = 0; i < 16; i++) {
            ChunkSection chunkSection = chunk.getSections()[i];
            if(chunkSection == null) {
                chunkSection = new ChunkSection(i << 4, !chunk.world.worldProvider.m());
                chunk.getSections()[i] = chunkSection;
            }

            if (!chunk.world.worldProvider.m())
                Arrays.fill(chunkSection.getSkyLightArray().asBytes(), (byte) 15);
        }

        chunk.initLighting();
    }

    @Override
    public org.bukkit.inventory.ItemStack getFlowerPot(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) world.getTileEntity(blockPosition);
        ItemStack itemStack = new ItemStack(tileEntityFlowerPot.d(), 1, tileEntityFlowerPot.e());
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
    public void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, org.bukkit.inventory.ItemStack[] contents, String name) {
        ItemStack[] items = getItems(tileEntityInventoryHolder);
        for(int i = 0; i < items.length && i < contents.length; i++){
            items[i] = CraftItemStack.asNMSCopy(contents[i]);
        }
        setName(tileEntityInventoryHolder, name);
    }

    @Override
    public void setTileEntityFlowerPot(Object objectTileEntityFlowerPot, org.bukkit.inventory.ItemStack bukkitFlower) {
        if(bukkitFlower == null || bukkitFlower.getType() == Material.AIR)
            return;

        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) objectTileEntityFlowerPot;
        ItemStack flower = CraftItemStack.asNMSCopy(bukkitFlower);
        tileEntityFlowerPot.a(flower.getItem(), flower.getData());
    }

    @Override
    public void setTileEntitySkull(Object objectTileEntitySkull, SkullType skullType, BlockFace rotation, String owner) {
        TileEntitySkull tileEntitySkull = (TileEntitySkull) objectTileEntitySkull;

        if (skullType == SkullType.PLAYER) {
            if(owner != null) {
                //noinspection deprecation
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
    public org.bukkit.Chunk getChunkIfLoaded(org.bukkit.World bukkitWorld, int x, int z) {
        Chunk chunk = ((CraftWorld) bukkitWorld).getHandle().getChunkProviderServer().getChunkIfLoaded(x, z);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> calculateChunk(org.bukkit.World bukkitWorld, int chunkX, int chunkZ) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);

        CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> completableFuture = new CompletableFuture<>();
        ChunkPosition chunkPosition = ChunkPosition.of(bukkitWorld, chunkX, chunkZ);

        runActionOnChunk(bukkitWorld, chunkCoords, false, chunk -> {
            KeyMap<Integer> blockCounts = new KeyMap<>();
            Set<Location> spawnersLocations = new HashSet<>();

            for(ChunkSection chunkSection : chunk.getSections()){
                if(chunkSection != null && chunkSection != Chunk.a){
                    for (BlockPosition bp : BlockPosition.b(new BlockPosition(0, 0, 0), new BlockPosition(15, 15, 15))) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(bukkitWorld, (chunkX << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkZ << 4) + bp.getZ());
                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            short data = (short) blockData.getBlock().toLegacyData(blockData);
                            Key blockKey = Key.of(type, data, location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (type == Material.MOB_SPAWNER) {
                                spawnersLocations.add(location);
                            }
                        }
                    }
                }
            }

            completableFuture.complete(new BiPair<>(chunkPosition, blockCounts, spawnersLocations));
        }, null);

        return completableFuture;
    }

    @Override
    public void deleteChunk(Island island, org.bukkit.World bukkitWorld, int chunkX, int chunkZ) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        runActionOnChunk(bukkitWorld, chunkCoords, true, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);
            Arrays.fill(chunk.entitySlices, new UnsafeList<>());

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::s);
            chunk.tileEntities.clear();

            ChunksTracker.markEmpty(island, ChunkPosition.of(bukkitWorld, chunkX, chunkZ), false);
        }, chunk -> refreshChunk(chunk.bukkitChunk));
    }

    @Override
    public void setChunkBiome(org.bukkit.World bukkitWorld, int chunkX, int chunkZ, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        runActionOnChunk(bukkitWorld, chunkCoords, true, chunk -> {
            byte biomeBase = (byte) BiomeBase.REGISTRY_ID.a(CraftBlock.biomeToBiomeBase(biome));
            Arrays.fill(chunk.getBiomeIndex(), biomeBase);
        },
        chunk -> {
            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkX, chunkZ);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer, Consumer<Chunk> updateChunk){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(bukkitWorld.getUID(), uuid -> (IChunkLoader) Fields.CHUNK_PROVIDER_CHUNK_LOADER.get(world.getChunkProvider()));

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(updateChunk != null)
                updateChunk.accept(chunk);
        }

        else{
            Executor.async(() -> {
                try{
                    Object[] chunkData = ((ChunkRegionLoader) chunkLoader).loadChunk(world, chunkCoords.x, chunkCoords.z);

                    if(chunkData == null)
                        return;

                    Chunk loadedChunk = (Chunk) chunkData[0];
                    chunkConsumer.accept(loadedChunk);
                    if(saveChunk)
                        chunkLoader.a(world, loadedChunk);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
    }

    @Override
    public int tickIslands(int random) {
        List<Pair<Island, List<org.bukkit.Chunk>>> activeChunks = new ArrayList<>();
        List<BiPair<WorldServer, BlockPosition, IBlockData>> blocksToTick = new ArrayList<>();
        org.bukkit.World normalWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.NORMAL),
                netherWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.NETHER),
                endWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.THE_END);
        int[] globalRandomTickSpeeds = new int[] {
                normalWorld == null ? 0 : ((CraftWorld) normalWorld).getHandle().getGameRules().c("randomTickSpeed"),
                netherWorld == null ? 0 : ((CraftWorld) netherWorld).getHandle().getGameRules().c("randomTickSpeed"),
                endWorld == null ? 0 : ((CraftWorld) endWorld).getHandle().getGameRules().c("randomTickSpeed")
        };

        plugin.getGrid().getIslands().stream()
                .filter(island -> island.getCropGrowthMultiplier() > 1 && !island.getAllPlayersInside().isEmpty())
                .forEach(island -> activeChunks.add(new Pair<>(island, island.getLoadedChunks(true, true))));

        for(Pair<Island, List<org.bukkit.Chunk>> chunkPair : activeChunks){
            Island island = chunkPair.getKey();
            double islandCropGrowthMultiplier = island == null ? 0 : island.getCropGrowthMultiplier() - 1;

            for(org.bukkit.Chunk bukkitChunk : chunkPair.getValue()) {
                Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
                WorldServer chunkWorld = (WorldServer) chunk.world;
                int chunkRandomTickSpeed = (int) (globalRandomTickSpeeds[chunkWorld.getWorld().getEnvironment().ordinal()] * islandCropGrowthMultiplier);

                int chunkX = chunk.locX * 16;
                int chunkZ = chunk.locZ * 16;

                if (chunkRandomTickSpeed > 0) {
                    for (ChunkSection chunkSection : chunk.getSections()) {
                        if (chunkSection != Chunk.a && chunkSection.shouldTick()) {
                            for (int i = 0; i < chunkRandomTickSpeed; i++) {
                                random = random * 3 + 1013904223;
                                int factor = random >> 2;
                                int x = factor & 15;
                                int z = factor >> 8 & 15;
                                int y = factor >> 16 & 15;
                                IBlockData blockData = chunkSection.getType(x, y, z);
                                Block block = blockData.getBlock();
                                if (block.isTicking() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(block).name())) {
                                    blocksToTick.add(new BiPair<>(chunkWorld, new BlockPosition(x + chunkX, y + chunkSection.getYPosition(), z + chunkZ), blockData));
                                }
                            }
                        }
                    }
                }
            }
        }

        Executor.sync(() -> blocksToTick.forEach(pair ->
                pair.getZ().getBlock().a(pair.getX(), pair.getY(), pair.getZ(), ThreadLocalRandom.current())));

        return random;
    }

    @Override
    public String getTileName(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return tileEntity instanceof INamableTileEntity && ((INamableTileEntity) tileEntity).hasCustomName() ? ((INamableTileEntity) tileEntity).getName() : "";
    }

    private ItemStack[] getItems(Object tileEntityInventoryHolder){
        if(tileEntityInventoryHolder instanceof TileEntityChest){
            return ((TileEntityChest) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityDispenser){
            return ((TileEntityDispenser) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityBrewingStand){
            return ((TileEntityBrewingStand) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityFurnace){
            return ((TileEntityFurnace) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityHopper){
            return ((TileEntityHopper) tileEntityInventoryHolder).getContents();
        }

        SuperiorSkyblockPlugin.log("&cCouldn't find inventory holder for class: " + tileEntityInventoryHolder.getClass() + " - contact @Ome_R!");

        return new ItemStack[0];
    }

    private void setName(Object tileEntityInventoryHolder, String name){
        if(tileEntityInventoryHolder instanceof TileEntityChest){
            ((TileEntityChest) tileEntityInventoryHolder).a(name);
        }
        else if(tileEntityInventoryHolder instanceof TileEntityDispenser){
            ((TileEntityDispenser) tileEntityInventoryHolder).a(name);
        }
        else if(tileEntityInventoryHolder instanceof TileEntityBrewingStand){
            ((TileEntityBrewingStand) tileEntityInventoryHolder).a(name);
        }
        else if(tileEntityInventoryHolder instanceof TileEntityFurnace){
            ((TileEntityFurnace) tileEntityInventoryHolder).a(name);
        }
        else if(tileEntityInventoryHolder instanceof TileEntityHopper){
            ((TileEntityHopper) tileEntityInventoryHolder).a(name);
        }
    }

}
