package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.schematics.WorldEditSchematic;
import com.boydti.fawe.FaweAPI;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public final class FAWEHook {

    public static Schematic loadSchematic(File file) throws IOException {
        return new WorldEditSchematic(FaweAPI.load(file));
    }

    public static boolean isEnabled(){
        return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

}
