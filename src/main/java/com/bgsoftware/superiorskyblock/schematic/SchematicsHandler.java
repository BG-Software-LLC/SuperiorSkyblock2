package com.bgsoftware.superiorskyblock.schematic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.schematic.container.SchematicsContainer;
import com.bgsoftware.superiorskyblock.schematic.parser.DefaultSchematicParser;
import com.bgsoftware.superiorskyblock.schematic.parser.FAWESchematicParser;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.FloatTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.tag.TagBuilder;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class SchematicsHandler extends AbstractHandler implements SchematicManager {

    private final SchematicsContainer schematicsContainer;

    public SchematicsHandler(SuperiorSkyblockPlugin plugin, SchematicsContainer schematicsContainer) {
        super(plugin);
        this.schematicsContainer = schematicsContainer;
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException("Not supported for SchematicsHandler.");
    }

    public void loadDataWithException() throws HandlerLoadException {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            FileUtils.saveResource("schematics/desert.schematic");
            FileUtils.saveResource("schematics/desert_nether.schematic", "schematics/normal_nether.schematic");
            FileUtils.saveResource("schematics/desert_the_end.schematic", "schematics/normal_the_end.schematic");
            FileUtils.saveResource("schematics/mycel.schematic");
            FileUtils.saveResource("schematics/mycel_nether.schematic", "schematics/normal_nether.schematic");
            FileUtils.saveResource("schematics/mycel_the_end.schematic", "schematics/normal_the_end.schematic");
            FileUtils.saveResource("schematics/normal.schematic");
            FileUtils.saveResource("schematics/normal_nether.schematic");
            FileUtils.saveResource("schematics/normal_the_end.schematic");
        }

        loadDefaultSchematicParsers();

        //noinspection ConstantConditions
        for (File schemFile : schematicsFolder.listFiles()) {
            String schemName = schemFile.getName().replace(".schematic", "").replace(".schem", "").toLowerCase();
            Schematic schematic = loadFromFile(schemName, schemFile);
            if (schematic != null) {
                this.schematicsContainer.addSchematic(schematic);
            }
        }

        if (this.schematicsContainer.getSchematicNames().isEmpty()) {
            throw new HandlerLoadException("&cThere were no valid schematics.",
                    HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }
    }

    private void loadDefaultSchematicParsers() {
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
            this.schematicsContainer.addSchematicParser(FAWESchematicParser.getInstance());
    }

    @Override
    public Schematic getSchematic(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.schematicsContainer.getSchematic(name);
    }

    @Override
    public List<String> getSchematics() {
        return this.schematicsContainer.getSchematicNames();
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
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(superiorPlayer.getLocation(), "superiorPlayer must be online.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        Location pos1 = superiorPlayer.getSchematicPos1().parse(), pos2 = superiorPlayer.getSchematicPos2().parse();
        Location min = new Location(pos1.getWorld(), Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        Location offset = superiorPlayer.getLocation().clone().subtract(min.clone().add(0, 1, 0));

        saveSchematic(superiorPlayer.getSchematicPos1().parse(), superiorPlayer.getSchematicPos2().parse(),
                offset.getBlockX(), offset.getBlockY(), offset.getBlockZ(), offset.getYaw(), offset.getPitch(), schematicName, () ->
                        Locale.SCHEMATIC_SAVED.send(superiorPlayer));

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
    public void saveSchematic(Location pos1, Location pos2, int offsetX, int offsetY, int offsetZ, float yaw, float pitch, String schematicName, @Nullable Runnable runnable) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        SuperiorSkyblockPlugin.debug("Action: Save Schematic, Pos #1: " + LocationUtils.getLocation(pos1) +
                ", Pos #2: " + LocationUtils.getLocation(pos2) + ", OffsetX: " + offsetX + ", OffsetY: " + offsetY +
                ", OffsetZ: " + offsetZ + ", Yaw: " + yaw + ", Pitch: " + pitch + ", Name: " + schematicName);

        World world = pos1.getWorld();
        Location min = new Location(world, Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        Location max = new Location(world, Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));

        int xSize = max.getBlockX() - min.getBlockX();
        int ySize = max.getBlockY() - min.getBlockY();
        int zSize = max.getBlockZ() - min.getBlockZ();

        List<Tag<?>> blocks = new ArrayList<>(), entities = new ArrayList<>();

        for (int x = 0; x <= xSize; x++) {
            for (int z = 0; z <= zSize; z++) {
                for (int y = 0; y <= ySize; y++) {
                    int _x = x + min.getBlockX(), _y = y + min.getBlockY(), _z = z + min.getBlockZ();
                    Block block = world.getBlockAt(_x, _y, _z);
                    Material blockType = block.getType();
                    Location blockLocation = block.getLocation();

                    if (blockType != Material.AIR) {
                        CompoundTag tileEntity = plugin.getNMSWorld().readTileEntity(blockLocation);
                        if (tileEntity != null && block.getState() instanceof InventoryHolder)
                            tileEntity.setString("inventoryType", ((InventoryHolder) block.getState()).getInventory().getType().name());

                        //noinspection deprecation
                        blocks.add(new TagBuilder()
                                .withBlockPosition(SchematicPosition.of(x, y, z))
                                .withBlockType(blockLocation, blockType, block.getData())
                                .withStates(plugin.getNMSWorld().readBlockStates(blockLocation))
                                .withLightLevels(plugin.getNMSWorld().getLightLevels(blockLocation))
                                .withTileEntity(tileEntity)
                                .build()
                        );
                    }
                }
            }
        }

        for (Entity livingEntity : getEntities(min, max)) {
            entities.add(new TagBuilder().applyEntity(livingEntity, min).build());
        }

        Map<String, Tag<?>> compoundValue = new HashMap<>();
        compoundValue.put("xSize", new IntTag(xSize));
        compoundValue.put("ySize", new IntTag(ySize));
        compoundValue.put("zSize", new IntTag(zSize));
        compoundValue.put("blocks", new ListTag(CompoundTag.class, blocks));
        compoundValue.put("entities", new ListTag(CompoundTag.class, entities));
        compoundValue.put("offsetX", new IntTag(offsetX));
        compoundValue.put("offsetY", new IntTag(offsetY));
        compoundValue.put("offsetZ", new IntTag(offsetZ));
        compoundValue.put("yaw", new FloatTag(yaw));
        compoundValue.put("pitch", new FloatTag(pitch));
        compoundValue.put("version", new StringTag(ServerVersion.getBukkitVersion()));

        SuperiorSchematic schematic = new SuperiorSchematic(schematicName, new CompoundTag(compoundValue));
        this.schematicsContainer.addSchematic(schematic);
        saveIntoFile(schematicName, schematic);

        if (runnable != null)
            runnable.run();
    }

    public String getDefaultSchematic(World.Environment environment) {
        String suffix = environment == World.Environment.NETHER ? "_nether" : "_the_end";
        for (String schematicName : this.schematicsContainer.getSchematicNames()) {
            if (getSchematic(schematicName + suffix) != null)
                return schematicName;
        }

        return "";
    }

    private Schematic parseSchematic(File file, String schemName, SchematicParser schematicParser,
                                     Consumer<SchematicParseException> onSchematicParseError) {
        try (DataInputStream reader = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            return schematicParser.parseSchematic(reader, schemName);
        } catch (SchematicParseException error) {
            onSchematicParseError.accept(error);
        } catch (Exception error) {
            SuperiorSkyblockPlugin.log("&cAn unexpected error occurred while loading schematic " + file.getName() + ":");
            error.printStackTrace();
            SuperiorSkyblockPlugin.debug(error);
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
                    SuperiorSkyblockPlugin.log("&cSchematic " + file.getName() + " is not a valid schematic, ignoring..."));
            if (schematic != null)
                usedParser = DefaultSchematicParser.getInstance();
        }

        if (schematic != null && usedParser != null) {
            SuperiorSkyblockPlugin.log("Successfully loaded schematic " + file.getName() + " (" + usedParser.getClass().getSimpleName() + ")");
        }

        return schematic;
    }

    private void saveIntoFile(String name, SuperiorSchematic schematic) {
        try {
            File file = new File(plugin.getDataFolder(), "schematics/" + name + ".schematic");

            if (file.exists())
                file.delete();

            file.getParentFile().mkdirs();
            file.createNewFile();

            try (DataOutputStream writer = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
                schematic.getTag().write(writer);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            SuperiorSkyblockPlugin.debug(ex);
        }
    }

    private List<Entity> getEntities(Location min, Location max) {
        List<Entity> livingEntities = new ArrayList<>();

        Chunk minChunk = min.getChunk(), maxChunk = max.getChunk();
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Chunk currentChunk = min.getWorld().getChunkAt(x, z);
                for (Entity entity : currentChunk.getEntities()) {
                    if (!(entity instanceof Player) && betweenLocations(entity.getLocation(), min, max))
                        livingEntities.add(entity);
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
