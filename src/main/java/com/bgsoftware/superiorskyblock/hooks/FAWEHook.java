package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.schematics.SchematicBlock;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.World;

import java.util.List;

public class FAWEHook {

    private static boolean enabled = false;

    public static boolean isEnabled(){
        return enabled;
    }

    public static void register(){
        enabled = true;
    }

    public static void setBlocks(List<SchematicBlock> blocks, Runnable callback){
        World world = blocks.get(0).getLocation().getWorld();
        EditSession editSession = new EditSessionBuilder(world.getName()).fastmode(true).autoQueue(false).build();
        for(SchematicBlock block : blocks){
            editSession.setBlock(block.getX(), block.getY(), block.getZ(), new BaseBlock(block.getId(), block.getData()));
        }
        editSession.addNotifyTask(callback);
        editSession.flushQueue();
    }

}
