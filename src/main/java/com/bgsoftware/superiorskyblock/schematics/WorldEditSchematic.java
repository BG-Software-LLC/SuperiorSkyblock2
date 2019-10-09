package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;

import java.lang.reflect.Method;

public final class WorldEditSchematic implements Schematic {

    private static Method blockVector3AtMethod = null;
    private static Method blockVector3PasteMethod = null;

    static {
        try{
            Class blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            blockVector3AtMethod = blockVector3Class.getMethod("at", int.class, int.class, int.class);
            blockVector3PasteMethod = com.boydti.fawe.object.schematic.Schematic.class
                    .getMethod("paste", World.class, blockVector3Class, boolean.class, boolean.class, Transform.class);
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }

    private com.boydti.fawe.object.schematic.Schematic schematic;


    public WorldEditSchematic(com.boydti.fawe.object.schematic.Schematic schematic){
        this.schematic = schematic;
    }

    @Override
    public void pasteSchematic(Location location, Runnable callback) {
        pasteSchematic(null, location, callback);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        EditSession editSession;

        try{
            Object point = blockVector3AtMethod.invoke(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
            editSession = (EditSession) blockVector3PasteMethod.invoke(schematic, new BukkitWorld(location.getWorld()), point, false, true, null);
        }catch(Throwable ex){
            com.sk89q.worldedit.Vector point = new com.sk89q.worldedit.Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            editSession = schematic.paste(new BukkitWorld(location.getWorld()), point, false, true, null);
        }

        editSession.addNotifyTask(callback);
    }
}
