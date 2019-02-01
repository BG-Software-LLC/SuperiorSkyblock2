package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.utils.jnbt.ByteTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.utils.TagUtil;

import org.bukkit.Location;

public final class SSchematic implements Schematic {

    private CompoundTag compoundTag;

    public SSchematic(CompoundTag compoundTag){
        this.compoundTag = compoundTag;
    }

    @Override
    public void pasteSchematic(Location location){
        byte xSize = ((ByteTag) compoundTag.getValue().get("xSize")).getValue();
        byte ySize = ((ByteTag) compoundTag.getValue().get("ySize")).getValue();
        byte zSize = ((ByteTag) compoundTag.getValue().get("zSize")).getValue();
        int offsetX = ((IntTag) compoundTag.getValue().getOrDefault("offsetX", new IntTag(xSize / 2))).getValue();
        int offsetY = ((IntTag) compoundTag.getValue().getOrDefault("offsetY", new IntTag(ySize / 2))).getValue();
        int offsetZ = ((IntTag) compoundTag.getValue().getOrDefault("offsetZ", new IntTag(zSize / 2))).getValue();

        Location min = location.clone().subtract(offsetX, offsetY, offsetZ);

        if(compoundTag.getValue().containsKey("blocks")) {
            for (Tag tag : ((ListTag) compoundTag.getValue().get("blocks")).getValue()) {
                TagUtil.assignIntoBlock((CompoundTag) tag, min);
            }
        }

        if(compoundTag.getValue().containsKey("entities")) {
            for (Tag tag : ((ListTag) compoundTag.getValue().get("entities")).getValue()) {
                TagUtil.spawnEntity((CompoundTag) tag, location);
            }
        }
    }

    public CompoundTag getTag(){
        return compoundTag;
    }

    @Override
    public String toString() {
        return compoundTag.toString();
    }

}
