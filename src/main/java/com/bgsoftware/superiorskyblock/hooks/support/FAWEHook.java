package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.schematic.WorldEditSchematic;
import com.boydti.fawe.FaweAPI;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public final class FAWEHook {

    public static Schematic loadSchematic(String name, File file) throws IOException {
        return new WorldEditSchematic(name, FaweAPI.load(file));
    }

    public static boolean isEnabled(){
        return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

}
