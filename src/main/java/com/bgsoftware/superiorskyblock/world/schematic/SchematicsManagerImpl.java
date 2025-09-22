package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.SchematicOptions;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.FloatTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.WorldReader;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.schematic.container.SchematicsContainer;
import com.bgsoftware.superiorskyblock.world.schematic.impl.CachedSuperiorSchematic;
import com.bgsoftware.superiorskyblock.world.schematic.impl.SuperiorSchematic;
import com.bgsoftware.superiorskyblock.world.schematic.parser.DefaultSchematicParser;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SchematicsManagerImpl extends Manager implements SchematicManager {

    private final SchematicsContainer schematicsContainer;

    public SchematicsManagerImpl(SuperiorSkyblockPlugin plugin, SchematicsContainer schematicsContainer) {
        super(plugin);
        this.schematicsContainer = schematicsContainer;
    }

    public void loadData() throws ManagerLoadException {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            Resources.saveResource("schematics/desert.schematic");
            Resources.saveResource("schematics/desert_nether.schematic", "schematics/normal_nether.schematic");
            Resources.saveResource("schematics/desert_the_end.schematic", "schematics/normal_the_end.schematic");
            Resources.saveResource("schematics/mycel.schematic");
            Resources.saveResource("schematics/mycel_nether.schematic", "schematics/normal_nether.schematic");
            Resources.saveResource("schematics/mycel_the_end.schematic", "schematics/normal_the_end.schematic");
            Resources.saveResource("schematics/normal.schematic");
            Resources.saveResource("schematics/normal_nether.schematic");
            Resources.saveResource("schematics/normal_the_end.schematic");
        }

        loadDefaultSchematicParsers();

        loadSchematics();
    }

    public void loadSchematics() throws ManagerLoadException {
        this.schematicsContainer.clearSchematics();

        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        for (File schemFile : Files.listFolderFiles(schematicsFolder, false)) {
            String schemName = Files.getFileName(schemFile).toLowerCase(Locale.ENGLISH);
            Schematic schematic = loadFromFile(schemName, schemFile);
            if (schematic != null) {
                this.schematicsContainer.addSchematic(schematic);
            }
        }

        if (this.schematicsContainer.getSchematics().isEmpty()) {
            throw new ManagerLoadException("&cThere were no valid schematics.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        System.gc();
    }

    public void cacheSchematics() {
        if (!plugin.getSettings().isCacheSchematics() || plugin.getSettings().getMaxIslandSize() % 4 != 0)
            return;

        List<Schematic> newSchematics = new LinkedList<>();
        boolean cachedSchematic = false;

        for (Schematic schematic : this.schematicsContainer.getSchematics().values()) {
            if (schematic instanceof SuperiorSchematic) {
                try {
                    schematic = new CachedSuperiorSchematic((SuperiorSchematic) schematic);
                    cachedSchematic = true;
                } catch (Throwable error) {
                    Log.warn("Cannot cache schematic ", schematic.getName(), ", skipping...");
                    error.printStackTrace();
                }
            }
            newSchematics.add(schematic);
        }

        if (!cachedSchematic)
            return;

        this.schematicsContainer.clearSchematics();
        newSchematics.forEach(this.schematicsContainer::addSchematic);
    }

    private void loadDefaultSchematicParsers() {
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            try {
                Class.forName("com.boydti.fawe.object.schematic.Schematic");
                SchematicParser schematicParser = (SchematicParser) Class.forName("com.bgsoftware.superiorskyblock.world.schematic.parser.FAWESchematicParser").newInstance();
                this.schematicsContainer.addSchematicParser(schematicParser);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Schematic getSchematic(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.schematicsContainer.getSchematic(name);
    }

    @Override
    public List<String> getSchematics() {
        return new SequentialListBuilder<String>()
                .build(this.schematicsContainer.getSchematics().keySet());
    }

    @Override
    public void registerSchematicParser(SchematicParser schematicParser) {
        Preconditions.checkNotNull(schematicParser, "schematicParser parameter cannot be null.");
        this.schematicsContainer.addSchematicParser(schematicParser);
    }

    @Override
    public List<SchematicParser> getSchematicParsers() {
        return this.schematicsContainer.getSchematicParsers();
    }

    @Override
    public void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName) {
        saveSchematic(superiorPlayer, schematicName, false);
    }

    @Override
    public void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName, boolean saveAir) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkArgument(superiorPlayer.isOnline(), "superiorPlayer must be online.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        BlockPosition pos1 = superiorPlayer.getSchematicPos1();
        BlockPosition pos2 = superiorPlayer.getSchematicPos2();

        Location offset;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            offset = superiorPlayer.getLocation(wrapper.getHandle()).subtract(
                    Math.min(pos1.getX(), pos2.getX()),
                    Math.min(pos1.getY(), pos2.getY()) + 1,
                    Math.min(pos1.getZ(), pos2.getZ())
            );
        }

        SchematicOptions schematicOptions = SchematicOptions.newBuilder(schematicName)
                .setOffset(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ())
                .setDirection(offset.getYaw(), offset.getPitch())
                .setSaveAir(saveAir)
                .build();

        saveSchematic(pos1.parse(), pos2.parse(), schematicOptions, () -> Message.SCHEMATIC_SAVED.send(superiorPlayer));

        superiorPlayer.setSchematicPos1(null);
        superiorPlayer.setSchematicPos2(null);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        saveSchematic(pos1, pos2, offsetX, offsetY, offsetZ, 0, 0, schematicName);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        saveSchematic(pos1, pos2, offsetX, offsetY, offsetZ, yaw, pitch, schematicName, null);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, String schematicName, Runnable callable) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        saveSchematic(pos1, pos2, offsetX, offsetY, offsetZ, 0, 0, schematicName, callable);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch,
                              String schematicName, @Nullable Runnable callable) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        SchematicOptions schematicOptions = SchematicOptions.newBuilder(schematicName)
                .setOffset(offsetX, offsetY, offsetZ)
                .setDirection(yaw, pitch)
                .build();

        saveSchematic(pos1, pos2, schematicOptions, callable);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, SchematicOptions schematicOptions) {
        saveSchematic(pos1, pos2, schematicOptions, null);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, SchematicOptions schematicOptions, @Nullable Runnable callable) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicOptions, "schematicOptions parameter cannot be null.");

        Log.debug(Debug.SAVE_SCHEMATIC, pos1, pos2, schematicOptions.getOffsetX(), schematicOptions.getOffsetY(),
                schematicOptions.getOffsetZ(), schematicOptions.getYaw(), schematicOptions.getPitch(),
                schematicOptions.shouldSaveAir(), schematicOptions.getSchematicName());

        World world = pos1.getWorld();

        int minBlockX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minBlockY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minBlockZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxBlockX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxBlockY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxBlockZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int xSize = maxBlockX - minBlockX;
        int ySize = maxBlockY - minBlockY;
        int zSize = maxBlockZ - minBlockZ;

        WorldReader worldReader = new WorldReader(world, ChunkLoadReason.SCHEMATIC_SAVE);

        int minChunkX = minBlockX >> 4;
        int minChunkZ = minBlockZ >> 4;
        int maxChunkX = maxBlockX >> 4;
        int maxChunkZ = maxBlockZ >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                worldReader.prepareChunk(ChunkPosition.of(world, chunkX, chunkZ));
            }
        }

        worldReader.finish(() -> {
            List<Tag<?>> entities = new ArrayList<>();
            List<Tag<?>> blocks = new ArrayList<>();

            for (int x = minBlockX; x <= maxBlockX; ++x) {
                for (int z = minBlockZ; z <= maxBlockZ; ++z) {
                    ChunkReader chunkReader = worldReader.getChunkReader(x, z);

                    int offsetBlockX = x - minBlockX;
                    int offsetBlockZ = z - minBlockZ;

                    int chunkBlockX = x & 0xF;
                    int chunkBlockZ = z & 0xF;

                    for (int chunkBlockY = minBlockY; chunkBlockY <= maxBlockY; ++chunkBlockY) {
                        Material blockType = chunkReader.getType(chunkBlockX, chunkBlockY, chunkBlockZ);
                        if (!schematicOptions.shouldSaveAir() && blockType == Material.AIR)
                            continue;

                        short blockData = chunkReader.getData(chunkBlockX, chunkBlockY, chunkBlockZ);
                        CompoundTag blockStates = chunkReader.readBlockStates(chunkBlockX, chunkBlockY, chunkBlockZ);
                        byte[] lightLevels = chunkReader.getLightLevels(chunkBlockX, chunkBlockY, chunkBlockZ);
                        CompoundTag tileEntity = chunkReader.getTileEntity(chunkBlockX, chunkBlockY, chunkBlockZ);

                        int offsetBlockY = chunkBlockY - minBlockY;

                        blocks.add(new SchematicBuilder()
                                .withBlockOffset(SBlockOffset.fromOffsets(offsetBlockX, offsetBlockY, offsetBlockZ))
                                .withBlockType(blockType, blockData)
                                .withStates(blockStates)
                                .withLightLevels(lightLevels)
                                .withTileEntity(tileEntity)
                                .build()
                        );
                    }
                }
            }

            for (int x = minChunkX; x <= maxChunkX; ++x) {
                for (int z = minChunkZ; z <= maxChunkZ; ++z) {
                    ChunkReader chunkReader = worldReader.getChunkReader(x << 4, z << 4);
                    if (chunkReader != null) {
                        chunkReader.forEachEntity((entityType, entityTag, location) -> {
                            if (entityType != EntityType.PLAYER &&
                                    location.getBlockX() >= minBlockX && location.getBlockX() <= maxBlockX &&
                                    location.getBlockY() >= minBlockY && location.getBlockY() <= maxBlockY &&
                                    location.getBlockZ() >= minBlockZ && location.getBlockZ() <= maxBlockZ) {
                                location.subtract(minBlockX, minBlockY, minBlockZ);
                                entities.add(new SchematicBuilder().applyEntity(entityType, entityTag, location).build());
                            }
                        });
                    }
                }
            }

            Map<String, Tag<?>> compoundValue = new HashMap<>();
            compoundValue.put("xSize", IntTag.of(xSize));
            compoundValue.put("ySize", IntTag.of(ySize));
            compoundValue.put("zSize", IntTag.of(zSize));
            compoundValue.put("blocks", ListTag.of(blocks, CompoundTag.class));
            compoundValue.put("entities", ListTag.of(entities, CompoundTag.class));
            compoundValue.put("offsetX", IntTag.of(schematicOptions.getOffsetX()));
            compoundValue.put("offsetY", IntTag.of(schematicOptions.getOffsetY()));
            compoundValue.put("offsetZ", IntTag.of(schematicOptions.getOffsetZ()));
            compoundValue.put("yaw", FloatTag.of(schematicOptions.getYaw()));
            compoundValue.put("pitch", FloatTag.of(schematicOptions.getPitch()));
            compoundValue.put("version", StringTag.of(ServerVersion.getBukkitVersion()));
            if (!ServerVersion.isLegacy())
                compoundValue.put("minecraftDataVersion", IntTag.of(plugin.getNMSAlgorithms().getDataVersion()));

            CompoundTag schematicTag = CompoundTag.of(compoundValue);
            SuperiorSchematic schematic = new SuperiorSchematic(schematicOptions.getSchematicName(), schematicTag);
            this.schematicsContainer.addSchematic(schematic);
            saveIntoFile(schematicOptions.getSchematicName(), schematicTag);

            if (callable != null)
                BukkitExecutor.sync(callable);
        });

    }

    public String getDefaultSchematic(Dimension dimension) {
        String suffix = "_" + dimension.getName().toLowerCase(Locale.ENGLISH);
        for (String schematicName : this.schematicsContainer.getSchematics().keySet()) {
            if (getSchematic(schematicName + suffix) != null)
                return schematicName;
        }

        return "";
    }

    private Schematic parseSchematic(File file, String schemName, SchematicParser schematicParser,
                                     Consumer<SchematicParseException> onSchematicParseError) {
        try (DataInputStream reader = new DataInputStream(new GZIPInputStream(java.nio.file.Files.newInputStream(file.toPath())))) {
            return schematicParser.parseSchematic(reader, schemName);
        } catch (SchematicParseException error) {
            onSchematicParseError.accept(error);
        } catch (Exception error) {
            Log.entering("SchematicsManagerImpl", "parseSchematic", "ENTER", file.getName(), schemName);
            Log.error(error, "An unexpected error occurred while loading schematic:");
        }

        return null;
    }

    private Schematic loadFromFile(String schemName, File file) {
        Schematic schematic = null;
        SchematicParser usedParser = null;

        for (SchematicParser schematicParser : this.schematicsContainer.getSchematicParsers()) {
            schematic = parseSchematic(file, schemName, schematicParser, error -> {
            });
            if (schematic != null) {
                usedParser = schematicParser;
                break;
            }
        }

        if (schematic == null) {
            schematic = parseSchematic(file, schemName, DefaultSchematicParser.getInstance(), error ->
                    Log.warn("Schematic ", file.getName(), " is not a valid schematic, ignoring...")
            );
            if (schematic != null)
                usedParser = DefaultSchematicParser.getInstance();
        }

        if (schematic != null && usedParser != null) {
            Log.info("Successfully loaded schematic ", file.getName(), " (", usedParser.getClass().getSimpleName(), ")");
        }

        return schematic;
    }

    private void saveIntoFile(String name, CompoundTag schematicTag) {
        try {
            File file = new File(plugin.getDataFolder(), "schematics/" + name + ".schematic");

            if (file.exists())
                file.delete();

            file.getParentFile().mkdirs();
            file.createNewFile();

            try (DataOutputStream writer = new DataOutputStream(new GZIPOutputStream(java.nio.file.Files.newOutputStream(file.toPath())))) {
                schematicTag.write(writer);
            }
        } catch (IOException error) {
            Log.entering("SchematicsManagerImpl", "saveIntoFile", "ENTER", name);
            Log.error(error, "An unexpected error occurred while saving schematic into file:");
        }
    }

    private List<Entity> getEntities(Location min, Location max) {
        List<Entity> livingEntities = new LinkedList<>();

        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Chunk currentChunk = min.getWorld().getChunkAt(x, z);
                for (Entity entity : currentChunk.getEntities()) {
                    try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                        if (!(entity instanceof Player) && betweenLocations(entity.getLocation(wrapper.getHandle()), min, max))
                            livingEntities.add(entity);
                    }
                }
            }
        }

        return livingEntities;
    }

    private boolean betweenLocations(Location location, Location min, Location max) {
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

}
