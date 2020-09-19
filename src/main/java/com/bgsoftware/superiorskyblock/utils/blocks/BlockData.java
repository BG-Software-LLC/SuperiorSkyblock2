package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

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

        if(plugin.getSettings().defaultContainersEnabled) {
            String inventoryType = clonedTileEntity.getString("inventoryType");
            if (inventoryType != null) {
                try {
                    InventoryType containerType = InventoryType.valueOf(inventoryType);
                    Registry<Integer, ItemStack> containerContents = plugin.getSettings().defaultContainersContents.get(containerType);
                    if(containerContents != null) {
                        ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                        containerContents.entries().forEach(itemEntry -> {
                            CompoundTag itemCompound = new CompoundTag();
                            itemCompound.setString("id", plugin.getNMSAdapter().getMinecraftKey(itemEntry.getValue()));
                            itemCompound.setByte("Count", (byte) itemEntry.getValue().getAmount());
                            itemCompound.setByte("Slot", (byte) (int) itemEntry.getKey());
                            items.addTag(itemCompound);
                        });
                        clonedTileEntity.setTag("Items", items);
                    }
                }catch (Exception ignored){}
            }
        }
    }

    public void doPostPlace(Island island){
        if(clonedTileEntity != null && (clonedTileEntity.containsKey("Text1") || clonedTileEntity.containsKey("Text2") ||
                clonedTileEntity.containsKey("Text3") || clonedTileEntity.containsKey("Text4")))
            plugin.getNMSBlocks().handleSignPlace(island, location);
    }

    private static String getSignLine(int index, String def){
        return index >= plugin.getSettings().defaultSignLines.size() ? def :
                plugin.getSettings().defaultSignLines.get(index);
    }

}
