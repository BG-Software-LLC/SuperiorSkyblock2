package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectField;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntArrayTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import net.minecraft.server.v1_16_R2.AxisAlignedBB;
import net.minecraft.server.v1_16_R2.BiomeBase;
import net.minecraft.server.v1_16_R2.BiomeStorage;
import net.minecraft.server.v1_16_R2.Block;
import net.minecraft.server.v1_16_R2.BlockBed;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.BlockProperties;
import net.minecraft.server.v1_16_R2.BlockStateBoolean;
import net.minecraft.server.v1_16_R2.BlockStateEnum;
import net.minecraft.server.v1_16_R2.BlockStateInteger;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.Chunk;
import net.minecraft.server.v1_16_R2.ChunkConverter;
import net.minecraft.server.v1_16_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R2.ChunkProviderServer;
import net.minecraft.server.v1_16_R2.ChunkRegionLoader;
import net.minecraft.server.v1_16_R2.ChunkSection;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.GameRules;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.IBlockState;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.ITickable;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagList;
import net.minecraft.server.v1_16_R2.PacketPlayOutBlockChange;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import net.minecraft.server.v1_16_R2.ProtoChunk;
import net.minecraft.server.v1_16_R2.TileEntity;
import net.minecraft.server.v1_16_R2.TileEntitySign;
import net.minecraft.server.v1_16_R2.TileEntityTypes;
import net.minecraft.server.v1_16_R2.World;
import net.minecraft.server.v1_16_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R2.util.UnsafeList;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
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
public final class NMSBlocks_v1_16_R2 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<String, IBlockState> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState, String> blockStateToName = new HashMap<>();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "h");

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("F", "axis-empty");
        fieldNameToName.put("N", "facing-notup");
        fieldNameToName.put("O", "facing-horizontal");
        fieldNameToName.put("S", "wall-east");
        fieldNameToName.put("T", "wall-north");
        fieldNameToName.put("U", "wall-south");
        fieldNameToName.put("V", "wall-west");
        fieldNameToName.put("W", "redstone-east");
        fieldNameToName.put("X", "redstone-north");
        fieldNameToName.put("Y", "redstone-south");
        fieldNameToName.put("Z", "redstone-west");
        fieldNameToName.put("aa", "double-half");
        fieldNameToName.put("ac", "track-shape-empty");
        fieldNameToName.put("ad", "track-shape");
        fieldNameToName.put("ae", "age1");
        fieldNameToName.put("af", "age2");
        fieldNameToName.put("ag", "age3");
        fieldNameToName.put("ah", "age5");
        fieldNameToName.put("ai", "age7");
        fieldNameToName.put("aj", "age15");
        fieldNameToName.put("ak", "age25");
        fieldNameToName.put("ar", "level3");
        fieldNameToName.put("as", "level8");
        fieldNameToName.put("at", "level1-8");
        fieldNameToName.put("av", "level15");
        fieldNameToName.put("an", "distance1-7");
        fieldNameToName.put("aB", "distance7");
        fieldNameToName.put("aF", "chest-type");
        fieldNameToName.put("aG", "comparator-mode");
        fieldNameToName.put("aJ", "piston-type");
        fieldNameToName.put("aK", "slab-type");

        try{
            for(Field field : BlockProperties.class.getFields()){
                Object value = field.get(null);
                if(value instanceof IBlockState) {
                    register(fieldNameToName.getOrDefault(field.getName(), ((IBlockState) value).getName()),
                            field.getName(), (IBlockState) value);
                }
            }
        }catch (Exception ignored){}
    }

    private static void register(String key, String fieldName, IBlockState<?> blockState){
        if(nameToBlockState.containsKey(key)){
            SuperiorSkyblockPlugin.log("&cWarning: block state " + key + "(" + fieldName + ") already exists. Contact Ome_R!");
        }
        else {
            nameToBlockState.put(key, blockState);
            blockStateToName.put(blockState, key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setBlock(org.bukkit.Chunk bukkitChunk, Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(statesTag != null){
            for(Map.Entry<String, Tag<?>> entry : statesTag.getValue().entrySet()){
                try {
                    IBlockState blockState = nameToBlockState.get(entry.getKey());
                    if(blockState != null) {
                        if (entry.getValue() instanceof ByteTag) {
                            blockData = blockData.set(blockState, ((ByteTag) entry.getValue()).getValue() == 1);
                        } else if (entry.getValue() instanceof IntArrayTag) {
                            int[] data = ((IntArrayTag) entry.getValue()).getValue();
                            blockData = blockData.set(blockState, data[0]);
                        } else if (entry.getValue() instanceof StringTag) {
                            String data = ((StringTag) entry.getValue()).getValue();
                            blockData = blockData.set(blockState, Enum.valueOf(blockState.getType(), data));
                        }
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

        if(tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            tileEntityCompound.setInt("x", blockPosition.getX());
            tileEntityCompound.setInt("y", blockPosition.getY());
            tileEntityCompound.setInt("z", blockPosition.getZ());
            world.getTileEntity(blockPosition).load(blockData, tileEntityCompound);
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(location.getChunk(), location, getCombinedId(material, data), null, null);

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
            String name = entry.getKey().getName();

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
    public CompoundTag readTileEntity(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if(tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = tileEntity.save(new NBTTagCompound());

        tileEntityCompound.remove("x");
        tileEntityCompound.remove("y");
        tileEntityCompound.remove("z");

        return CompoundTag.fromNBT(tileEntityCompound);
    }

    @Override
    public String parseSignLine(String original) {
        return IChatBaseComponent.ChatSerializer.a(CraftChatMessage.fromString(original)[0]);
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
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData =  world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        return Block.getCombinedId(data == 0 ? CraftMagicNumbers.getBlock(material).getBlockData() :
                CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProvider()
                .getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> calculateChunk(ChunkPosition chunkPosition) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());

        CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> completableFuture = new CompletableFuture<>();
        KeyMap<Integer> blockCounts = new KeyMap<>();
        Set<Location> spawnersLocations = new HashSet<>();

        Consumer<ChunkSection[]> calculateConsumer = chunkSections -> {
            for(ChunkSection chunkSection : chunkSections){
                if(chunkSection != null){
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(chunkPosition.getWorld(), (chunkCoords.x << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkCoords.z << 4) + bp.getZ());
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
        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, false, chunk -> {
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
    public void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);
            Arrays.fill(chunk.entitySlices, new UnsafeList<>());

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
            chunk.tileEntities.clear();

            if(!(world.generator instanceof WorldGenerator)){
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, world.getChunkProvider().chunkGenerator, world.generator);
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                customChunkGenerator.buildBase(null, protoChunk);

                for(int i = 0; i < 16; i++)
                    chunk.getSections()[i] = protoChunk.getSections()[i];

                for(Map.Entry<BlockPosition, TileEntity> entry : protoChunk.x().entrySet())
                    world.setTileEntity(entry.getKey(), entry.getValue());
            }

            refreshChunk(chunk.getBukkitChunk());
        },
        levelCompound -> {
            NBTTagList sectionsList = new NBTTagList();
            NBTTagList tileEntities = new NBTTagList();

            levelCompound.set("Sections", sectionsList);
            levelCompound.set("TileEntities", tileEntities);
            levelCompound.set("Entities", new NBTTagList());

            if(!(world.generator instanceof WorldGenerator)) {
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, world.getChunkProvider().chunkGenerator, world.generator);
                customChunkGenerator.buildBase(null, protoChunk);
                ChunkSection[] chunkSections = protoChunk.getSections();

                for(int i = -1; i < 17; ++i) {
                    int chunkSectionIndex = i;
                    ChunkSection chunkSection = Arrays.stream(chunkSections).filter(_chunkPosition ->
                            _chunkPosition != null && _chunkPosition.getYPosition() >> 4 == chunkSectionIndex)
                            .findFirst().orElse(Chunk.a);

                    if (chunkSection != Chunk.a) {
                        NBTTagCompound sectionCompound = new NBTTagCompound();
                        sectionCompound.setByte("Y", (byte) (i & 255));
                        chunkSection.getBlocks().a(sectionCompound, "Palette", "BlockStates");
                        sectionsList.add(sectionCompound);
                    }
                }

                for(BlockPosition tilePosition : protoChunk.c()){
                    NBTTagCompound tileCompound = protoChunk.i(tilePosition);
                    if(tileCompound != null)
                        tileEntities.add(tileCompound);
                }
            }
        });

        ChunksTracker.markEmpty(island, chunkPosition, false);
    }

    @Override
    public void setChunkBiome(ChunkPosition chunkPosition, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();
        IRegistry<BiomeBase> biomeBaseRegistry = world.r().b(IRegistry.ay);
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, chunk -> {
            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if(biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.x + "," + chunkCoords.z + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        },
        levelCompound -> {
            int[] biomes = levelCompound.hasKeyOfType("Biomes", 11) ? levelCompound.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, biomeBaseRegistry.a(biomeBase));
            levelCompound.setIntArray("Biomes", biomes);
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        runActionOnChunk(bukkitWorld, chunkCoords, saveChunk, null, chunkConsumer, compoundConsumer);
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Runnable onFinish, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        PlayerChunkMap playerChunkMap = world.getChunkProvider().playerChunkMap;

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(onFinish != null)
                onFinish.run();
        }

        else{
            Executor.createTask().runAsync(v -> {
                try{
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if(chunkCompound == null){
                        ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                        chunkCompound = ChunkRegionLoader.saveChunk(world, protoChunk);
                    }

                    else{
                        chunkCompound = playerChunkMap.getChunkData(world.getTypeKey(),
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
            }).runSync(v -> {
                if(onFinish != null)
                    onFinish.run();
            });
        }
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if(stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.tickingChunks
                    .remove(((CraftChunk) chunk).getHandle().getPos().pair());
            if(cropsTickingTileEntity != null)
                cropsTickingTileEntity.getWorld().tileEntityListTick.remove(cropsTickingTileEntity);
        }
        else
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
    }

    @Override
    public void handleSignPlace(Island island, Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if(tileEntity instanceof TileEntitySign) {
            TileEntitySign tileEntitySign = (TileEntitySign) tileEntity;
            String[] lines = new String[4];
            System.arraycopy(CraftSign.revertComponents(tileEntitySign.lines), 0, lines, 0, lines.length);
            String[] strippedLines = new String[4];
            for (int i = 0; i < 4; i++)
                strippedLines[i] = StringUtils.stripColors(lines[i]);

            IChatBaseComponent[] newLines;

            if (BlocksListener.IMP.onSignPlace(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
        }
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

    private static final class CropsTickingTileEntity extends TileEntity implements ITickable{

        private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
        private static int random = ThreadLocalRandom.current().nextInt();

        private final Island island;
        private final Chunk chunk;
        private final int chunkX, chunkZ;

        private int currentTick = 0;

        private CropsTickingTileEntity(Island island, Chunk chunk){
            super(TileEntityTypes.COMMAND_BLOCK);
            this.island = island;
            this.chunk = chunk;
            this.chunkX = chunk.getPos().x;
            this.chunkZ = chunk.getPos().z;
            setLocation(chunk.getWorld(), new BlockPosition(chunkX << 4, 1, chunkZ << 4));
            world.tileEntityListTick.add(this);
        }

        @Override
        public void tick() {
            if(++currentTick <= plugin.getSettings().cropsInterval)
                return;

            currentTick = 0;

            int worldRandomTick = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            double cropGrowth = island.getCropGrowthMultiplier() - 1;

            int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().cropsInterval);

            if (chunkRandomTickSpeed > 0) {
                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != Chunk.a && chunkSection.d()) {
                        for (int i = 0; i < chunkRandomTickSpeed; i++) {
                            random = random * 3 + 1013904223;
                            int factor = random >> 2;
                            int x = factor & 15;
                            int z = factor >> 8 & 15;
                            int y = factor >> 16 & 15;
                            IBlockData blockData = chunkSection.getType(x, y, z);
                            Block block = blockData.getBlock();
                            if (block.isTicking(blockData) && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(block).name())) {
                                blockData.b((WorldServer) world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)), ThreadLocalRandom.current());
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void w() {
            tick();
        }

        static void create(Island island, Chunk chunk){
            long chunkPair = chunk.getPos().pair();
            if(!tickingChunks.containsKey(chunkPair))
                tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk));
        }

    }

}
