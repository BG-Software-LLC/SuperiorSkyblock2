package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandArea;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.island.container.value.SyncedValue;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SIslandBase implements IslandBase {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final DatabaseBridge databaseBridge = plugin.getFactory().createDatabaseBridge(this);

    /*
     * Island Identifiers
     */
    protected final UUID uuid;
    protected SuperiorPlayer owner;
    protected final BlockPosition center;
    protected final long creationTime;
    protected String creationTimeDate;

    protected volatile long lastTimeUpdate = -1;
    protected volatile String islandName;
    protected volatile String islandRawName;

    protected final Synchronized<Value<Integer>> islandSize;

    protected final AtomicInteger generatedSchematics = new AtomicInteger(0);

    public SIslandBase(@Nullable SuperiorPlayer owner, UUID uuid, Location location, String islandName,
                       long creationTime, Value<Integer> islandSize, int generatedSchematic) {
        this.uuid = uuid;
        this.owner = owner;

        if (owner != null) {
            owner.setPlayerRole(SPlayerRole.lastRole());
            owner.setIsland(this);
        }

        this.center = new SBlockPosition(location);
        this.creationTime = creationTime;
        this.creationTimeDate = Formatters.DATE_FORMATTER.format(new Date(creationTime * 1000));
        this.islandName = islandName;
        this.islandRawName = Formatters.STRIP_COLOR_FORMATTER.format(islandName);
        this.islandSize = Synchronized.of(islandSize);
        this.generatedSchematics.set(generatedSchematic);
    }

    @Override
    public SuperiorPlayer getOwner() {
        return this.owner;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getCreationTimeDate() {
        return this.creationTimeDate;
    }

    @Override
    public void updateDatesFormatter() {
        this.creationTimeDate = Formatters.DATE_FORMATTER.format(new Date(creationTime * 1000));
    }

    @Override
    public long getLastTimeUpdate() {
        return lastTimeUpdate;
    }

    @Override
    public boolean isSpawn() {
        return false;
    }

    @Override
    public String getName() {
        return plugin.getSettings().getIslandNames().isColorSupport() ? islandName : islandRawName;
    }

    @Override
    public void setName(String islandName) {
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        PluginDebugger.debug("Action: Set Name, Island: " + owner.getName() + ", Name: " + islandName);

        this.islandName = islandName;
        this.islandRawName = Formatters.STRIP_COLOR_FORMATTER.format(this.islandName);

        IslandsDatabaseBridge.saveName(this);
    }

    @Override
    public String getRawName() {
        return islandRawName;
    }

    @Override
    public <T extends IslandBase> T loadIsland(IslandLoadLevel<T> loadLevel) {
        return plugin.getGrid().getIslandByUUID(uuid, loadLevel);
    }

    @Override
    public Location getCenter(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        World world = plugin.getGrid().getIslandsWorld(this, environment);

        Preconditions.checkNotNull(world, "Couldn't find world for environment " + environment + ".");

        return center.parse(world).add(0.5, 0, 0.5);
    }

    @Override
    public int getIslandSize() {
        if (plugin.getSettings().isBuildOutsideIsland())
            return (int) Math.round(plugin.getSettings().getMaxIslandSize() * 1.5);

        return this.islandSize.readAndGet(Value::get);
    }

    @Override
    public void setIslandSize(int islandSize) {
        islandSize = Math.max(1, islandSize);

        PluginDebugger.debug("Action: Set Size, Island: " + owner.getName() + ", Size: " + islandSize);

        this.islandSize.set(Value.fixed(islandSize));

        IslandsDatabaseBridge.saveSize(this);
    }

    @Override
    public int getIslandSizeRaw() {
        return this.islandSize.readAndGet(islandSize -> islandSize instanceof SyncedValue ? -1 : islandSize.get());
    }

    @Override
    public Location getMinimum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMinimumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandSize, 0, islandSize);
    }

    @Override
    public Location getMaximum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandSize, 0, islandSize);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        List<Chunk> chunks = new LinkedList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getAllChunks(environment, onlyProtected));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(environment, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return getAllChunks(environment, onlyProtected, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = getCenter(environment).getWorld();
        return new SequentialListBuilder<Chunk>()
                .build(IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks), ChunkPosition::loadChunk);
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        return Collections.emptyList();
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getAllChunksAsync(this, world, onlyProtected, noEmptyChunks, ChunkLoadReason.API_REQUEST, onChunkLoad);
    }

    @Override
    public boolean isInside(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        IslandArea islandArea = new IslandArea(this.center, islandDistance);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return isInsideRange(location, 0);
    }

    public boolean isInsideRange(Location location, int extra) {
        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.expand(extra);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

        if (chunk.getWorld() == null || !plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.rshift(4);

        return islandArea.intercepts(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        int generateBitChange = getGeneratedSchematicBitMask(environment);

        if (generateBitChange == 0)
            return false;

        return (generatedSchematics.get() & generateBitChange) != 0;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        setSchematicGenerate(environment, true);
    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        PluginDebugger.debug("Action: Set Schematic, Island: " + owner.getName() + ", Environment: " + environment);

        int generateBitChange = getGeneratedSchematicBitMask(environment);

        if (generateBitChange == 0)
            return;

        this.generatedSchematics.updateAndGet(generatedSchematics -> {
            return generated ? generatedSchematics | generateBitChange : generatedSchematics & ~generateBitChange & 0xF;
        });

        IslandsDatabaseBridge.saveGeneratedSchematics(this);
    }

    @Override
    public int getGeneratedSchematicsFlag() {
        return this.generatedSchematics.get();
    }

    @Override
    public boolean isIgnored() {
        return false;
    }

    @Override
    public void setIgnored(boolean ignored) {

    }

    @Override
    public int getPosition(SortingType sortingType) {
        return 0;
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    private static int getGeneratedSchematicBitMask(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return 8;
            case NETHER:
                return 4;
            case THE_END:
                return 3;
            default:
                return 0;
        }
    }

}
