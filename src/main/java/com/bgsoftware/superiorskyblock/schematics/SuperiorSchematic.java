package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;

import org.bukkit.Location;

public final class SuperiorSchematic implements Schematic {

    private CompoundTag compoundTag;

    public SuperiorSchematic(CompoundTag compoundTag){
        this.compoundTag = compoundTag;
    }

    @Override
    public void pasteSchematic(Location location, Runnable callback){
        pasteSchematic(null, location, callback);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        byte xSize = ((ByteTag) compoundTag.getValue().get("xSize")).getValue();
        byte ySize = ((ByteTag) compoundTag.getValue().get("ySize")).getValue();
        byte zSize = ((ByteTag) compoundTag.getValue().get("zSize")).getValue();
        int offsetX = ((IntTag) compoundTag.getValue().getOrDefault("offsetX", new IntTag(xSize / 2))).getValue();
        int offsetY = ((IntTag) compoundTag.getValue().getOrDefault("offsetY", new IntTag(ySize / 2))).getValue();
        int offsetZ = ((IntTag) compoundTag.getValue().getOrDefault("offsetZ", new IntTag(zSize / 2))).getValue();

        Location min = location.clone().subtract(offsetX, offsetY, offsetZ);

        if(compoundTag.getValue().containsKey("blocks")) {
            TagUtils.assignIntoBlocks(island, ((ListTag) compoundTag.getValue().get("blocks")).getValue(), min, callback);
        }

        if(compoundTag.getValue().containsKey("entities")) {
            for (Tag tag : ((ListTag) compoundTag.getValue().get("entities")).getValue()) {
                TagUtils.spawnEntity((CompoundTag) tag, location);
            }
        }
    }

    public CompoundTag getTag(){
        return compoundTag;
    }

//    @Override
//    public String toString() {
//        return compoundTag.toString();
//    }

}
