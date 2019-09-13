package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Location;

public final class WorldEditSchematic implements Schematic {

    private com.boydti.fawe.object.schematic.Schematic schematic;

    public WorldEditSchematic(com.boydti.fawe.object.schematic.Schematic schematic){
        this.schematic = schematic;
    }

    @Override
    public void pasteSchematic(Location location, Runnable callback) {
        Vector point = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        EditSession editSession = schematic.paste(new BukkitWorld(location.getWorld()), point, false, true, null);
        editSession.addNotifyTask(callback);
    }

}
