package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WorldEditSchematic7 extends BaseSchematic implements Schematic {

    private final Clipboard clipboard;
    private List<ChunkPosition> affectedChunks = Collections.emptyList();

    public WorldEditSchematic7(String name, Clipboard clipboard) {
        super(name);
        this.clipboard = clipboard;
        readBlocks();
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        pasteSchematic(island, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, Consumer<Throwable> onFailure) {
        Log.debug(Debug.PASTE_SCHEMATIC, this.name, island.getOwner().getName(), location);

        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();
        BlockVector3 origin = clipboard.getOrigin();

        int targetMinX = location.getBlockX() + (min.getX() - origin.getX());
        int targetMinZ = location.getBlockZ() + (min.getZ() - origin.getZ());
        int targetMaxX = location.getBlockX() + (max.getX() - origin.getX());
        int targetMaxZ = location.getBlockZ() + (max.getZ() - origin.getZ());

        List<ChunkPosition> affected = new ArrayList<>();
        org.bukkit.World world = location.getWorld();
        for (int x = targetMinX >> 4; x <= targetMaxX >> 4; x++) {
            for (int z = targetMinZ >> 4; z <= targetMaxZ >> 4; z++) {
                affected.add(ChunkPosition.of(world, x, z, false));
            }
        }
        this.affectedChunks = affected;

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);

            Log.debugResult(Debug.PASTE_SCHEMATIC, "Task Finished", "");
            
            island.handleBlocksPlace(cachedCounts);
            PluginEventsFactory.callIslandSchematicPasteEvent(island, null, name, location);
            
            if (callback != null) {
                callback.run();
            }
        } catch (WorldEditException ex) {
            if (onFailure != null) {
                onFailure.accept(ex);
            }
        } catch (Throwable ex) {
            if (onFailure != null) {
                onFailure.accept(ex);
            }
        }
    }

    @Override
    public Location adjustRotation(Location location) {
        return location;
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        return this.affectedChunks;
    }

    @Override
    public Runnable onTeleportCallback() {
        return null;
    }

    private void readBlocks() {
        for (BlockVector3 pos : clipboard.getRegion()) {
            try {
                BlockState blockState = clipboard.getBlock(pos);
                Material material = BukkitAdapter.adapt(blockState.getBlockType());
                Key key = Keys.of(material, (byte) 0);
                cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
            } catch (Exception ignored) {
            }
        }
    }
}
