package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;

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
        String id = this.tileEntityData.getString("id");

        if (id.equalsIgnoreCase(ServerVersion.isLegacy() ? "Sign" : "minecraft:sign")) {
            boolean needSignFormat = false;
            for (int i = 1; i <= 4; i++) {
                boolean isDefaultSignLine = false;
                String line;

                if ((i - 1) >= plugin.getSettings().getDefaultSign().size()) {
                    line = this.tileEntityData.getString("Text" + i);
                } else {
                    line = plugin.getSettings().getDefaultSign().get(i - 1);
                    if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                        isDefaultSignLine = true;
                        needSignFormat = true;
                    }
                }

                if (line != null) {
                    this.tileEntityData.setString((isDefaultSignLine ? "SSB.Text" : "Text") + i, line
                            .replace("{player}", island.getOwner().getName())
                            .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                    );
                }
            }
            if (needSignFormat)
                this.tileEntityData.setByte("SSB.HasSignLines", (byte) 1);
        } else if (id.equalsIgnoreCase(ServerVersion.isLegacy() ? "Chest" : "minecraft:chest")) {
            if (plugin.getSettings().getDefaultContainers().isEnabled()) {
                String inventoryType = this.tileEntityData.getString("inventoryType");
                if (inventoryType != null) {
                    try {
                        InventoryType containerType = InventoryType.valueOf(inventoryType);
                        ListTag items = plugin.getSettings().getDefaultContainers().getContents(containerType);
                        if (items != null)
                            this.tileEntityData.setTag("Items", items.copy());
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public boolean shouldPostPlace() {
        return this.tileEntityData != null && (this.tileEntityData.containsKey("Text1") ||
                this.tileEntityData.containsKey("Text2") ||
                this.tileEntityData.containsKey("Text3") ||
                this.tileEntityData.containsKey("Text4")
        );
    }

    public void doPostPlace(Island island) {
        try {
            plugin.getNMSWorld().placeSign(island, location);
        } finally {
            this.tileEntityData = null;
        }
    }

}
