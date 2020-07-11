package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.threads.MutableObject;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockFlowerPot;
import net.minecraft.server.v1_15_R1.BlockLeaves;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkProviderServer;
import net.minecraft.server.v1_15_R1.ChunkSection;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumColor;
import net.minecraft.server.v1_15_R1.GameRules;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.INamableTileEntity;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NonNullList;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityBanner;
import net.minecraft.server.v1_15_R1.TileEntityBarrel;
import net.minecraft.server.v1_15_R1.TileEntityBrewingStand;
import net.minecraft.server.v1_15_R1.TileEntityChest;
import net.minecraft.server.v1_15_R1.TileEntityDispenser;
import net.minecraft.server.v1_15_R1.TileEntityFurnace;
import net.minecraft.server.v1_15_R1.TileEntityHopper;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_15_R1.TileEntityShulkerBox;
import net.minecraft.server.v1_15_R1.TileEntitySign;
import net.minecraft.server.v1_15_R1.TileEntitySkull;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSBlocks_v1_15_R1 implements NMSBlocks {

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
    public int getCombinedId(Material material, byte data) {
        return Block.getCombinedId(CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
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
        //noinspection deprecation, OptionalGetWithoutIsPresent
        mobSpawner.setMobName(EntityTypes.a(spawnedType.getName()).get());
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(org.bukkit.World bukkitWorld, int x, int z) {
        Chunk chunk = ((CraftWorld) bukkitWorld).getHandle().getChunkProvider().getChunkAt(x, z, false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> loadChunk(org.bukkit.World bukkitWorld, int chunkX, int chunkZ) {
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        PlayerChunkMap playerChunkMap = world.getChunkProvider().playerChunkMap;
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        ChunkPosition chunkPosition = ChunkPosition.of(bukkitWorld, chunkX, chunkZ);

        CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> completableFuture = new CompletableFuture<>();

        Chunk chunk = world.getChunkIfLoaded(chunkX, chunkZ);
        MutableObject<ChunkSection[]> chunkSections = MutableObject.of(chunk == null ? new ChunkSection[0] : Arrays.copyOf(chunk.getSections(), chunk.getSections().length));

        Executor.async(() -> {
            KeyMap<Integer> blockCounts = new KeyMap<>();
            Set<Location> spawnersLocations = new HashSet<>();

            /* Load chunk from the files without actually loading it to the game. */
            if(chunkSections.get().length == 0){
                try{
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);
                    chunkCompound = chunkCompound == null ? null : playerChunkMap.getChunkData(
                            world.getWorldProvider().getDimensionManager(),
                            Suppliers.ofInstance(world.getWorldPersistentData()),
                            chunkCompound,
                            chunkCoords,
                            world
                    );

                    if(chunkCompound.hasKeyOfType("Level", 10)) {
                        NBTTagCompound levelCompound = chunkCompound.getCompound("Level");
                        NBTTagList sectionsList = levelCompound.getList("Sections", 10);
                        chunkSections.set(new ChunkSection[sectionsList.size()]);

                        for (int i = 0; i < sectionsList.size(); ++i) {
                            NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                            byte yPosition = sectionCompound.getByte("Y");
                            if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                                chunkSections.get()[i] = new ChunkSection(yPosition << 4);
                                chunkSections.get()[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                            }
                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            for(ChunkSection chunkSection : chunkSections.get()){
                if(chunkSection != null){
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Key blockKey = Key.of(CraftMagicNumbers.getMaterial(blockData.getBlock()).name());
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (blockKey.getGlobalKey().equals("SPAWNER")) {
                                spawnersLocations.add(new Location(bukkitWorld, (chunkX << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkZ << 4) + bp.getZ()));
                            }
                        }
                    }
                }
            }

            completableFuture.complete(new BiPair<>(chunkPosition, blockCounts, spawnersLocations));
        });

        return completableFuture;
    }

    @Override
    public int tickIslands(int random) {
        List<Pair<Island, List<org.bukkit.Chunk>>> activeChunks = new ArrayList<>();
        List<BiPair<WorldServer, BlockPosition, IBlockData>> blocksToTick = new ArrayList<>();
        org.bukkit.World normalWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.NORMAL),
                netherWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.NETHER),
                endWorld = plugin.getGrid().getIslandsWorld(org.bukkit.World.Environment.THE_END);
        int[] globalRandomTickSpeeds = new int[] {
                normalWorld == null ? 0 : ((CraftWorld) normalWorld).getHandle().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED),
                netherWorld == null ? 0 : ((CraftWorld) netherWorld).getHandle().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED),
                endWorld == null ? 0 : ((CraftWorld) endWorld).getHandle().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED)
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
                        if (chunkSection != Chunk.a && chunkSection.d()) {
                            for (int i = 0; i < chunkRandomTickSpeed; i++) {
                                BlockPosition blockPosition = chunkWorld.a(chunkX, chunkSection.getYPosition(),chunkZ, 15);
                                IBlockData blockData = chunkSection.getType(
                                        blockPosition.getX() - chunkX,
                                        blockPosition.getY() - chunkSection.getYPosition(),
                                        blockPosition.getZ() - chunkZ);
                                if (blockData.q() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(blockData.getBlock()).name())) {
                                    blocksToTick.add(new BiPair<>(chunkWorld, blockPosition, blockData));
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
        else if(tileEntityInventoryHolder instanceof TileEntityBarrel){
            return (NonNullList<ItemStack>) ((TileEntityBarrel) tileEntityInventoryHolder).getContents();
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
        else if(tileEntityInventoryHolder instanceof TileEntityBarrel){
            ((TileEntityBarrel) tileEntityInventoryHolder).setCustomName(IChatBaseComponent.ChatSerializer.a(name));
        }
    }

}
