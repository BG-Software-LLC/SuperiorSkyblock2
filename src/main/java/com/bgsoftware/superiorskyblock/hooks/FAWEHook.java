package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.wrappers.BlockPosition;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public final class FAWEHook {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static boolean enabled = false;

    public static boolean isEnabled(){
        return enabled;
    }

    public static void register(){
        enabled = true;
    }

    @SuppressWarnings("deprecation")
    public static void setBlocks(List<Tag> blocks, Location offset, Runnable callback){
        World world = plugin.getGrid().getIslandsWorld();
        EditSession editSession = new EditSessionBuilder(world.getName()).fastmode(true).autoQueue(false).build();
        for(Tag tag : blocks){
            Map<String, Tag> compoundValue = ((CompoundTag) tag).getValue();
            Location block = BlockPosition.of(((StringTag) compoundValue.get("blockPosition")).getValue()).addToLocation(offset);
            int combinedId = ((IntTag) compoundValue.get("combinedId")).getValue();
            try {
                editSession.setBlock(new Vector(block.getBlockX(), block.getBlockY(), block.getBlockZ()), new BaseBlock(combinedId & 4095, combinedId >> 12 & 15));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editSession.addNotifyTask(callback);
        editSession.flushQueue();
    }

}
