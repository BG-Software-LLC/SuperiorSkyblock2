package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;

public final class BlockData {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Location location;
    private final int combinedId;
    private final byte skyLightLevel, blockLightLevel;
    private final CompoundTag statesTag, tileEntity;

    private CompoundTag clonedTileEntity = null;

    public BlockData(Location location, int combinedId, byte skyLightLevel, byte blockLightLevel, CompoundTag statesTag, CompoundTag tileEntity){
        this.location = location;
        this.combinedId = combinedId;
        this.skyLightLevel = skyLightLevel;
        this.blockLightLevel = blockLightLevel;
        this.statesTag = statesTag;
        this.tileEntity = tileEntity;
    }

    public int getX(){
        return location.getBlockX();
    }

    public int getY(){
        return location.getBlockY();
    }

    public int getZ(){
        return location.getBlockZ();
    }

    public World getWorld(){
        return location.getWorld();
    }

    public int getCombinedId() {
        return combinedId;
    }

    public byte getSkyLightLevel() {
        return skyLightLevel;
    }

    public byte getBlockLightLevel() {
        return blockLightLevel;
    }

    public CompoundTag getStatesTag() {
        return statesTag;
    }

    public CompoundTag getTileEntity() {
        return tileEntity;
    }

    public CompoundTag getClonedTileEntity() {
        return clonedTileEntity;
    }

    public void doPrePlace(Island island){
        if(tileEntity == null)
            return;

        clonedTileEntity = new CompoundTag(tileEntity);
        for (int i = 1; i <= 4; i++) {
            String line = getSignLine(i - 1, clonedTileEntity.getString("Text" + i));
            if (line != null)
                clonedTileEntity.setString("Text" + i, line
                        .replace("{player}", island.getOwner().getName())
                        .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                );
        }

        if(plugin.getSettings().getDefaultContainers().isEnabled()) {
            String inventoryType = clonedTileEntity.getString("inventoryType");
            if (inventoryType != null) {
                try {
                    InventoryType containerType = InventoryType.valueOf(inventoryType);
                    ListTag items = plugin.getSettings().getDefaultContainers().getContents(containerType);
                    if(items != null)
                        clonedTileEntity.setTag("Items", new ListTag(CompoundTag.class, items.getValue()));
                }catch (Exception ignored){}
            }
        }
    }

    public void doPostPlace(Island island){
        if(clonedTileEntity != null && (clonedTileEntity.containsKey("Text1") || clonedTileEntity.containsKey("Text2") ||
                clonedTileEntity.containsKey("Text3") || clonedTileEntity.containsKey("Text4")))
            plugin.getNMSWorld().placeSign(island, location);
    }

    private static String getSignLine(int index, String def){
        return index >= plugin.getSettings().getDefaultSign().size() ? def :
                plugin.getSettings().getDefaultSign().get(index);
    }

}
