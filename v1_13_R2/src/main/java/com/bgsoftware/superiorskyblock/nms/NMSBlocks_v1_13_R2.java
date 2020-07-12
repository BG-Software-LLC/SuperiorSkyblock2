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
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BiomeBase;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockFlowerPot;
import net.minecraft.server.v1_13_R2.BlockLeaves;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.Chunk;
import net.minecraft.server.v1_13_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R2.ChunkSection;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.EnumColor;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.IChunkAccess;
import net.minecraft.server.v1_13_R2.IChunkLoader;
import net.minecraft.server.v1_13_R2.INamableTileEntity;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NonNullList;
import net.minecraft.server.v1_13_R2.PacketPlayOutBlockChange;
import net.minecraft.server.v1_13_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_13_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import net.minecraft.server.v1_13_R2.ProtoChunk;
import net.minecraft.server.v1_13_R2.TileEntity;
import net.minecraft.server.v1_13_R2.TileEntityBanner;
import net.minecraft.server.v1_13_R2.TileEntityBrewingStand;
import net.minecraft.server.v1_13_R2.TileEntityChest;
import net.minecraft.server.v1_13_R2.TileEntityDispenser;
import net.minecraft.server.v1_13_R2.TileEntityFurnace;
import net.minecraft.server.v1_13_R2.TileEntityHopper;
import net.minecraft.server.v1_13_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R2.TileEntityShulkerBox;
import net.minecraft.server.v1_13_R2.TileEntitySign;
import net.minecraft.server.v1_13_R2.TileEntitySkull;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftSign;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_13_R2.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSBlocks_v1_13_R2 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.g());

        int blockX = location.getBlockX() & 15, blockY = location.getBlockY() & 15, blockZ = location.getBlockZ() & 15;

        chunkSection.setType(blockX, blockY, blockZ, blockData);

        if(blockType != BlockType.BLOCK && blockType != BlockType.FLOWER_POT) {
            TileEntity tileEntity = world.getTileEntity(blockPosition);

            switch (blockType) {
                case BANNER:
                    //noinspection unchecked
                    setTileEntityBanner(tileEntity, (DyeColor) args[0], (List<Pattern>) args[1]);
                    break;
                case INVENTORY_HOLDER:
                    setTileEntityInventoryHolder(tileEntity, (org.bukkit.inventory.ItemStack[]) args[0], (String) args[1]);
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
    public void setBlock(Location location, Material material, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(location.getChunk(), location, Block.getCombinedId(CraftMagicNumbers.getBlock(material, data)), BlockType.BLOCK);

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

        Executor.ensureMain(() -> {
            for(Entity entity : chunk.getWorld().getEntities(null, bb)){
                if(entity instanceof EntityPlayer)
                    ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutMapChunk);
            }
        });
    }

    @Override
    public void refreshLight(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        for(int i = 0; i < 16; i++) {
            ChunkSection chunkSection = chunk.getSections()[i];
            if(chunkSection == null) {
                chunkSection = new ChunkSection(i << 4, chunk.world.worldProvider.g());
                chunk.getSections()[i] = chunkSection;
            }

            if (chunk.world.worldProvider.g())
                Arrays.fill(chunkSection.getSkyLightArray().asBytes(), (byte) 15);
        }

        chunk.initLighting();
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
    public int getCombinedId(Material material, byte data) {
        return Block.getCombinedId(CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        if(o1.isBlock() && o2.isBlock()) {
            int firstMaterial = Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData());
            int secondMaterial = Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData());
            return Integer.compare(firstMaterial, secondMaterial);
        }
        else{
            return o1.name().compareTo(o2.name());
        }
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
    public void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, org.bukkit.inventory.ItemStack[] contents, String name) {
        NonNullList<ItemStack> items = getItems(tileEntityInventoryHolder);
        for(int i = 0; i < items.size() && i < contents.length; i++){
            items.set(i, CraftItemStack.asNMSCopy(contents[i]));
        }
        setName(tileEntityInventoryHolder, name);
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
        //noinspection deprecation
        mobSpawner.setMobName(EntityTypes.a(spawnedType.getName()));
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(org.bukkit.World bukkitWorld, int x, int z) {
        Chunk chunk = ((CraftWorld) bukkitWorld).getHandle().getChunkProvider().chunks.get(ChunkCoordIntPair.a(x, z));
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
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            Key blockKey = Key.of(type.name());
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (type == Material.SPAWNER) {
                                spawnersLocations.add(new Location(bukkitWorld, (chunkX << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkZ << 4) + bp.getZ()));
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

            if(chunk instanceof Chunk) {
                Arrays.fill(((Chunk) chunk).entitySlices, new UnsafeList<>());

                new HashSet<>(((Chunk) chunk).tileEntities.keySet()).forEach(((Chunk) chunk).world::n);
                ((Chunk) chunk).tileEntities.clear();
            }
            else{
                ((ProtoChunk) chunk).r().clear();
                ((ProtoChunk) chunk).s().clear();
            }

            ChunksTracker.markEmpty(island, ChunkPosition.of(bukkitWorld, chunkX, chunkZ), false);
        }, chunk -> refreshChunk(chunk.bukkitChunk));
    }

    @Override
    public void setChunkBiome(org.bukkit.World bukkitWorld, int chunkX, int chunkZ, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        runActionOnChunk(bukkitWorld, chunkCoords, true, chunk -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
            Arrays.fill(chunk.getBiomeIndex(), biomeBase);
            if(chunk instanceof Chunk)
                ((Chunk) chunk).markDirty();
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

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<IChunkAccess> chunkConsumer, Consumer<Chunk> updateChunk){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        IChunkLoader chunkLoader = world.getChunkProvider().chunkLoader;

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(updateChunk != null)
                updateChunk.accept(chunk);
        }

        else{
            Executor.async(() -> {
                try{
                    ProtoChunk protoChunk = chunkLoader.b(world, chunkCoords.x, chunkCoords.z, null);
                    chunkConsumer.accept(protoChunk);
                    if(saveChunk)
                        chunkLoader.saveChunk(world, protoChunk);
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
                ChunkCoordIntPair chunkCoord = chunk.getPos();
                int chunkRandomTickSpeed = (int) (globalRandomTickSpeeds[chunkWorld.getWorld().getEnvironment().ordinal()] * islandCropGrowthMultiplier);

                int chunkX = chunkCoord.d();
                int chunkZ = chunkCoord.e();

                if (chunkRandomTickSpeed > 0) {
                    for (ChunkSection chunkSection : chunk.getSections()) {
                        if (chunkSection != Chunk.a && chunkSection.b()) {
                            for (int i = 0; i < chunkRandomTickSpeed; i++) {
                                random = random * 3 + 1013904223;
                                int factor = random >> 2;
                                int x = factor & 15;
                                int z = factor >> 8 & 15;
                                int y = factor >> 16 & 15;
                                IBlockData blockData = chunkSection.getType(x, y, z);
                                if (blockData.t() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(blockData.getBlock()).name())) {
                                    blocksToTick.add(new BiPair<>(chunkWorld, new BlockPosition(x + chunkX, y + chunkSection.getYPosition(), z + chunkZ), blockData));
                                }
                            }
                        }
                    }
                }
            }
        }

        Executor.sync(() -> blocksToTick.forEach(pair -> {
            Block block = pair.getZ().getBlock();
            if(!Fields.BLOCK_RANDOM_TICK.isNull())
                Fields.BLOCK_RANDOM_TICK.set(block, true);
            pair.getZ().b(pair.getX(), pair.getY(), ThreadLocalRandom.current());
            if(!Fields.BLOCK_RANDOM_TICK.isNull())
                Fields.BLOCK_RANDOM_TICK.set(block, false);
        }));

        return random;
    }

    @Override
    public Material getMaterial(int combinedId) {
        return CraftMagicNumbers.getMaterial(Block.getByCombinedId(combinedId).getBlock());
    }

    @Override
    public byte getData(int combinedId) {
        return 0;
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        return Key.of(minecart.getDisplayBlockData().getMaterial(), (byte) 0);
    }

    @Override
    public String getTileName(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return tileEntity instanceof INamableTileEntity && ((INamableTileEntity) tileEntity).hasCustomName() ?
                ((INamableTileEntity) tileEntity).getCustomName().getText() : "";
    }

    private NonNullList<ItemStack> getItems(Object tileEntityInventoryHolder){
        if(tileEntityInventoryHolder instanceof TileEntityChest){
            return (NonNullList<ItemStack>) ((TileEntityChest) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityDispenser){
            return (NonNullList<ItemStack>) ((TileEntityDispenser) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityBrewingStand){
            return (NonNullList<ItemStack>) ((TileEntityBrewingStand) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityFurnace){
            return (NonNullList<ItemStack>) ((TileEntityFurnace) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityHopper){
            return (NonNullList<ItemStack>) ((TileEntityHopper) tileEntityInventoryHolder).getContents();
        }
        else if(tileEntityInventoryHolder instanceof TileEntityShulkerBox){
            return (NonNullList<ItemStack>) ((TileEntityShulkerBox) tileEntityInventoryHolder).getContents();
        }

        SuperiorSkyblockPlugin.log("&cCouldn't find inventory holder for class: " + tileEntityInventoryHolder.getClass() + " - contact @Ome_R!");

        return NonNullList.a();
    }

    private void setName(Object tileEntityInventoryHolder, String name){
        if(tileEntityInventoryHolder instanceof TileEntityChest){
            ((TileEntityChest) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
        else if(tileEntityInventoryHolder instanceof TileEntityDispenser){
            ((TileEntityDispenser) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
        else if(tileEntityInventoryHolder instanceof TileEntityBrewingStand){
            ((TileEntityBrewingStand) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
        else if(tileEntityInventoryHolder instanceof TileEntityFurnace){
            ((TileEntityFurnace) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
        else if(tileEntityInventoryHolder instanceof TileEntityHopper){
            ((TileEntityHopper) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
        else if(tileEntityInventoryHolder instanceof TileEntityShulkerBox){
            ((TileEntityShulkerBox) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
    }

}
