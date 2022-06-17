package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Nullable;

public class SchematicBlock {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Location location;
    private final SchematicBlockData schematicBlockData;
    @Nullable
    private CompoundTag tileEntityData = null;

    public SchematicBlock(Location location, SchematicBlockData schematicBlockData) {
        this.location = location;
        this.schematicBlockData = schematicBlockData;
    }

    private static String getSignLine(int index, String def) {
        return index >= plugin.getSettings().getDefaultSign().size() ? def :
                plugin.getSettings().getDefaultSign().get(index);
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public World getWorld() {
        return location.getWorld();
    }

    public int getCombinedId() {
        return schematicBlockData.getCombinedId();
    }

    public byte getSkyLightLevel() {
        return schematicBlockData.getSkyLightLevel();
    }

    public byte getBlockLightLevel() {
        return schematicBlockData.getBlockLightLevel();
    }

    @Nullable
    public CompoundTag getStatesTag() {
        return schematicBlockData.getStatesTag();
    }

    @Nullable
    public CompoundTag getOriginalTileEntity() {
        CompoundTag tileEntity = schematicBlockData.getTileEntity();
        return tileEntity == null ? null : new CompoundTag(tileEntity);
    }

    @Nullable
    public CompoundTag getTileEntityData() {
        return this.tileEntityData;
    }

    public void doPrePlace(Island island) {
        CompoundTag originalTileEntity = getOriginalTileEntity();

        if (originalTileEntity == null)
            return;

        this.tileEntityData = new CompoundTag(originalTileEntity);
        for (int i = 1; i <= 4; i++) {
            String line = getSignLine(i - 1, this.tileEntityData.getString("Text" + i));
            if (line != null)
                this.tileEntityData.setString("Text" + i, line
                        .replace("{player}", island.getOwner().getName())
                        .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                );
        }

        if (plugin.getSettings().getDefaultContainers().isEnabled()) {
            String inventoryType = this.tileEntityData.getString("inventoryType");
            if (inventoryType != null) {
                try {
                    InventoryType containerType = InventoryType.valueOf(inventoryType);
                    ListTag items = plugin.getSettings().getDefaultContainers().getContents(containerType);
                    if (items != null)
                        this.tileEntityData.setTag("Items", items.copy());
                } catch (Exception error) {
                    PluginDebugger.debug(error);
                }
            }
        }
    }

    public void doPostPlace(Island island) {
        if (this.tileEntityData == null)
            return;

        try {
            if ((this.tileEntityData.containsKey("Text1") || this.tileEntityData.containsKey("Text2") ||
                    this.tileEntityData.containsKey("Text3") || this.tileEntityData.containsKey("Text4")))
                plugin.getNMSWorld().placeSign(island, location);
        } finally {
            this.tileEntityData = null;
        }
    }

}
