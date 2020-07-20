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
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntArrayTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BiomeBase;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockBed;
import net.minecraft.server.v1_15_R1.BlockFlowerPot;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.BlockProperties;
import net.minecraft.server.v1_15_R1.BlockStateBoolean;
import net.minecraft.server.v1_15_R1.BlockStateEnum;
import net.minecraft.server.v1_15_R1.BlockStateInteger;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkConverter;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkProviderServer;
import net.minecraft.server.v1_15_R1.ChunkRegionLoader;
import net.minecraft.server.v1_15_R1.ChunkSection;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumColor;
import net.minecraft.server.v1_15_R1.GameRules;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IBlockState;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.INamableTileEntity;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NonNullList;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.ProtoChunk;
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
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_15_R1.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ConstantConditions", "rawtypes"})
public final class NMSBlocks_v1_15_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<String, BlockStateEnum> nameToBlockState = new HashMap<>();
    private static final Map<BlockStateEnum, String> blockStateToName = new HashMap<>();

    static {
        register("axis", BlockProperties.D);
        register("axis-empty", BlockProperties.E);
        register("facing", BlockProperties.L);
        register("facing-notup", BlockProperties.M);
        register("facing-horizontal", BlockProperties.N);
        register("face", BlockProperties.O);
        register("attachment", BlockProperties.P);
        register("redstone-east", BlockProperties.Q);
        register("redstone-north", BlockProperties.R);
        register("redstone-south", BlockProperties.S);
        register("redstone-west", BlockProperties.T);
        register("double-half", BlockProperties.U);
        register("half", BlockProperties.V);
        register("track-shape-empty", BlockProperties.W);
        register("track-shape", BlockProperties.X);
        register("part", BlockProperties.ax);
        register("chest-type", BlockProperties.ay);
        register("comparator-mode", BlockProperties.az);
        register("hinge", BlockProperties.aA);
        register("instrument", BlockProperties.aB);
        register("piston-type", BlockProperties.aC);
        register("slab-type", BlockProperties.aD);
        register("shape", BlockProperties.aE);
        register("mode", BlockProperties.aF);
        register("leaves", BlockProperties.aG);
    }

    private static void register(String key, BlockStateEnum<?> blockStateEnum){
        nameToBlockState.put(key, blockStateEnum);
        blockStateToName.put(blockStateEnum, key);
    }

    @Override
    public void setBlock(org.bukkit.Chunk bukkitChunk, Location location, int combinedId, CompoundTag statesTag, BlockType blockType, Object... args) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(statesTag != null){
            for(Map.Entry<String, Tag<?>> entry : statesTag.getValue().entrySet()){
                try {
                    if (entry.getValue() instanceof ByteTag) {
                        blockData = blockData.set(BlockStateBoolean.of(entry.getKey()), ((ByteTag) entry.getValue()).getValue() == 1);
                    } else if (entry.getValue() instanceof IntArrayTag) {
                        int[] data = ((IntArrayTag) entry.getValue()).getValue();
                        blockData = blockData.set(BlockStateInteger.of(entry.getKey(), data[1], data[2]), data[0]);
                    } else if (entry.getValue() instanceof StringTag) {
                        String data = ((StringTag) entry.getValue()).getValue();
                        BlockStateEnum blockStateEnum = nameToBlockState.get(entry.getKey());
                        if(blockStateEnum != null)
                            //noinspection unchecked
                            blockData = blockData.set(blockStateEnum, Enum.valueOf(blockStateEnum.b(), data));
                    }
                }catch (Exception ignored){}
            }
        }

        if((blockData.getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) || blockData.getBlock() instanceof BlockBed) {
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
        setBlock(location.getChunk(), location, Block.getCombinedId(CraftMagicNumbers.getBlock(material, data)), null, BlockType.BLOCK);

        AxisAlignedBB bb = new AxisAlignedBB(blockPosition.getX() - 60, 0, blockPosition.getZ() - 60,
                blockPosition.getX() + 60, 256, blockPosition.getZ() + 60);

        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(world, blockPosition);

        for(Entity entity : world.getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutBlockChange);
        }
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = world.getType(blockPosition);
        CompoundTag compoundTag = null;

        for(Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.getStateMap().entrySet()){
            if(compoundTag == null)
                compoundTag = new CompoundTag();

            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = entry.getKey().a();

            if(keyClass.equals(BlockStateBoolean.class)) {
                value = new ByteTag((Boolean) entry.getValue() ? (byte) 1 : 0);
            }
            else if(keyClass.equals(BlockStateInteger.class)) {
                BlockStateInteger key = (BlockStateInteger) entry.getKey();
                value = new IntArrayTag(new int[] {(Integer) entry.getValue(), key.min, key.max});
            }
            else{
                BlockStateEnum<?> key = (BlockStateEnum<?>) entry.getKey();
                name = blockStateToName.get(key);
                value = new StringTag(((Enum<?>) entry.getValue()).name());
            }

            compoundTag.setTag(name, value);
        }

        return compoundTag;
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
        return Block.getCombinedId(CraftMagicNumbers.getBlock(material).getBlockData());
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
    public CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> calculateChunk(org.bukkit.World bukkitWorld, int chunkX, int chunkZ) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        ChunkPosition chunkPosition = ChunkPosition.of(bukkitWorld, chunkX, chunkZ);

        CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> completableFuture = new CompletableFuture<>();
        KeyMap<Integer> blockCounts = new KeyMap<>();
        Set<Location> spawnersLocations = new HashSet<>();

        Consumer<ChunkSection[]> calculateConsumer = chunkSections -> {
            for(ChunkSection chunkSection : chunkSections){
                if(chunkSection != null){
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(bukkitWorld, (chunkX << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkZ << 4) + bp.getZ());
                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            Key blockKey = Key.of(type.name(), location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (type == Material.SPAWNER) {
                                spawnersLocations.add(location);
                            }
                        }
                    }
                }
            }

            completableFuture.complete(new BiPair<>(chunkPosition, blockCounts, spawnersLocations));
        };

        //noinspection all
        runActionOnChunk(bukkitWorld, chunkCoords, false, chunk -> {
                    calculateConsumer.accept(chunk.getSections());
                },
                levelCompound -> {
                    NBTTagList sectionsList = levelCompound.getList("Sections", 10);
                    ChunkSection[] chunkSections = new ChunkSection[sectionsList.size()];

                    for (int i = 0; i < sectionsList.size(); ++i) {
                        NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                        byte yPosition = sectionCompound.getByte("Y");
                        if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                            chunkSections[i] = new ChunkSection(yPosition << 4);
                            chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                        }
                    }

                    calculateConsumer.accept(chunkSections);
                });

        return completableFuture;
    }

    @Override
    public void deleteChunk(Island island, org.bukkit.World bukkitWorld, int chunkX, int chunkZ) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        runActionOnChunk(bukkitWorld, chunkCoords, true, chunk -> {
                    Arrays.fill(chunk.getSections(), Chunk.a);
                    Arrays.fill(chunk.entitySlices, new UnsafeList<>());

                    new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
                    chunk.tileEntities.clear();

                    refreshChunk(chunk.getBukkitChunk());
                },
                levelCompound -> {
                    levelCompound.set("Sections", new NBTTagList());
                    levelCompound.set("TileEntities", new NBTTagList());
                    levelCompound.set("Entities", new NBTTagList());
                });

        ChunksTracker.markEmpty(island, ChunkPosition.of(bukkitWorld, chunkX, chunkZ), false);
    }

    @Override
    public void setChunkBiome(org.bukkit.World bukkitWorld, int chunkX, int chunkZ, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
        runActionOnChunk(bukkitWorld, chunkCoords, true, chunk -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

            BiomeBase[] biomeBases = (BiomeBase[]) Fields.BIOME_STORAGE_BIOME_BASES.get(chunk.getBiomeIndex());

            if(biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkX + "," + chunkZ + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkX, chunkZ);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        },
        levelCompound -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
            int[] biomes = levelCompound.hasKeyOfType("Biomes", 11) ? levelCompound.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, IRegistry.BIOME.a(biomeBase));
            levelCompound.setIntArray("Biomes", biomes);
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        PlayerChunkMap playerChunkMap = world.getChunkProvider().playerChunkMap;

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
        }

        else{
            Executor.async(() -> {
                try{
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if(chunkCompound == null){
                        ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                        chunkCompound = ChunkRegionLoader.saveChunk(world, protoChunk);
                    }

                    else{
                        chunkCompound = playerChunkMap.getChunkData(world.getWorldProvider().getDimensionManager(),
                                Suppliers.ofInstance(world.getWorldPersistentData()), chunkCompound, chunkCoords, world);
                    }

                    if(chunkCompound.hasKeyOfType("Level", 10)) {
                        compoundConsumer.accept(chunkCompound.getCompound("Level"));
                        if(saveChunk)
                            playerChunkMap.a(chunkCoords, chunkCompound);
                    }
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
